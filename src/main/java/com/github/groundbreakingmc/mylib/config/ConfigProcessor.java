package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.colorizer.ColorizerFactory;
import com.github.groundbreakingmc.mylib.colorizer.component.ComponentColorizer;
import com.github.groundbreakingmc.mylib.colorizer.legacy.StringColorizer;
import com.github.groundbreakingmc.mylib.config.annotations.Config;
import com.github.groundbreakingmc.mylib.config.annotations.Section;
import com.github.groundbreakingmc.mylib.config.annotations.Value;
import com.github.groundbreakingmc.mylib.logger.console.Logger;
import com.github.groundbreakingmc.mylib.utils.player.settings.EffectSettings;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import com.github.groundbreakingmc.mylib.utils.player.settings.TitleSettings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public abstract class ConfigProcessor {

    protected final Plugin plugin;
    protected final Logger logger;
    protected final boolean debug;
    @Getter
    private StringColorizer stringColorizer;
    @Getter
    private ComponentColorizer componentColorizer;
    protected final Map<String, Field> sections;

    protected ConfigProcessor(@NotNull Plugin plugin, @NotNull Logger logger, final boolean debug) {
        this.plugin = plugin;
        this.logger = logger;
        this.debug = debug;
        this.sections = new Object2ObjectOpenHashMap<>();
    }

    protected ConfigProcessor(@NotNull Plugin plugin, @NotNull Logger logger, final boolean debug, @Nullable StringColorizer stringColorizer) {
        this.plugin = plugin;
        this.logger = logger;
        this.debug = debug;
        this.stringColorizer = stringColorizer;
        this.sections = new Object2ObjectOpenHashMap<>();
    }

    protected ConfigProcessor(@NotNull Plugin plugin, @NotNull Logger logger, final boolean debug, @Nullable ComponentColorizer componentColorizer) {
        this.plugin = plugin;
        this.logger = logger;
        this.debug = debug;
        this.componentColorizer = componentColorizer;
        this.sections = new Object2ObjectOpenHashMap<>();
    }

    public final ConfigurationNode setupValues() throws IllegalAccessException {
        final Class<?> clazz = this.getClass();
        if (!clazz.isAnnotationPresent(Config.class)) {
            throw new UnsupportedOperationException("Class is not annotated with required annotation!");
        }

        final Config configAnnotation = clazz.getAnnotation(Config.class);
        final ConfigurationNode config = this.getConfig(configAnnotation.fileName(), configAnnotation.version(), configAnnotation.versionPath());

        if (!configAnnotation.colorizerPath().isEmpty()) {
            final String mode = config.node(configAnnotation.colorizerPath().split("\\.")).getString();
            this.componentColorizer = ColorizerFactory.createComponentColorizer(mode);
            this.stringColorizer = componentColorizer.getStringColorizer();
        }

        this.setupFields(this, config);
        return config;
    }

    public final void setupValues(final ConfigurationNode config) throws IllegalAccessException {
        this.setupFields(this, config);
    }

    public final ConfigurationNode getConfig(final String fileName, final Double fileVersion, final String versionPath) {
        final ConfigurateLoader.Loader loader = ConfigurateLoader.loader(this.plugin, this.logger).fileName(fileName);

        if (fileVersion != null) {
            loader.fileVersion(fileVersion);
        }

        if (versionPath != null) {
            loader.fileVersionPath(versionPath);
        }

        return loader.load();
    }

    private void setupFields(final Object object, final ConfigurationNode node) throws IllegalAccessException {
        for (final Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Value.class)) {
                try {
                    final Object value = this.getValue(field, node, field.getAnnotation(Value.class));
                    if (value != null) {
                        field.set(object, value);
                    }
                } catch (final Exception ex) {
                    throw new RuntimeException("Failed to set value for field: " + field.getName(), ex);
                }
            } else if (field.isAnnotationPresent(Section.class)) {
                final Object objectClass = field.get(object);
                if (objectClass != null) {
                    try {
                        final Section sectionAnnotation = field.getAnnotation(Section.class);
                        this.setupFields(objectClass, node.node(sectionAnnotation.name()));
                        if (!this.sections.containsKey(sectionAnnotation.name())) {
                            this.sections.put(sectionAnnotation.name(), field);
                        }
                    } catch (final Exception ex) {
                        throw new RuntimeException("Failed to set section: " + field.getName(), ex);
                    }
                } else {
                    throw new RuntimeException("Section not specified for field: " + field.getName());
                }
            }
        }
    }

    @Nullable
    private Object getValue(final Field field, final ConfigurationNode node, final Value values) throws SerializationException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final ConfigurationNode valueNode = node.node(values.path().split("\\."));
        if (valueNode.virtual()) {
            return null;
        }

        final Class<?> fieldType = field.getType();
        final Object object = this.get(fieldType, node, values);
        if (object != null) {
            return object;
        }

        final boolean isList = List.class.isAssignableFrom(fieldType);
        if (isList || Set.class.isAssignableFrom(fieldType)) {
            return this.getCollection(field, node, values, isList);
        }
        if (Map.class.isAssignableFrom(fieldType)) {
            return this.getMap(field, node, values);
        }

        return null;
    }

    private Collection<?> getCollection(final Field field, final ConfigurationNode node, final Value values,
                                        final boolean isList) throws SerializationException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        final Type genericType = field.getGenericType();
        if (genericType instanceof final ParameterizedType parameterizedType) {
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof final Class<?> argumentType) {
                final Collection<?> collection;
                if (ConfigSerializable.class.isAssignableFrom(argumentType)) {
                    collection = this.getCustomCollection(argumentType, node, values);
                } else if (String.class.isAssignableFrom(argumentType)) {
                    collection = this.getStringCollection(node, values);
                } else if (World.class.isAssignableFrom(argumentType)) {
                    collection = this.getWorldCollection(node, values);
                } else if (Material.class.isAssignableFrom(argumentType)) {
                    collection = this.getMaterialCollection(node, values);
                } else if (EffectSettings.class.isAssignableFrom(argumentType)) {
                    collection = this.getSettingsCollection(node, values, EffectSettings::fromString);
                } else if (SoundSettings.class.isAssignableFrom(argumentType)) {
                    collection = this.getSettingsCollection(node, values, SoundSettings::fromString);
                } else if (TitleSettings.class.isAssignableFrom(argumentType)) {
                    collection = this.getSettingsCollection(node, values, TitleSettings::fromString);
                } else {
                    collection = node.getList(argumentType);
                }

                if (isList) {
                    return values.immutable() ? ImmutableList.copyOf(collection) : collection;
                }

                return values.immutable() ? ImmutableSet.copyOf(collection) : new HashSet<>(collection);
            }
        }

        throw new IllegalArgumentException("Field is not a parameterized type!");
    }

    private Map<?, ?> getMap(final Field field, final ConfigurationNode node, final Value values) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        final Type genericType = field.getGenericType();
        if (genericType instanceof final ParameterizedType parameterizedType) {
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 1
                    && actualTypeArguments[0] instanceof final Class<?> keyType
                    && actualTypeArguments[1] instanceof final Class<?> valueType) {
                final Map<Object, Object> result = new Object2ObjectOpenHashMap<>();
                for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
                    final Object value = this.get(valueType, entry.getValue(), values);
                    if (value != null) {
                        result.put(entry.getKey(), value);
                    }
                }

                return values.immutable() ? ImmutableMap.copyOf(result) : result;
            }
        }

        throw new IllegalArgumentException("Field is not a parameterized type!");
    }

    @Nullable
    private Object get(final Class<?> clazz, final ConfigurationNode node, final Value values) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (ConfigProcessor.class.isAssignableFrom(clazz)) {
            final Method serializeMethod = clazz.getDeclaredMethod("serializeValue", Object.class);
            serializeMethod.setAccessible(true);
            return clazz.cast(serializeMethod.invoke(null, node.rawScalar()));
        }
        if (String.class.isAssignableFrom(clazz)) {
            final String value = node.getString();
            return values.colorize() ? this.stringColorizer.colorize(value) : value;
        }
        if (Component.class.isAssignableFrom(clazz)) {
            final String value = node.getString();
            return values.colorize() ? this.componentColorizer.colorize(value) : value != null ? Component.text(value) : null;
        }
        if (int.class.isAssignableFrom(clazz)) {
            return node.getInt();
        }
        if (double.class.isAssignableFrom(clazz)) {
            return node.getDouble();
        }
        if (float.class.isAssignableFrom(clazz)) {
            return node.getFloat();
        }
        if (World.class.isAssignableFrom(clazz)) {
            final String worldName = node.getString();
            return worldName != null ? Bukkit.getWorld(worldName) : null;
        }
        if (StringColorizer.class.isAssignableFrom(clazz)) {
            return ColorizerFactory.createStringColorizer(node.getString());
        }
        if (ComponentColorizer.class.isAssignableFrom(clazz)) {
            return ColorizerFactory.createComponentColorizer(node.getString());
        }
        if (EffectSettings.class.isAssignableFrom(clazz)) {
            return EffectSettings.fromString(node.getString());
        }
        if (SoundSettings.class.isAssignableFrom(clazz)) {
            return SoundSettings.fromString(node.getString());
        }
        if (TitleSettings.class.isAssignableFrom(clazz)) {
            return TitleSettings.fromString(node.getString());
        }
        if (Pattern.class.isAssignableFrom(clazz)) {
            return Pattern.compile(node.getString());
        }

        return null;
    }

    private Collection<?> getCustomCollection(final Class<?> clazz, final ConfigurationNode node, final Value values) throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException, SerializationException {
        final Method serializeMethod = clazz.getDeclaredMethod("serializeValue", Object.class);
        serializeMethod.setAccessible(true);

        final List<Object> list = node.getList(Object.class);
        final List<Object> serialized = new ObjectArrayList<>(list.size());

        for (int i = 0; i < list.size(); i++) {
            serialized.add(serializeMethod.invoke(null, list.get(i)));
        }

        return serialized;
    }

    private Collection<String> getStringCollection(final ConfigurationNode node, final Value values) throws
            SerializationException {
        final List<String> list = node.getList(String.class);
        if (values.colorize()) {
            list.replaceAll(this.stringColorizer::colorize);
        }

        return list;
    }

    private Collection<Component> getComponentCollection(final ConfigurationNode node, final Value values) throws
            SerializationException {
        final List<String> list = node.getList(String.class);
        final List<Component> adapted = new ObjectArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (values.colorize()) {
                adapted.add(this.componentColorizer.colorize(list.get(i)));
            } else {
                adapted.add(Component.text(list.get(i)));
            }
        }

        return adapted;
    }

    private Collection<World> getWorldCollection(final ConfigurationNode node, final Value values) throws
            SerializationException {
        final List<World> worlds = new ObjectArrayList<>();
        for (final String string : node.getList(String.class)) {
            final World world = Bukkit.getWorld(string);
            if (world != null) {
                worlds.add(world);
            } else {
                this.warn("World with the name \"" + string + "\" was not found!");
            }
        }

        return worlds;
    }

    private Collection<Material> getMaterialCollection(final ConfigurationNode node, final Value values) throws
            SerializationException {
        final List<Material> materials = new ObjectArrayList<>();
        for (final String string : node.getList(String.class)) {
            final Material material = Material.getMaterial(string.toUpperCase());
            if (material != null) {
                materials.add(material);
            } else {
                this.warn("Material with the name \"" + string + "\" was not found!");
            }
        }

        return materials;
    }

    private <T> Collection<T> getSettingsCollection(final ConfigurationNode node, final Value values,
                                                    final Function<String, T> mapper) throws SerializationException {
        final List<T> settings = new ObjectArrayList<>();
        for (final String string : node.getList(String.class)) {
            settings.add(mapper.apply(string));
        }

        return settings;
    }

    private void warn(final String... messages) {
        if (!this.debug) {
            return;
        }

        for (final String message : messages) {
            this.logger.warn(message);
        }
    }
}
