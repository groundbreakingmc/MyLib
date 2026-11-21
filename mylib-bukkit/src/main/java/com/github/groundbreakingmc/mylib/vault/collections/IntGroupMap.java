package com.github.groundbreakingmc.mylib.vault.collections;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.OptionalInt;

/**
 * A {@link GroupMap} specialization for primitive {@code int} values.
 *
 * <p>Extends {@code GroupMap<Integer>} while providing unboxed primitive access
 * methods to avoid unnecessary autoboxing overhead. Backed by a fastutil
 * {@code Object2IntMap} for efficient primitive storage.
 *
 * <p>Example usage:
 * <pre>{@code
 * IntGroupMap limits = IntGroupMap.of(
 *     Map.of("vip", 5, "admin", 20),
 *     1 // default
 * );
 *
 * int limit = limits.getInt(player);
 * }</pre>
 */
public interface IntGroupMap extends GroupMap<Integer> {

    /**
     * Returns the {@code int} value mapped to the player's primary group,
     * or the default value specified at construction time.
     *
     * @param player the player whose group is looked up; must not be {@code null}
     * @return the mapped primitive value, or the default
     */
    int getInt(Player player);

    /**
     * Returns the {@code int} value mapped to the player's primary group,
     * or the provided {@code fallback} if no mapping exists.
     *
     * @param player   the player whose group is looked up; must not be {@code null}
     * @param fallback the fallback primitive value
     * @return the mapped primitive value, or {@code fallback}
     */
    int getIntOr(Player player, int fallback);

    /**
     * Returns the {@code int} value mapped to the player's primary group
     * as an {@link OptionalInt}, or {@link OptionalInt#empty()} if no mapping exists.
     *
     * @param player the player whose group is looked up; must not be {@code null}
     * @return an {@code OptionalInt} with the mapped value, or empty
     */
    OptionalInt findInt(Player player);

    /**
     * Returns the {@code int} value mapped to the player's primary group,
     * or throws {@link NullPointerException} if no mapping exists.
     *
     * @param player the player whose group is looked up; must not be {@code null}
     * @return the mapped primitive value
     * @throws NullPointerException if no mapping exists for the player's group
     */
    int requireInt(Player player);

    // --- Factory ---

    /**
     * Creates a new {@code IntGroupMap} backed by a fastutil primitive map.
     *
     * @param groupValues  a map of group names to {@code int} values
     * @param defaultValue the fallback value when no group mapping is found
     * @return a new immutable {@code IntGroupMap}
     */
    static IntGroupMap of(Map<String, Integer> groupValues, int defaultValue) {
        return new IntGroupMapImpl(groupValues, defaultValue);
    }
}
