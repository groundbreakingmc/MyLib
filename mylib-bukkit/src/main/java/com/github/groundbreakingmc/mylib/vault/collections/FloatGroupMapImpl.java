package com.github.groundbreakingmc.mylib.vault.collections;

import com.github.groundbreakingmc.mylib.vault.utils.VaultUtils;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

/**
 * Immutable {@link FloatGroupMap} implementation backed by a fastutil
 * {@link Object2FloatOpenHashMap} for allocation-free primitive access.
 */
final class FloatGroupMapImpl implements FloatGroupMap {

    private final Object2FloatMap<String> groupValues;
    private final float defaultValue;

    FloatGroupMapImpl(Map<String, Float> groupValues, float defaultValue) {
        final Object2FloatOpenHashMap<String> map = new Object2FloatOpenHashMap<>(groupValues);
        map.defaultReturnValue(defaultValue);
        this.groupValues = Object2FloatMaps.unmodifiable(map);
        this.defaultValue = defaultValue;
    }

    @Override
    public float getFloat(Player player) {
        return this.groupValues.getOrDefault(getPrimaryGroup(player), this.defaultValue);
    }

    @Override
    public float getFloatOr(Player player, float fallback) {
        return this.groupValues.getOrDefault(getPrimaryGroup(player), fallback);
    }

    @Override
    public Optional<Float> findFloat(Player player) {
        final String group = getPrimaryGroup(player);
        final float value = this.groupValues.getFloat(group);
        if (value != this.groupValues.defaultReturnValue()) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    @Override
    public float requireFloat(Player player) {
        final String group = getPrimaryGroup(player);
        final float value = this.groupValues.getFloat(group);
        if (value == this.groupValues.defaultReturnValue()) {
            throw new NullPointerException("No value mapped for permission group: \"" + group + "\"");
        }
        return value;
    }

    @Override
    public Float get(Player player) {
        return this.getFloat(player);
    }

    @Override
    public Float getOr(Player player, Float fallback) {
        return this.getFloatOr(player, fallback);
    }

    @Override
    public Optional<Float> find(Player player) {
        return this.findFloat(player);
    }

    @Override
    public Float require(Player player) {
        return this.requireFloat(player);
    }

    private static String getPrimaryGroup(Player player) {
        return VaultUtils.getChatProvider().getPrimaryGroup(player);
    }
}
