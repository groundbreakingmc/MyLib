package com.github.groundbreakingmc.mylib.server.version;

import org.bukkit.Bukkit;

/**
 * Static utility for querying the current server version.
 *
 * <p>Detection runs once on the first call to {@link #serverVersion()} and is cached.
 * Two strategies are tried in order:
 * <ol>
 *   <li>NMS package name — most reliable, works on all CraftBukkit-based servers.</li>
 *   <li>Bukkit version string — fallback for unusual environments.</li>
 * </ol>
 *
 * @author GroundbreakingMC
 * @since 1.0.0
 */
public final class ServerVersionUtils {

    private static volatile ServerVersion SERVER_VERSION;

    private ServerVersionUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ── Primary API ───────────────────────────────────────────────────────────

    /**
     * Returns the detected server version, detecting and caching it on the first call.
     *
     * @return detected {@link ServerVersion}; never {@code null}
     * @since 1.0.0
     */
    public static ServerVersion serverVersion() {
        if (SERVER_VERSION == null) {
            synchronized (ServerVersionUtils.class) {
                if (SERVER_VERSION == null) {
                    SERVER_VERSION = detect();
                }
            }
        }
        return SERVER_VERSION;
    }

    /**
     * Returns the server version as a human-readable string (e.g. {@code "1.20.4"}).
     *
     * @since 1.0.0
     */
    public static String versionString() {
        return serverVersion().versionString();
    }

    /**
     * Returns the NMS version string (e.g. {@code "v1_20_R3"}).
     * Primarily useful for pre-1.17 package path construction.
     *
     * @since 1.0.0
     */
    public static String nmsVersionString() {
        return serverVersion().nmsVersion();
    }

    // ── Version comparisons ───────────────────────────────────────────────────

    /**
     * @since 1.0.0
     */
    public static boolean isHigher(ServerVersion version) {
        return serverVersion().isHigher(version);
    }

    /**
     * @since 1.0.0
     */
    public static boolean isLower(ServerVersion version) {
        return serverVersion().isLower(version);
    }

    /**
     * @since 1.0.0
     */
    public static boolean isHigherOrEqual(ServerVersion version) {
        return serverVersion().isHigherOrEqual(version);
    }

    /**
     * @since 1.0.0
     */
    public static boolean isLowerOrEqual(ServerVersion version) {
        return serverVersion().isLowerOrEqual(version);
    }

    /**
     * @since 1.0.0
     */
    public static boolean isEqual(ServerVersion version) {
        return serverVersion() == version;
    }

    /**
     * @since 1.0.0
     */
    public static boolean isInRange(ServerVersion min, ServerVersion max) {
        return serverVersion().isBetween(min, max);
    }

    /**
     * @since 1.0.0
     */
    public static boolean isModern() {
        return serverVersion().isModern();
    }

    /**
     * @since 1.0.0
     */
    public static boolean isLegacy() {
        return serverVersion().isLegacy();
    }

    // ── Semantic feature flags ────────────────────────────────────────────────

    /**
     * Returns {@code true} if the server uses the modern command system (1.13+).
     *
     * @since 1.0.0
     */
    public static boolean hasNewCommandSystem() {
        return isHigherOrEqual(ServerVersion.V1_13_R1);
    }

    /**
     * Returns {@code true} if the server supports Paper's Adventure component API (1.16+).
     *
     * @since 1.0.0
     */
    public static boolean supportsPaperComponents() {
        return isHigherOrEqual(ServerVersion.V1_16_R1);
    }

    /**
     * Returns {@code true} if the server uses the new world generation system (1.18+).
     *
     * @since 1.0.0
     */
    public static boolean hasNewWorldGeneration() {
        return isHigherOrEqual(ServerVersion.V1_18_R1);
    }

    // ── Testing support ───────────────────────────────────────────────────────

    /**
     * Clears the cached version, forcing re-detection on the next call.
     * Intended for testing only.
     *
     * @since 1.0.0
     */
    public static void forceRedetection() {
        SERVER_VERSION = null;
    }

    // ── Detection ─────────────────────────────────────────────────────────────

    private static ServerVersion detect() {
        final ServerVersion fromNms = detectFromNmsPackage();
        if (fromNms != ServerVersion.UNKNOWN) {
            return fromNms;
        }
        return detectFromBukkitVersion();
    }

