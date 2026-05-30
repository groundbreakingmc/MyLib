package com.github.groundbreakingmc.mylib.server;

/**
 * Enum representing known Minecraft server implementations.
 *
 * <p>Each entry carries a {@link #forkParent} reference that encodes the fork hierarchy,
 * allowing callers to check ancestry via {@link #isBasedOn(ServerType)} instead of
 * maintaining separate boolean flags per platform.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Check if running on Paper or any of its forks (Purpur, Folia, Leaf, …)
 * ServerInfo.serverType().isBasedOn(ServerType.PAPER);
 *
 * // Check if running on any BungeeCord-based proxy
 * ServerInfo.serverType().isBasedOn(ServerType.BUNGEECORD);
 * }</pre>
 *
 * @author GroundbreakingMC
 * @since 1.0.0
 */
public enum ServerType {

    // ── Unknown / fallback ────────────────────────────────────────────────────
    UNKNOWN(null),

    // ── Non-Bukkit platforms ──────────────────────────────────────────────────
    SPONGE(null),

    // ── Proxy: Velocity (standalone, no BungeeCord relation) ─────────────────
    VELOCITY(null),

    // ── Proxy: BungeeCord and its forks ──────────────────────────────────────
    BUNGEECORD(null),
    WATERFALL(BUNGEECORD),
    NULLCORDX(BUNGEECORD),

    // ── Bukkit-based servers ──────────────────────────────────────────────────
    BUKKIT(null),
    SPIGOT(BUKKIT),
    PAPER(SPIGOT),
    PUFFERFISH(PAPER),
    PURPUR(PAPER),
    FOLIA(PAPER),
    GALE(PAPER),
    LEAF(GALE);

    private final ServerType forkParent;

    ServerType(ServerType forkParent) {
        this.forkParent = forkParent;
    }

    /**
     * Returns the direct fork parent of this server type,
     * or {@code null} if this is a root platform (Bukkit, BungeeCord, Velocity, etc.).
     *
     * @return parent server type, or {@code null}
     * @since 1.0.0
     */
    public ServerType forkParent() {
        return this.forkParent;
    }

    /**
     * Checks whether this server type is equal to or is a (transitive) fork of {@code type}.
     *
     * <p>Examples:
     * <pre>{@code
     * LEAF.isBasedOn(GALE)   // true  — direct parent
     * LEAF.isBasedOn(PAPER)  // true  — grandparent
     * LEAF.isBasedOn(SPIGOT) // true  — great-grandparent
     * LEAF.isBasedOn(PURPUR) // false — unrelated fork
     * }</pre>
     *
     * @param type the ancestor type to check against
     * @return {@code true} if this type equals {@code type} or any ancestor in the fork chain equals {@code type}
     * @since 1.0.0
     */
    public boolean isBasedOn(ServerType type) {
        ServerType current = this;
        while (current != null) {
            if (current == type) {
                return true;
            }
            current = current.forkParent;
        }
        return false;
    }

    /**
     * Checks whether this is a proxy server (Velocity, BungeeCord, or any BungeeCord fork).
     *
     * @return {@code true} if this is a proxy-based server type
     * @since 1.0.0
     */
    public boolean isProxy() {
        return this == VELOCITY || this.isBasedOn(BUNGEECORD);
    }

    /**
     * Checks whether this is a Bukkit-based server.
     *
     * @return {@code true} if this is Bukkit or any of its forks
     * @since 1.0.0
     */
    public boolean isBukkitBased() {
        return this.isBasedOn(BUKKIT);
    }

    /**
     * Checks whether this is a Paper-based server.
     *
     * @return {@code true} if this is Paper or any of its forks
     * @since 1.0.0
     */
    public boolean isPaperBased() {
        return this.isBasedOn(PAPER);
    }
}
