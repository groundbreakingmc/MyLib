package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.utils.player.settings.EffectSettings;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import com.github.groundbreakingmc.mylib.utils.player.settings.TitleSettings;
import com.google.common.collect.ImmutableList;
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

    public static List<String> getStringList(final ConfigurationNode node, final String path) throws SerializationException {
        return node.node(path).getList(String.class);
    }

    public static String getColorizedString(final ConfigurationNode node, final String path, final Colorizer colorizer) {
        return getColorizedString(node, path, null, colorizer);
    }

    public static String getColorizedString(final ConfigurationNode node, final String path, final String defaultValue, final Colorizer colorizer) {
        final String string = node.node(path).getString();
        return colorizer.colorize(string == null ? defaultValue : string);
    }

    public static List<String> getColorizedStringList(final ConfigurationNode node, final String path, final Colorizer colorizer) throws SerializationException {
        final List<String> list = getStringList(node, path);
        list.replaceAll(colorizer::colorize);
        return list;
    }

    public static ImmutableList<String> getImmutableColorizedStringList(final ConfigurationNode node, final String path, final Colorizer colorizer) throws SerializationException {
        return ImmutableList.copyOf(getColorizedStringList(node, path, colorizer));
    }

    public static ImmutableList<String> getImmutableStringList(final ConfigurationNode node, final String path) throws SerializationException {
        return ImmutableList.copyOf(getStringList(node, path));
    }

    public static ImmutableList<Material> getImmutableMaterialList(final ConfigurationNode node, final String path) throws SerializationException {
        return ImmutableList.copyOf(getMaterialList(node, path));
    }

    public static List<Material> getMaterialList(final ConfigurationNode node, final String path) throws SerializationException {
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

    public static ImmutableList<World> getImmutableWorldList(final ConfigurationNode node, final String path) throws SerializationException {
        return ImmutableList.copyOf(getWorldList(node, path));
    }

    public static List<World> getWorldList(final ConfigurationNode node, final String path) throws SerializationException {
        final List<World> worlds = new ArrayList<>();
        final List<String> list = getStringList(node, path);
        if (list.isEmpty()) {
            return worlds;
        }

        for (final String worldName : list) {
            worlds.add(Bukkit.getWorld(worldName));
        }

        return worlds;
    }

    public static ImmutableList<PotionEffectType> getImmutableEffectList(final ConfigurationNode node, final String path) throws SerializationException {
        return ImmutableList.copyOf(getEffectList(node, path));
    }

    public static List<PotionEffectType> getEffectList(final ConfigurationNode node, final String path) throws SerializationException {
        final List<PotionEffectType> effectTypes = new ArrayList<>();
        final List<String> list = getStringList(node, path);
        if (list.isEmpty()) {
            return effectTypes;
        }

        for (final String effectName : list) {
            effectTypes.add(PotionEffectType.getByName(effectName));
        }

        return effectTypes;
    }

    @Nullable
    public static EffectSettings getEffectSettings(final ConfigurationNode node, final String path) {
        return getEffectSettings(node, path, null);
    }

    @Nullable
    public static EffectSettings getEffectSettings(final ConfigurationNode node, final String path, final EffectSettings defaultValue) {
        final String string = node.node(path).getString();
        if (string == null) {
            return defaultValue;
        }

        return EffectSettings.get(string);
    }

    @Nullable
    public static SoundSettings getSoundSettings(final ConfigurationNode node, final String path) {
        return getSoundSettings(node, path, null);
    }

    @Nullable
    public static SoundSettings getSoundSettings(final ConfigurationNode node, final String path, final SoundSettings defaultValue) {
        final String string = node.node(path).getString();
        if (string == null) {
            return defaultValue;
        }

        return SoundSettings.get(string);
    }

    @Nullable
    public static TitleSettings getTitleSettings(final ConfigurationNode node, final String path) {
        return getTitleSettings(node, path, null);
    }

    @Nullable
    public static TitleSettings getTitleSettings(final ConfigurationNode node, final String path, final TitleSettings defaultValue) {
        final String string = node.node(path).getString();
        if (string == null) {
            return defaultValue;
        }

        return TitleSettings.get(string);
    }
}