    /**
     * Extracts the NMS version string from the CraftBukkit package name
     * (e.g. {@code org.bukkit.craftbukkit.v1_20_R3}) and maps it to a
     * {@link ServerVersion}. Works on Spigot/CraftBukkit for all versions;
     * on Paper 1.20.5+ the package no longer contains a version suffix,
     * so this method falls through to the Bukkit-version fallback.
     */
    private static ServerVersion detectFromNmsPackage() {
        try {
            final String pkg = Bukkit.getServer().getClass().getPackage().getName();
            // org.bukkit.craftbukkit.v1_20_R3 → segment [3] = "v1_20_R3"
            if (pkg.contains("org.bukkit.craftbukkit.v")) {
                final String nmsVersion = pkg.split("\\.")[3];
                // enum constant names use uppercase V: V1_20_R3
                return ServerVersion.valueOf(nmsVersion.toUpperCase());
            }
        } catch (IllegalArgumentException ignored) {
            // NMS package found but version not in enum → fall through
        } catch (Throwable ignored) {
            // Bukkit not available or unexpected structure → fall through
        }
        return ServerVersion.UNKNOWN;
    }

    /**
     * Maps the Bukkit version string (e.g. {@code "1.20.4-R0.1-SNAPSHOT"}) to a
     * {@link ServerVersion}. Used as a fallback when the NMS package does not
     * contain a version suffix (Paper 1.20.5+) or when package detection fails.
     */
    private static ServerVersion detectFromBukkitVersion() {
        try {
            final String raw = Bukkit.getBukkitVersion();
            // Strip "-R0.1-SNAPSHOT" suffix
            final int dash = raw.indexOf('-');
            final String version = dash > 0 ? raw.substring(0, dash) : raw;

            return switch (version) {
                case "1.8" -> ServerVersion.V1_8_R1;
                case "1.8.3" -> ServerVersion.V1_8_R2;
                case "1.8.8" -> ServerVersion.V1_8_R3;
                case "1.9", "1.9.2" -> ServerVersion.V1_9_R1;
                case "1.9.4" -> ServerVersion.V1_9_R2;
                case "1.10.2" -> ServerVersion.V1_10_R1;
                case "1.11", "1.11.2" -> ServerVersion.V1_11_R1;
                case "1.12", "1.12.1", "1.12.2" -> ServerVersion.V1_12_R1;
                case "1.13", "1.13.1" -> ServerVersion.V1_13_R1;
                case "1.13.2" -> ServerVersion.V1_13_R2;
                case "1.14", "1.14.1", "1.14.2", "1.14.3", "1.14.4" -> ServerVersion.V1_14_R1;
                case "1.15", "1.15.1", "1.15.2" -> ServerVersion.V1_15_R1;
                case "1.16", "1.16.1" -> ServerVersion.V1_16_R1;
                case "1.16.2", "1.16.3" -> ServerVersion.V1_16_R2;
                case "1.16.4", "1.16.5" -> ServerVersion.V1_16_R3;
                case "1.17", "1.17.1" -> ServerVersion.V1_17_R1;
                case "1.18", "1.18.1" -> ServerVersion.V1_18_R1;
                case "1.18.2" -> ServerVersion.V1_18_R2;
                case "1.19", "1.19.1", "1.19.2" -> ServerVersion.V1_19_R1;
                case "1.19.3" -> ServerVersion.V1_19_R2;
                case "1.19.4" -> ServerVersion.V1_19_R3;
                case "1.20", "1.20.1" -> ServerVersion.V1_20_R1;
                case "1.20.2" -> ServerVersion.V1_20_R2;
                case "1.20.3", "1.20.4" -> ServerVersion.V1_20_R3;
                case "1.20.5", "1.20.6" -> ServerVersion.V1_20_R4;
                case "1.21", "1.21.1" -> ServerVersion.V1_21_R1;
                case "1.21.2", "1.21.3" -> ServerVersion.V1_21_R2;
                case "1.21.4" -> ServerVersion.V1_21_R3;
                case "1.21.5" -> ServerVersion.V1_21_R4;
                case "1.21.6", "1.21.7", "1.21.8" -> ServerVersion.V1_21_R5;
                case "1.21.9", "1.21.10", "1.21.11" -> ServerVersion.V1_21_R6;
                case "26.1" -> ServerVersion.V26_R1;
                case "26.2" -> ServerVersion.V26_R2;
                default -> ServerVersion.UNKNOWN;
            };
        } catch (Exception ignored) {
            return ServerVersion.UNKNOWN;
        }
    }
}
