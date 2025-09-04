package com.github.groundbreakingmc.mylib.utils.server.version;

/**
 * Enum of all major Minecraft versions in ascending order.
 * Ordinal values are used comparisons.
 *
 * @version 1.0.1
 */
public enum ServerVersion {
    // Legacy versions
    V1_8_R1("1.8"),
    V1_8_R2("1.8.3"),
    V1_8_R3("1.8.8"),
    V1_9_R1("1.9.2"),
    V1_9_R2("1.9.4"),
    V1_10_R1("1.10.2"),
    V1_11_R1("1.11.2"),
    V1_12_R1("1.12.2"),

    // Modern versions (NMS mapping changed from 1.17+)
    V1_13_R1("1.13"),
    V1_13_R2("1.13.2"),
    V1_14_R1("1.14.4"),
    V1_15_R1("1.15.2"),
    V1_16_R1("1.16.1"),
    V1_16_R2("1.16.3"),
    V1_16_R3("1.16.5"),

    // New NMS format (1.17+)
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
    V1_21_R3("1.21.5"),
    V1_21_R5("1.21.8"),

    // If failed to detect server version
    UNKNOWN("unknown");

    private final String versionString;
    /**
     * @since 1.0.1
     */
    private final String nmsVersionString;
    private final int minor;
    private final int patch;

    ServerVersion(String versionString) {
        this.versionString = versionString;
        // converting V1_20_R3 to v1_20_R3 for packages
        this.nmsVersionString = super.name().charAt(0) == 'V' ? ("v" + name().substring(1)) : "UNKNOWN";
        String[] params = versionString.split("\\.");
        this.minor = params.length > 1 ? Integer.parseInt(params[1]) : 0;
        this.patch = params.length > 2 ? Integer.parseInt(params[2]) : 0;
    }

    /**
     * Gets the human-readable version string (e.g., "1.20.4").
     *
     * @return version string
     * @since 1.0.0
     */
    public String versionString() {
        return this.versionString;
    }

    /**
     * Gets the minor version number (e.g., if server version is "1.20.4" it will return 20).
     *
     * @return minor number
     * @since 1.0.0
     */
    public int minor() {
        return this.minor;
    }

    /**
     * Gets the patch version number (e.g., if server version is "1.20.4" it will return 4).
     *
     * @return patch number
     * @since 1.0.0
     */
    public int patch() {
        return this.patch;
    }

    /**
     * Gets the NMS package version (e.g., "v1_20_R3").
     *
     * @return NMS version string (e.g., "v1_20_R3") or "UNKNOWN"
     * @since 1.0.0
     */
    public String nmsVersion() {
        return this.nmsVersionString;
    }

    /**
     * Checks if this version is higher than another version.
     *
     * @param other version to compare against
     * @return true if this version is higher
     * @since 1.0.0
     */
    public boolean isHigher(ServerVersion other) {
        return this.ordinal() > other.ordinal();
    }

    /**
     * Checks if this version is lower than another version.
     *
     * @param other version to compare against
     * @return true if this version is lower
     * @since 1.0.0
     */
    public boolean isLower(ServerVersion other) {
        return this.ordinal() < other.ordinal();
    }

    /**
     * Checks if this version is higher than or equal to another version.
     *
     * @param other version to compare against
     * @return true if this version is higher or equal
     * @since 1.0.0
     */
    public boolean isHigherOrEqual(ServerVersion other) {
        return this.ordinal() >= other.ordinal();
    }

    /**
     * Checks if this version is lower than or equal to another version.
     *
     * @param other version to compare against
     * @return true if this version is lower or equal
     * @since 1.0.0
     */
    public boolean isLowerOrEqual(ServerVersion other) {
        return this.ordinal() <= other.ordinal();
    }

    /**
     * Checks if this version is between two other versions (inclusive).
     *
     * @param min minimum version (inclusive)
     * @param max maximum version (inclusive)
     * @return true if version is in range
     * @since 1.0.0
     */
    public boolean isBetween(ServerVersion min, ServerVersion max) {
        int thisOrdinal = this.ordinal();
        return thisOrdinal >= min.ordinal() && thisOrdinal <= max.ordinal();
    }

    /**
     * Checks if this version supports modern Minecraft features (1.13+).
     *
     * @return true if modern version
     * @since 1.0.0
     */
    public boolean isModern() {
        return this.ordinal() >= V1_13_R1.ordinal();
    }

    /**
     * Checks if this version is legacy (pre-1.13).
     *
     * @return true if legacy version
     * @since 1.0.0
     */
    public boolean isLegacy() {
        return this.ordinal() < V1_13_R1.ordinal();
    }
}
