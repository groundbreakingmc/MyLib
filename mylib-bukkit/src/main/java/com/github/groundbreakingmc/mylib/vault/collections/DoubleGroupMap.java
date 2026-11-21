package com.github.groundbreakingmc.mylib.vault.collections;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.OptionalDouble;

/**
 * A {@link GroupMap} specialization for primitive {@code double} values.
 *
 * <p>Backed by a fastutil {@code Object2DoubleMap} for efficient primitive storage.
 *
 * <p>Example usage:
 * <pre>{@code
 * DoubleGroupMap multipliers = DoubleGroupMap.of(
 *     Map.of("vip", 1.5, "admin", 2.0),
 *     1.0 // default
 * );
 *
 * double multiplier = multipliers.getDouble(player);
 * }</pre>
 */
public interface DoubleGroupMap extends GroupMap<Double> {

    /**
     * Returns the {@code double} value mapped to the player's primary group,
     * or the default value specified at construction time.
     *
     * @param player the player whose group is looked up; must not be {@code null}
     * @return the mapped primitive value, or the default
     */
    double getDouble(Player player);

    /**
     * Returns the {@code double} value mapped to the player's primary group,
     * or the provided {@code fallback} if no mapping exists.
     *
     * @param player   the player whose group is looked up; must not be {@code null}
     * @param fallback the fallback primitive value
     * @return the mapped primitive value, or {@code fallback}
     */
    double getDoubleOr(Player player, double fallback);

    /**
     * Returns the {@code double} value mapped to the player's primary group
     * as an {@link OptionalDouble}, or {@link OptionalDouble#empty()} if no mapping exists.
     *
     * @param player the player whose group is looked up; must not be {@code null}
     * @return an {@code OptionalDouble} with the mapped value, or empty
     */
    OptionalDouble findDouble(Player player);

    /**
     * Returns the {@code double} value mapped to the player's primary group,
     * or throws {@link NullPointerException} if no mapping exists.
     *
     * @param player the player whose group is looked up; must not be {@code null}
     * @return the mapped primitive value
     * @throws NullPointerException if no mapping exists for the player's group
     */
    double requireDouble(Player player);

    // --- Factory ---

    /**
     * Creates a new {@code DoubleGroupMap} backed by a fastutil primitive map.
     *
     * @param groupValues  a map of group names to {@code double} values
     * @param defaultValue the fallback value when no group mapping is found
     * @return a new immutable {@code DoubleGroupMap}
     */
    static DoubleGroupMap of(Map<String, Double> groupValues, double defaultValue) {
        return new DoubleGroupMapImpl(groupValues, defaultValue);
    }
}
