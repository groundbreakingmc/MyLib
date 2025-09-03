package com.github.groundbreakingmc.mylib.utils.server.version;

import org.bukkit.Bukkit;

/**
 * Utility class for Minecraft server version handling.
 *
 * @author GroundbreakingMC
 * @version 1.0
 * @since 1.0.0
 */
public final class ServerVersionUtils {

    // Cached server version
    private static volatile ServerVersion SERVER_VERSION;
    private static volatile String DETECTED_NMS_VERSION;

    // Private constructor for utility class
    private ServerVersionUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Gets the server version with caching for maximum performance.
     * First call detects version, subsequent calls return cached result.
     *
     * @return the server version enum
     * @since 1.0.0
     */
    public static ServerVersion serverVersion() {
        if (SERVER_VERSION == null) {
            synchronized (ServerVersionUtils.class) {
                if (SERVER_VERSION == null) {
                    SERVER_VERSION = detectServerVersion();
                }
            }
        }
        return SERVER_VERSION;
    }

    /**
     * Detects server version using multiple methods for maximum reliability:
     * 1. NMS package detection (most reliable)
     * 2. Bukkit version fallback
     *
     * @return detected server version
     * @since 1.0.0
     */
    private static ServerVersion detectServerVersion() {
        // Method 1: Try NMS package detection (most reliable)
        String nmsVersion = detectNmsVersion();
        if (nmsVersion != null) {
            DETECTED_NMS_VERSION = nmsVersion;
            ServerVersion version = versionFromNms(nmsVersion);
            if (version != ServerVersion.UNKNOWN) {
                return version;
            }
        }

        // Method 2: Fallback to Bukkit version parsing
        return detectFromBukkitVersion();
    }

    /**
     * Detects NMS version from CraftBukkit package structure.
     * Looks for org.bukkit.craftbukkit.v1_XX_RX pattern.
     *
     * @return NMS version string or null if not found
     * @since 1.0.0
     */
    private static String detectNmsVersion() {
        try {
            // Get CraftServer class which always exists in CraftBukkit
            String craftServerClass = Bukkit.getServer().getClass().getPackage().getName();
            // Extract version from package: org.bukkit.craftbukkit.v1_20_R3.CraftServer
            if (craftServerClass.contains("org.bukkit.craftbukkit.v")) {
                return craftServerClass.split("\\.")[3];
            }
        } catch (Throwable ignored) {
            // Fallback to other methods
        }

        return null;
    }

    /**
     * Maps NMS version string to Version enum.
     *
     * @param nmsVersion the NMS version string
     * @return corresponding Version enum or UNKNOWN
     * @since 1.0.0
     */
    private static ServerVersion versionFromNms(String nmsVersion) {
        try {
            return ServerVersion.valueOf(nmsVersion);
        } catch (IllegalArgumentException ignored) {
            return ServerVersion.UNKNOWN;
        }
    }

    /**
     * Fallback method: detects version from Bukkit version string.
     *
     * @return detected version or UNKNOWN
     * @since 1.0.0
     */
    private static ServerVersion detectFromBukkitVersion() {
        try {
            String bukkitVersion = Bukkit.getBukkitVersion();
            String version = bukkitVersion;
            int dashIndex = bukkitVersion.indexOf('-');
            if (dashIndex > 0) {
                version = bukkitVersion.substring(0, dashIndex);
            }

            // Map common Bukkit versions to our enum
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
                case "1.21.5" -> ServerVersion.V1_21_R3;
                case "1.21.6", "1.21.7", "1.21.8" -> ServerVersion.V1_21_R5;
                default -> ServerVersion.UNKNOWN;
            };
        } catch (Exception ignored) {
            return ServerVersion.UNKNOWN;
        }
    }

    /**
     * Checks if server version is higher than specified version.
     *
     * @param version version to compare against
     * @return true if server version is higher
     * @since 1.0.0
     */
    public static boolean isHigher(ServerVersion version) {
        return serverVersion().isHigher(version);
    }

    /**
     * Checks if server version is lower than specified version.
     *
     * @param version version to compare against
     * @return true if server version is lower
     * @since 1.0.0
     */
    public static boolean isLower(ServerVersion version) {
        return serverVersion().isLower(version);
    }

    /**
     * Checks if server version is higher than or equal to specified version.
     *
     * @param version version to compare against
     * @return true if server version is higher or equal
     * @since 1.0.0
     */
    public static boolean isHigherOrEqual(ServerVersion version) {
        return serverVersion().isHigherOrEqual(version);
    }

    /**
     * Checks if server version is lower than or equal to specified version.
     *
     * @param version version to compare against
     * @return true if server version is lower or equal
     * @since 1.0.0
     */
    public static boolean isLowerOrEqual(ServerVersion version) {
        return serverVersion().isLowerOrEqual(version);
    }

    /**
     * Checks if server version equals specified version.
     *
     * @param version version to compare against
     * @return true if versions are equal
     * @since 1.0.0
     */
    public static boolean isEqual(ServerVersion version) {
        return serverVersion() == version;
    }

    /**
     * Checks if server version is within specified range (inclusive).
     *
     * @param min minimum version (inclusive)
     * @param max maximum version (inclusive)
     * @return true if server version is in range
     * @since 1.0.0
     */
    public static boolean isInRange(ServerVersion min, ServerVersion max) {
        return serverVersion().isBetween(min, max);
    }

    /**
     * Checks if server supports modern Minecraft features (1.13+).
     * Includes: new command system, modern materials, etc.
     *
     * @return true if modern version
     * @since 1.0.0
     */
    public static boolean isModern() {
        return serverVersion().isModern();
    }

    /**
     * Checks if server is legacy version (pre-1.13).
     *
     * @return true if legacy version
     * @since 1.0.0
     */
    public static boolean isLegacy() {
        return serverVersion().isLegacy();
    }

    /**
     * Checks if server supports new command system (1.13+).
     *
     * @return true if new command system is supported
     * @since 1.0.0
     */
    public static boolean hasNewCommandSystem() {
        return isHigherOrEqual(ServerVersion.V1_13_R1);
    }

    /**
     * Checks if server supports Paper/Adventure components (1.16+).
     *
     * @return true if Paper components are supported
     * @since 1.0.0
     */
    public static boolean supportsPaperComponents() {
        return isHigherOrEqual(ServerVersion.V1_16_R1);
    }

    /**
     * Checks if server has new world generation (1.18+).
     *
     * @return true if new world generation is supported
     * @since 1.0.0
     */
    public static boolean hasNewWorldGeneration() {
        return isHigherOrEqual(ServerVersion.V1_18_R1);
    }

    /**
     * Gets the server version as human-readable string.
     *
     * @return version string (e.g., "1.20.4")
     * @since 1.0.0
     */
    public static String versionString() {
        return serverVersion().versionString();
    }

    /**
     * Gets the detected NMS version string.
     *
     * @return NMS version (e.g., "v1_20_R3") or "UNKNOWN"
     * @since 1.0.0
     */
    public static String nmsVersionString() {
        return serverVersion().name();
    }

    /**
     * Forces re-detection of server version.
     * Useful for testing or if server version changes at runtime. (IDK how is it possible, but...)
     *
     * @since 1.0.0
     */
    public static void forceRedetection() {
        SERVER_VERSION = null;
        DETECTED_NMS_VERSION = null;
    }
}
