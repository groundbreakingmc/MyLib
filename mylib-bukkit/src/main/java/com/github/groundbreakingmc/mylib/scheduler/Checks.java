package com.github.groundbreakingmc.mylib.scheduler;

/**
 * Argument validation shared by {@link PluginScheduler} implementations.
 */
final class Checks {

    private Checks() {
    }

    /**
     * @param delay the delay to validate
     * @throws IllegalArgumentException if {@code delay} is negative
     */
    static void checkDelay(long delay) {
        if (delay < 0L) throw new IllegalArgumentException("delay must be non-negative");
    }

    /**
     * @param period the repeat period to validate
     * @throws IllegalArgumentException if {@code period} is zero or negative
     */
    static void checkPeriod(long period) {
        if (period <= 0L) throw new IllegalArgumentException("period must be positive");
    }

    /**
     * Validates both timer arguments.
     *
     * @param delay  the initial delay
     * @param period the repeat period
     * @throws IllegalArgumentException if {@code delay} is negative or {@code period} is not positive
     */
    static void checkTimer(long delay, long period) {
        checkDelay(delay);
        checkPeriod(period);
    }
}
