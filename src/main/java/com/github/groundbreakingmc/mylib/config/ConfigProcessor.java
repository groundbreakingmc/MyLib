package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.ColorizerFactory;
import com.github.groundbreakingmc.mylib.config.annotations.Config;
import com.github.groundbreakingmc.mylib.config.annotations.Section;
import com.github.groundbreakingmc.mylib.config.annotations.Value;
import com.github.groundbreakingmc.mylib.logger.Logger;
import com.github.groundbreakingmc.mylib.utils.player.settings.EffectSettings;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import com.github.groundbreakingmc.mylib.utils.player.settings.TitleSettings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public abstract class ConfigProcessor {

    protected final Plugin plugin;
    protected final Logger logger;
    protected final boolean debug;
    @Getter
    private Colorizer colorizer;
    protected final Map<String, Field> sections;

    protected ConfigProcessor(final Plugin plugin, final Logger logger, final boolean debug) {
        this(plugin, logger, debug, null);
    }

    protected ConfigProcessor(final Plugin plugin, final Logger logger, final boolean debug, final Colorizer colorizer) {
        this.plugin = plugin;
        this.logger = logger;
        this.debug = debug;
        this.colorizer = colorizer;
        this.sections = new HashMap<>();
    }

    public final void setupValues() throws IllegalAccessException {
        final Class<?> clazz = this.getClass();
        if (!clazz.isAnnotationPresent(Config.class)) {
            throw new UnsupportedOperationException("Class is not annotated with required annotation!");
        }

        final Config configAnnotation = clazz.getAnnotation(Config.class);
        final ConfigurationNode config = this.getConfig(configAnnotation.fileName(), configAnnotation.version(), configAnnotation.versionPath());

        if (!configAnnotation.colorizerPath().isEmpty()) {
            this.colorizer = ColorizerFactory.createColorizer(config.node(configAnnotation.colorizerPath().split("\\.")).getString());
        }

        this.setupFields(this, config);
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
    private Object getValue(final Field field, final ConfigurationNode node, final Value values) throws SerializationException {
        final ConfigurationNode valueNode = node.node(values.path().split("\\."));
        if (valueNode.virtual()) {
            return null;
        }

        final Class<?> fieldType = field.getType();
        if (String.class.isAssignableFrom(fieldType)) {
            final String value = valueNode.getString();
            return values.colorize() ? this.colorizer.colorize(value) : value;
        }
        if (int.class.isAssignableFrom(fieldType)) {
            return valueNode.getInt();
        }
        if (double.class.isAssignableFrom(fieldType)) {
            return valueNode.getDouble();
        }
        if (float.class.isAssignableFrom(fieldType)) {
            return valueNode.getFloat();
        }
        final boolean isList = List.class.isAssignableFrom(fieldType);
        if (isList || Set.class.isAssignableFrom(fieldType)) {
            return this.getCollection(field, node, values, isList);
        }
        if (World.class.isAssignableFrom(fieldType)) {
            final String worldName = valueNode.getString();
            return worldName != null ? Bukkit.getWorld(worldName) : null;
        }
        if (Colorizer.class.isAssignableFrom(fieldType)) {
            return ColorizerFactory.createColorizer(valueNode.getString());
        }
        if (EffectSettings.class.isAssignableFrom(fieldType)) {
            return EffectSettings.get(valueNode.getString());
        }
        if (SoundSettings.class.isAssignableFrom(fieldType)) {
            return SoundSettings.get(valueNode.getString());
        }
        if (TitleSettings.class.isAssignableFrom(fieldType)) {
            return TitleSettings.get(valueNode.getString());
        }
        if (Pattern.class.isAssignableFrom(fieldType)) {
            return Pattern.compile(valueNode.getString());
        }
        if (Map.class.isAssignableFrom(fieldType)) {
            this.logger.warn("Maps are not supported yet!");
            return null;
        }

        return null;
    }

    private Collection<?> getCollection(final Field field, final ConfigurationNode node, final Value values,
                                        final boolean isList) throws SerializationException {
        final Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) genericType;
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class<?>) {
                final Class<?> argumentType = (Class<?>) actualTypeArguments[0];
                final Collection<?> collection;
                if (String.class.isAssignableFrom(argumentType)) {
                    collection = this.getStringCollection(node, values);
                } else if (World.class.isAssignableFrom(argumentType)) {
                    collection = this.getWorldCollection(node, values);
                } else if (Material.class.isAssignableFrom(argumentType)) {
                    collection = this.getMaterialCollection(node, values);
                } else if (EffectSettings.class.isAssignableFrom(argumentType)) {
                    collection = this.getSettingsCollection(node, values, EffectSettings::get);
                } else if (SoundSettings.class.isAssignableFrom(argumentType)) {
                    collection = this.getSettingsCollection(node, values, SoundSettings::get);
                } else if (TitleSettings.class.isAssignableFrom(argumentType)) {
                    collection = this.getSettingsCollection(node, values, TitleSettings::get);
                } else {
                    collection = node.node(values.path()).getList(argumentType);
                }

                if (isList) {
                    return values.immutable() ? ImmutableList.copyOf(collection) : collection;
                }

                return values.immutable() ? ImmutableSet.copyOf(collection) : new HashSet<>(collection);
            }
        }

        throw new IllegalArgumentException("Field is not a parameterized type!");
    }

    private Collection<String> getStringCollection(final ConfigurationNode node, final Value values) throws
            SerializationException {
        final List<String> list = node.node(values.path()).getList(String.class);
        if (values.colorize()) {
            list.replaceAll(this.colorizer::colorize);
        }

        return list;
    }

    private Collection<World> getWorldCollection(final ConfigurationNode node, final Value values) throws
            SerializationException {
        final List<World> worlds = new ArrayList<>();
        for (final String string : node.node(values.path()).getList(String.class)) {
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
        final List<Material> materials = new ArrayList<>();
        for (final String string : node.node(values.path()).getList(String.class)) {
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
        final List<T> settings = new ArrayList<>();
        for (final String string : node.node(values.path()).getList(String.class)) {
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
