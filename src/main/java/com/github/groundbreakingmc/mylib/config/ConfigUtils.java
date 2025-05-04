package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.legacy.StringColorizer;
import com.github.groundbreakingmc.mylib.utils.player.settings.EffectSettings;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import com.github.groundbreakingmc.mylib.utils.player.settings.TitleSettings;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@UtilityClass
public class ConfigUtils {

    public static List<String> getStringList(ConfigurationNode node, String path) throws SerializationException {
        return node.node(path).getList(String.class);
    }

    public static <T> T getColorizedString(ConfigurationNode node, String path, Colorizer<T> colorizer) {
        return getColorizedString(node, path, null, colorizer);
    }

    public static <T> T getColorizedString(ConfigurationNode node, String path, String defaultValue, Colorizer<T> colorizer) {
        final String string = node.node(path).getString();
        return colorizer.colorize(string == null ? defaultValue : string);
    }

    public static <T> List<T> getColorizedStringList(ConfigurationNode node, String path, Colorizer<T> colorizer) throws SerializationException {
        final List<String> list = getStringList(node, path);
        final List<T> adapted = new ObjectArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            adapted.add(colorizer.colorize(list.get(i)));
        }
        return adapted;
    }

    public static ImmutableList<String> getImmutableColorizedStringList(ConfigurationNode node, String path, StringColorizer colorizer) throws SerializationException {
        return ImmutableList.copyOf(getColorizedStringList(node, path, colorizer));
    }

    public static ImmutableList<String> getImmutableStringList(ConfigurationNode node, String path) throws SerializationException {
        return ImmutableList.copyOf(getStringList(node, path));
    }

    public static ImmutableList<Material> getImmutableMaterialList(ConfigurationNode node, String path) throws SerializationException {
        return ImmutableList.copyOf(getMaterialList(node, path));
    }

    public static List<Material> getMaterialList(ConfigurationNode node, String path) throws SerializationException {
        final List<Material> materials = new ArrayList<>();
        final List<String> list = getStringList(node, path);
        if (list.isEmpty()) {
            return materials;
        }

        for (final String materialName : list) {
            materials.add(Material.getMaterial(materialName));
        }

        return materials;
    }

    public static ImmutableList<World> getImmutableWorldList(ConfigurationNode node, String path) throws SerializationException {
        return ImmutableList.copyOf(getWorldList(node, path));
    }

    public static List<World> getWorldList(ConfigurationNode node, String path) throws SerializationException {
        final List<String> list = getStringList(node, path);
        final List<World> worlds = new ObjectArrayList<>(list.size());
        if (list.isEmpty()) {
            return worlds;
        }

        for (int i = 0; i < list.size(); i++) {
            worlds.add(Bukkit.getWorld(list.get(i)));
        }

        return worlds;
    }

    public static ImmutableList<PotionEffectType> getImmutableEffectList(ConfigurationNode node, String path) throws SerializationException {
        return ImmutableList.copyOf(getEffectList(node, path));
    }

    public static List<PotionEffectType> getEffectList(ConfigurationNode node, String path) throws SerializationException {
        final List<String> list = getStringList(node, path);
        final List<PotionEffectType> effectTypes = new ObjectArrayList<>(list.size());
        if (list.isEmpty()) {
            return effectTypes;
        }

        for (int i = 0; i < list.size(); i++) {
            effectTypes.add(PotionEffectType.getByName(list.get(i)));
        }

        return effectTypes;
    }

    @Nullable
    public static EffectSettings getEffectSettings(ConfigurationNode node, String path) {
        return getEffectSettings(node, path, null);
    }

    @Nullable
    public static EffectSettings getEffectSettings(ConfigurationNode node, String path, EffectSettings defaultValue) {
        final String string = node.node(path).getString();
        EffectSettings value = EffectSettings.fromString(string);
        return value != null ? value : defaultValue;
    }

    @Nullable
    public static SoundSettings getSoundSettings(ConfigurationNode node, String path) {
        return getSoundSettings(node, path, null);
    }

    @Nullable
    public static SoundSettings getSoundSettings(ConfigurationNode node, String path, SoundSettings defaultValue) {
        final String string = node.node(path).getString();
        SoundSettings value = SoundSettings.fromString(string);
        return value != null ? value : defaultValue;
    }

    @Nullable
    public static TitleSettings getTitleSettings(ConfigurationNode node, String path) {
        return getTitleSettings(node, path, null);
    }

    @Nullable
    public static TitleSettings getTitleSettings(ConfigurationNode node, String path, StringColorizer colorizer) {
        return getTitleSettings(node, path, colorizer, null);
    }

    @Nullable
    public static TitleSettings getTitleSettings(ConfigurationNode node, String path, StringColorizer colorizer, TitleSettings defaultValue) {
        final String string = node.node(path).getString();
        if (string == null) {
            return defaultValue;
        }

        return TitleSettings.fromString(colorizer != null ? colorizer.colorize(string) : string);
    }
}
