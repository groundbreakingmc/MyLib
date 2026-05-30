package com.github.groundbreakingmc.mylib.server;

/**
 * Provides information about the current server implementation.
 *
 * <p>Server type is detected once and cached. Detection walks from the most specific
 * fork down to the most generic platform, so e.g. a Leaf server will never be
 * misidentified as plain Paper.
 *
 * @author GroundbreakingMC
 * @since 1.0.0
 */
public final class ServerInfo {

    private static volatile ServerType SERVER_TYPE;

    private ServerInfo() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Returns the detected server type, detecting and caching it on the first call.
     *
     * @return detected {@link ServerType}; never {@code null}
     * @since 1.0.0
     */
    public static ServerType serverType() {
        if (SERVER_TYPE == null) {
            synchronized (ServerInfo.class) {
                if (SERVER_TYPE == null) {
                    SERVER_TYPE = detect();
                }
            }
        }
        return SERVER_TYPE;
    }

    /**
     * Checks whether the server is Paper or any of its forks (Purpur, Folia, Gale, Leaf, …).
     *
     * @return {@code true} if the server is Paper-based
     * @since 1.0.0
     */
    public static boolean isPaperOrFork() {
        return serverType().isPaperBased();
    }

    /**
     * Checks whether the server is a proxy (Velocity, BungeeCord, Waterfall, NullCordX).
     *
     * @return {@code true} if the server is a proxy
     * @since 1.0.0
     */
    public static boolean isProxy() {
        return serverType().isProxy();
    }

    // ── Detection ─────────────────────────────────────────────────────────────

    private static ServerType detect() {
        // ── Proxy servers ──────────────────────────────────────────────────────
        if (classExists("com.velocitypowered.api.proxy.ProxyServer")) return ServerType.VELOCITY;

        // ── Bungee servers ─────────────────────────────────────────────────────
        if (classExists("net.shieldcommunity.nullcordx.NullCordXLoader")) return ServerType.NULLCORDX;
        if (classExists("io.github.waterfallmc.waterfall.conf.WaterfallConfiguration")) return ServerType.WATERFALL;
        if (classExists("net.md_5.bungee.api.ProxyServer")) return ServerType.BUNGEECORD;

        // ── Non-Bukkit servers ─────────────────────────────────────────────────
        if (classExists("org.spongepowered.api.Sponge")) return ServerType.SPONGE;

        // ── Bukkit servers ─────────────────────────────────────────────────────
        if (classExists("org.dreeam.leaf.Leaf")) return ServerType.LEAF;
        if (classExists("org.galemc.gale.configuration.GaleGlobalConfiguration")) return ServerType.GALE;
        if (classExists("io.papermc.paper.threadedregions.RegionizedServer")) return ServerType.FOLIA;
        if (classExists("org.purpurmc.purpur.PurpurConfig")) return ServerType.PURPUR;
        if (classExists("gg.pufferfish.pufferfish.PufferfishConfig")) return ServerType.PUFFERFISH;
        if (classExists("io.papermc.paper.configuration.Configuration")) return ServerType.PAPER;
        if (classExists("org.spigotmc.SpigotConfig")) return ServerType.SPIGOT;
        if (classExists("org.bukkit.Bukkit")) return ServerType.BUKKIT;

        return ServerType.UNKNOWN;
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
