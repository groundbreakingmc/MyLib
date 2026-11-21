package com.github.groundbreakingmc.mylib.vault.collections;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

/**
 * A {@link GroupMap} specialization for primitive {@code float} values.
 *
 * <p>Backed by a fastutil {@code Object2FloatMap} for efficient primitive storage.
 *
 * <p>Note: the JDK does not provide {@code OptionalFloat}, so {@link #findFloat(Player)}
 * returns {@code Optional<Float>}. If zero-allocation is critical in that path,
 * prefer {@link #getFloat(Player)} with a sentinel value instead.
 *
 * <p>Example usage:
 * <pre>{@code
 * FloatGroupMap speeds = FloatGroupMap.of(
 *     Map.of("vip", 0.3f, "admin", 0.5f),
 *     0.2f // default
 * );
 *
 * float speed = speeds.getFloat(player);
 * }</pre>
 */
public interface FloatGroupMap extends GroupMap<Float> {

    /**
     * Returns the {@code float} value mapped to the player's primary group,
     * or the default value specified at construction time.
     *
     * @param player the player whose group is looked up; must not be {@code null}
     * @return the mapped primitive value, or the default
     */
    float getFloat(Player player);

    /**
     * Returns the {@code float} value mapped to the player's primary group,
     * or the provided {@code fallback} if no mapping exists.
     *
     * @param player   the player whose group is looked up; must not be {@code null}
     * @param fallback the fallback primitive value
     * @return the mapped primitive value, or {@code fallback}
     */
    float getFloatOr(Player player, float fallback);

    /**
     * Returns the {@code float} value mapped to the player's primary group
     * as an {@link Optional}, or {@link Optional#empty()} if no mapping exists.
     *
     * <p>The JDK does not provide {@code OptionalFloat}; boxing is unavoidable here.
     * For hot paths, prefer {@link #getFloat(Player)} or {@link #getFloatOr(Player, float)}.
     *
     * @param player the player whose group is looked up; must not be {@code null}
     * @return an {@code Optional} containing the mapped value, or empty
     */
    Optional<Float> findFloat(Player player);

    /**
     * Returns the {@code float} value mapped to the player's primary group,
     * or throws {@link NullPointerException} if no mapping exists.
     *
     * @param player the player whose group is looked up; must not be {@code null}
     * @return the mapped primitive value
     * @throws NullPointerException if no mapping exists for the player's group
     */
    float requireFloat(Player player);

    // --- Factory ---

    /**
     * Creates a new {@code FloatGroupMap} backed by a fastutil primitive map.
     *
     * @param groupValues  a map of group names to {@code float} values
     * @param defaultValue the fallback value when no group mapping is found
     * @return a new immutable {@code FloatGroupMap}
     */
    static FloatGroupMap of(Map<String, Float> groupValues, float defaultValue) {
        return new FloatGroupMapImpl(groupValues, defaultValue);
    }
}
