package com.github.groundbreakingmc.mylib.utils.time;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for parsing human-readable duration strings like "1h 30m" or "2d5h20m".
 * Returns the total duration in nanoseconds.
 */
@UtilityClass
public class DurationParser {

    private final long NS_IN_MICRO = 1_000L;
    private final long NS_IN_MILLI = 1_000_000L;
    private final long NS_IN_SECOND = 1_000_000_000L;
    private final long NS_IN_MINUTE = 60 * NS_IN_SECOND;
    private final long NS_IN_HOUR = 60 * NS_IN_MINUTE;
    private final long NS_IN_DAY = 24 * NS_IN_HOUR;

    /**
     * Parses a human-readable duration string into nanoseconds.
     * <p>
     * Supported units:
     * <ul>
     *     <li>ns, nanos, nanosecond(s)</li>
     *     <li>ms, millis, millisecond(s)</li>
     *     <li>s, second(s)</li>
     *     <li>m, minute(s)</li>
     *     <li>h, hour(s)</li>
     *     <li>d, day(s)</li>
     * </ul>
     * <p>
     * Examples:
     * <pre>
     *     parse("1h 30m") -> 5400000000000L
     *     parse("2d5h") -> 190800000000000L
     * </pre>
     *
     * @param input the duration string
     * @return total duration in nanoseconds
     * @throws IllegalArgumentException if input is invalid or an unknown unit is encountered
     */
    public long parse(@NotNull String input) {
        if (input.isBlank()) {
            throw new IllegalArgumentException("Input string cannot be null or blank");
        }

        long result = 0;

        final StringBuilder number = new StringBuilder(4); // small buffer to avoid resizing
        final StringBuilder unit = new StringBuilder(2); // small buffer to avoid resizing

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (Character.isDigit(c)) {
                if (!unit.isEmpty()) {
                    result += addDuration(parseNumber(number), unit.toString());
                    unit.setLength(0);
                }
                number.append(c);
            } else if (!Character.isWhitespace(c)) {
                unit.append(c);
            }
        }

        if (!unit.isEmpty()) {
            result += addDuration(parseNumber(number), unit.toString());
        }

        return result;
    }

    /**
     * Converts a duration in nanoseconds into a human-readable format
     * using the largest possible time units (e.g., "2d 3h 5m 1s 300ms").
     *
     * @param nanos the duration in nanoseconds
     * @return the formatted string
     */
    public String parse(long nanos) {
        if (nanos == 0) return "0s";

        final StringBuilder result = new StringBuilder(32); // small buffer to avoid resizing

        final long days = nanos / NS_IN_DAY;
        if (days > 0) {
            result.append(days).append('d').append(' ');
            nanos %= NS_IN_DAY;
        }

        final long hours = nanos / NS_IN_HOUR;
        if (hours > 0) {
            result.append(hours).append('h').append(' ');
            nanos %= NS_IN_HOUR;
        }

        final long minutes = nanos / NS_IN_MINUTE;
        if (minutes > 0) {
            result.append(minutes).append('m').append(' ');
            nanos %= NS_IN_MINUTE;
        }

        final long seconds = nanos / NS_IN_SECOND;
        if (seconds > 0) {
            result.append(seconds).append('s').append(' ');
            nanos %= NS_IN_SECOND;
        }

        final long millis = nanos / NS_IN_MILLI;
        if (millis > 0) {
            result.append(millis).append("ms").append(' ');
            nanos %= NS_IN_MILLI;
        }

        final long micros = nanos / NS_IN_MICRO;
        if (micros > 0) {
            result.append(micros).append("us").append(' ');
            nanos %= NS_IN_MICRO;
        }

        if (nanos > 0) {
            result.append(nanos).append("ns");
        }

        int len = result.length();
        if (len > 0 && result.charAt(len - 1) == ' ') {
            result.setLength(len - 1); // trim trailing space
        }

        return result.toString();
    }

    // Helper methods

    private long parseNumber(StringBuilder builder) {
        if (builder.isEmpty()) {
            throw new IllegalArgumentException("Missing number before unit");
        }
        try {
            final long value = Long.parseLong(builder.toString());
            builder.setLength(0);
            return value;
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number: " + builder, ex);
        }
    }

    private long addDuration(long number, String unitRaw) {
        final String unit = unitRaw.toLowerCase();
        return switch (unit) {
            case "ns", "nanos", "nanosecond", "nanoseconds" -> TimeUnit.NANOSECONDS.toNanos(number);
            case "us", "micro", "microsecond", "microseconds" -> TimeUnit.MICROSECONDS.toNanos(number);
            case "ms", "millis", "millisecond", "milliseconds" -> TimeUnit.MILLISECONDS.toNanos(number);
            case "s", "second", "seconds" -> TimeUnit.SECONDS.toNanos(number);
            case "m", "minute", "minutes" -> TimeUnit.MINUTES.toNanos(number);
            case "h", "hour", "hours" -> TimeUnit.HOURS.toNanos(number);
            case "d", "day", "days" -> TimeUnit.DAYS.toNanos(number);
            default -> throw new IllegalArgumentException("Unknown time unit: " + unitRaw);
        };
    }
}
