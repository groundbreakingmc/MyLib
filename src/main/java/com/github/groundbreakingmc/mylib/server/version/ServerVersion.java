package com.github.groundbreakingmc.mylib.server.version;

/**
 * Enum of all major Minecraft server versions in ascending order.
 * Ordinal values are used for comparisons.
 *
 * <p>NMS R-version mappings are sourced from the official Spigot wiki:
 * <a href="https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-1-21/">1.21+</a>
 * <a href="https://www.spigotmc.org/wiki/spigot-nms-and-minecraft-versions-1-16/">1.16–1.20</a>
 *
 * @since 1.0.0
 */
public enum ServerVersion {

    // ── Legacy (pre-1.13, obfuscated NMS) ─────────────────────────────────────
    V1_8_R1("1.8"),
    V1_8_R2("1.8.3"),
    V1_8_R3("1.8.8"),
    V1_9_R1("1.9.2"),
    V1_9_R2("1.9.4"),
    V1_10_R1("1.10.2"),
    V1_11_R1("1.11.2"),
    V1_12_R1("1.12.2"),

    // ── Modern (1.13–1.16, pre-Mojang-mappings) ────────────────────────────────
    V1_13_R1("1.13"),
    V1_13_R2("1.13.2"),
    V1_14_R1("1.14.4"),
    V1_15_R1("1.15.2"),
    V1_16_R1("1.16.1"),
    V1_16_R2("1.16.3"),
    V1_16_R3("1.16.5"),

    // ── Mojang-mapped NMS (1.17+) ─────────────────────────────────────────────
    V1_17_R1("1.17.1"),
    V1_18_R1("1.18.1"),
    V1_18_R2("1.18.2"),
    V1_19_R1("1.19.2"),
    V1_19_R2("1.19.3"),
    V1_19_R3("1.19.4"),
    V1_20_R1("1.20.1"),
    V1_20_R2("1.20.2"),
    V1_20_R3("1.20.4"),
    V1_20_R4("1.20.6"),
    V1_21_R1("1.21.1"),
    V1_21_R2("1.21.3"),
    V1_21_R3("1.21.4"),
    V1_21_R4("1.21.5"),
    V1_21_R5("1.21.8"),
    V1_21_R6("1.21.11"),

    // ── New versioning scheme (year-based, 2026+) ─────────────────────────────
    V26_R1("26.1"),
    V26_R2("26.2"),

    // ── Fallback ──────────────────────────────────────────────────────────────
    UNKNOWN("unknown");

    private final String versionString;
    private final String nmsVersionString;
    private final int minor;
    private final int patch;

    ServerVersion(String versionString) {
        this.versionString = versionString;
        // V1_20_R3 → v1_20_R3 for use in legacy package names (pre-1.17)
        final char first = name().charAt(0);
        this.nmsVersionString = first == 'V' ? "v" + name().substring(1) : "UNKNOWN";
        final String[] parts = versionString.split("\\.");
        this.minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        this.patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
    }

    /**
     * Returns the human-readable version string (e.g. {@code "1.20.4"}).
     *
     * @since 1.0.0
     */
    public String versionString() {
        return this.versionString;
    }

    /**
     * Returns the minor version component (e.g. {@code 20} for {@code "1.20.4"}).
     *
     * @since 1.0.0
     */
    public int minor() {
        return this.minor;
    }

    /**
     * Returns the patch version component (e.g. {@code 4} for {@code "1.20.4"}).
     *
     * @since 1.0.0
     */
    public int patch() {
        return this.patch;
    }

    /**
     * Returns the NMS package version string (e.g. {@code "v1_20_R3"}).
     * Used for pre-1.17 package path construction. Returns {@code "UNKNOWN"} for
     * the {@link #UNKNOWN} constant.
     *
     * @since 1.0.0
     */
    public String nmsVersion() {
        return this.nmsVersionString;
    }

    /**
     * Returns {@code true} if this version is strictly higher than {@code other}.
     *
     * @since 1.0.0
     */
    public boolean isHigher(ServerVersion other) {
        return this.ordinal() > other.ordinal();
    }

    /**
     * Returns {@code true} if this version is strictly lower than {@code other}.
     *
     * @since 1.0.0
     */
    public boolean isLower(ServerVersion other) {
        return this.ordinal() < other.ordinal();
    }

    /**
     * Returns {@code true} if this version is higher than or equal to {@code other}.
     *
     * @since 1.0.0
     */
    public boolean isHigherOrEqual(ServerVersion other) {
        return this.ordinal() >= other.ordinal();
    }

    /**
     * Returns {@code true} if this version is lower than or equal to {@code other}.
     *
     * @since 1.0.0
     */
    public boolean isLowerOrEqual(ServerVersion other) {
        return this.ordinal() <= other.ordinal();
    }

    /**
     * Returns {@code true} if this version falls within [{@code min}, {@code max}] inclusive.
     *
     * @since 1.0.0
     */
    public boolean isBetween(ServerVersion min, ServerVersion max) {
        final int ord = this.ordinal();
        return ord >= min.ordinal() && ord <= max.ordinal();
    }

    /**
     * Returns {@code true} if this is a modern version (1.13+).
     *
     * @since 1.0.0
     */
    public boolean isModern() {
        return this.ordinal() >= V1_13_R1.ordinal();
    }

    /**
     * Returns {@code true} if this is a legacy version (pre-1.13).
     *
     * @since 1.0.0
     */
    public boolean isLegacy() {
        return this.ordinal() < V1_13_R1.ordinal();
    }
}
