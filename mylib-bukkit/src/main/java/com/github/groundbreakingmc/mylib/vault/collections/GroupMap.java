package com.github.groundbreakingmc.mylib.vault.collections;

import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * A mapping of Vault permission groups to values of type {@code T}.
 * Resolves a value for a given {@link Player} based on their primary group.
 *
 * @param <T> the type of values associated with permission groups
 */
public interface GroupMap<T> {

    /**
     * Returns the value mapped to the player's primary group,
     * or the default value specified at construction time if no mapping exists.
     *
     * @param player the player whose group is looked up; must not be {@code null}
     * @return the mapped value, or the default if no mapping was found
     */
    T get(Player player);

    /**
     * Returns the value mapped to the player's primary group,
     * or the provided {@code fallback} if no mapping exists.
     *
     * <p>This method ignores the default value set at construction time.
     *
     * @param player   the player whose group is looked up; must not be {@code null}
     * @param fallback the value to return if no mapping is found; may be {@code null}
     * @return the mapped value, or {@code fallback} if no mapping was found
     */
    T getOr(Player player, T fallback);

    /**
     * Returns the value mapped to the player's primary group as an {@link Optional}.
     * Returns {@link Optional#empty()} if no mapping exists, ignoring the default value.
     *
     * @param player the player whose group is looked up; must not be {@code null}
     * @return an {@code Optional} containing the mapped value, or empty if not found
     */
    Optional<T> find(Player player);

    /**
     * Returns the value mapped to the player's primary group,
     * or throws {@link NullPointerException} if no mapping exists.
     *
     * <p>Use this when a missing mapping indicates a misconfiguration
     * that should fail loudly rather than silently fall back.
     *
     * @param player the player whose group is looked up; must not be {@code null}
     * @return the mapped value; never {@code null}
     * @throws NullPointerException if no mapping exists for the player's group
     */
    T require(Player player);
}
