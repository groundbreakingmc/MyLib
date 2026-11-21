package com.github.groundbreakingmc.mylib.vault.collections;

import com.github.groundbreakingmc.mylib.vault.utils.VaultUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Immutable {@link IntGroupMap} implementation backed by a fastutil
 * {@link Object2IntOpenHashMap} for allocation-free primitive access.
 */
final class IntGroupMapImpl implements IntGroupMap {

    private final Object2IntMap<String> groupValues;
    private final int defaultValue;

    IntGroupMapImpl(Map<String, Integer> groupValues, int defaultValue) {
        final Object2IntOpenHashMap<String> map = new Object2IntOpenHashMap<>(groupValues);
        map.defaultReturnValue(defaultValue);
        this.groupValues = Object2IntMaps.unmodifiable(map);
        this.defaultValue = defaultValue;
    }

    @Override
    public int getInt(Player player) {
        return this.groupValues.getOrDefault(getPrimaryGroup(player), this.defaultValue);
    }

    @Override
    public int getIntOr(Player player, int fallback) {
        return this.groupValues.getOrDefault(getPrimaryGroup(player), fallback);
    }

    @Override
    public OptionalInt findInt(Player player) {
        final String group = getPrimaryGroup(player);
        final int value = this.groupValues.getInt(group);
        if (value != this.groupValues.defaultReturnValue()) {
            return OptionalInt.of(value);
        }
        return OptionalInt.empty();
    }

    @Override
    public int requireInt(Player player) {
        final String group = getPrimaryGroup(player);
        final int value = this.groupValues.getInt(group);
        if (value == this.groupValues.defaultReturnValue()) {
            throw new NullPointerException("No value mapped for permission group: \"" + group + "\"");
        }
        return value;
    }

    @Override
    public Integer get(Player player) {
        return this.getInt(player);
    }

    @Override
    public Integer getOr(Player player, Integer fallback) {
        return this.getIntOr(player, fallback);
    }

    @Override
    public Optional<Integer> find(Player player) {
        final String group = getPrimaryGroup(player);
        final int value = this.groupValues.getInt(group);
        if (value != this.groupValues.defaultReturnValue()) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    @Override
    public Integer require(Player player) {
        return this.requireInt(player);
    }

    private static String getPrimaryGroup(Player player) {
        return VaultUtils.getChatProvider().getPrimaryGroup(player);
    }
}
