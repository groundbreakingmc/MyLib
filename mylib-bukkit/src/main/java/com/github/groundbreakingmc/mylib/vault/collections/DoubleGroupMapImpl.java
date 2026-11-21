package com.github.groundbreakingmc.mylib.vault.collections;

import com.github.groundbreakingmc.mylib.vault.utils.VaultUtils;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Immutable {@link DoubleGroupMap} implementation backed by a fastutil
 * {@link Object2DoubleOpenHashMap} for allocation-free primitive access.
 */
final class DoubleGroupMapImpl implements DoubleGroupMap {

    private final Object2DoubleMap<String> groupValues;
    private final double defaultValue;

    DoubleGroupMapImpl(Map<String, Double> groupValues, double defaultValue) {
        final Object2DoubleOpenHashMap<String> map = new Object2DoubleOpenHashMap<>(groupValues);
        map.defaultReturnValue(defaultValue);
        this.groupValues = Object2DoubleMaps.unmodifiable(map);
        this.defaultValue = defaultValue;
    }

    @Override
    public double getDouble(Player player) {
        return this.groupValues.getOrDefault(getPrimaryGroup(player), this.defaultValue);
    }

    @Override
    public double getDoubleOr(Player player, double fallback) {
        return this.groupValues.getOrDefault(getPrimaryGroup(player), fallback);
    }

    @Override
    public OptionalDouble findDouble(Player player) {
        final String group = getPrimaryGroup(player);
        final double value = this.groupValues.getDouble(group);
        if (value != this.groupValues.defaultReturnValue()) {
            return OptionalDouble.of(value);
        }
        return OptionalDouble.empty();
    }

    @Override
    public double requireDouble(Player player) {
        final String group = getPrimaryGroup(player);
        final double value = this.groupValues.getDouble(group);
        if (value == this.groupValues.defaultReturnValue()) {
            throw new NullPointerException("No value mapped for permission group: \"" + group + "\"");
        }
        return value;
    }

    @Override
    public Double get(Player player) {
        return this.getDouble(player);
    }

    @Override
    public Double getOr(Player player, Double fallback) {
        return this.getDoubleOr(player, fallback);
    }

    @Override
    public Optional<Double> find(Player player) {
        final String group = getPrimaryGroup(player);
        final double value = this.groupValues.getDouble(group);
        if (value != this.groupValues.defaultReturnValue()) {
            return Optional.of(value);
        }
        return Optional.empty();
    }

    @Override
    public Double require(Player player) {
        return this.requireDouble(player);
    }

    private static String getPrimaryGroup(Player player) {
        return VaultUtils.getChatProvider().getPrimaryGroup(player);
    }
}
