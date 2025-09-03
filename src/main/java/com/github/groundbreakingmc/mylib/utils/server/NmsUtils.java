package com.github.groundbreakingmc.mylib.utils.server;

import com.github.groundbreakingmc.mylib.utils.server.version.ServerVersion;
import com.github.groundbreakingmc.mylib.utils.server.version.ServerVersionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for getting NMS (Net Minecraft Server) and OBC (CraftBukkit) classes
 * across different Minecraft versions and server implementations.
 *
 * <p>This class handles:</p>
 * <ul>
 *   <li>NMS package structure changes in Minecraft 1.17+ (obfuscated mappings removal)</li>
 *   <li>Paper remapping changes in 1.20.5+ (CraftBukkit package structure)</li>
 *   <li>Cross-version compatibility for reflection-based code</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This utility helps construct proper class names and package paths.
 *
 * @author GroundbreakingMC
 * @version 1.0
 * @since 1.0.0
 */
public final class NmsUtils {

    private static final boolean IS_1_17_OR_HIGHER;
    private static final boolean IS_PAPER_REMAPPED;
    private static final String NMS_PACKAGE_PREFIX;
    private static final String OBC_PACKAGE_PREFIX;

    static {
        IS_1_17_OR_HIGHER = ServerVersionUtils.isHigherOrEqual(ServerVersion.V1_17_R1);
        IS_PAPER_REMAPPED = ServerVersionUtils.isHigherOrEqual(ServerVersion.V1_20_R4) && ServerInfo.isPaperOrFork();

        if (IS_1_17_OR_HIGHER) {
            // 1.17+: net.minecraft.
            NMS_PACKAGE_PREFIX = "net.minecraft.";
        } else {
            // Pre-1.17: net.minecraft.server.v1_16_R3.
            NMS_PACKAGE_PREFIX = "net.minecraft.server." + ServerVersionUtils.nmsVersionString() + ".";
        }

        if (IS_PAPER_REMAPPED) {
            // Paper 1.20.5+: org.bukkit.craftbukkit. (no version suffix)
            OBC_PACKAGE_PREFIX = "org.bukkit.craftbukkit.";
        } else {
            // Spigot/older Paper: org.bukkit.craftbukkit.v1_X_RX.
            OBC_PACKAGE_PREFIX = "org.bukkit.craftbukkit." + ServerVersionUtils.nmsVersionString() + ".";
        }
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always, as this class should not be instantiated
     */
    private NmsUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Gets NMS class using modern class name (for 1.17+).
     *
     * @param modernClassName class name for 1.17+ versions
     * @return loaded class or null if not found
     * @since 1.0.0
     */
    public static @Nullable Class<?> nmsClass(@NotNull String modernClassName) {
        return nmsClass(modernClassName, modernClassName);
    }

    /**
     * Gets NMS class with different names for different version ranges.
     *
     * @param modernClassName class name for 1.17+ versions
     * @param legacyClassName class name for pre-1.17 versions
     * @return loaded class or null if not found
     * @since 1.0.0
     */
    public static @Nullable Class<?> nmsClass(@NotNull String modernClassName, @NotNull String legacyClassName) {
        try {
            String className = IS_1_17_OR_HIGHER ? modernClassName : legacyClassName;
            return Class.forName(NMS_PACKAGE_PREFIX + className);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    /**
     * Gets CraftBukkit class with different names for different version ranges.
     * From org.bukkit.craftbukkit.v1_X_RX if is not paper or fork and not 1.20.5+
     * Else org.bukkit.craftbukkit
     *
     * @param className class name
     * @return loaded class or null if not found
     * @since 1.0.0
     */
    public static @Nullable Class<?> obcClass(@NotNull String className) {
        try {
            return Class.forName(OBC_PACKAGE_PREFIX + className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Gets the NMS package prefix for current server version.
     *
     * @return NMS package prefix (e.g., "net.minecraft." or "net.minecraft.server.v1_16_R3.")
     * @since 1.0.0
     */
    public static @NotNull String nmsPackagePrefix() {
        return NMS_PACKAGE_PREFIX;
    }

    /**
     * Gets the CraftBukkit package prefix for current server version and developer.
     *
     * @return CraftBukkit package prefix (e.g., "org.bukkit.craftbukkit" or "org.bukkit.craftbukkit.v1_16_R3.")
     * @since 1.0.0
     */
    public static @NotNull String obcPackagePrefix() {
        return OBC_PACKAGE_PREFIX;
    }

    /**
     * Checks if server uses modern NMS structure (1.17+).
     *
     * @return true if 1.17 or higher
     * @since 1.0.0
     */
    public static boolean isModernNms() {
        return IS_1_17_OR_HIGHER;
    }

    /**
     * Checks if server uses modern Paper structure (1.20.5+).
     *
     * @return true if 1.20.5 or higher and Paper or his fork
     * @since 1.0.0
     */
    public static boolean isModernObc() {
        return IS_PAPER_REMAPPED;
    }
}
