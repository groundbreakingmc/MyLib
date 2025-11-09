package com.github.groundbreakingmc.mylib;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Benchmarks for hex color code conversion.
 *
 * Compares different approaches:
 * - appendReplacement() vs manual buffer management
 * - StringBuilder capacity pre-calculation
 *
 * Test data: 1000 strings with 1-5 random hex codes each.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(3)
@Warmup(iterations = 5, time = 3)
@Measurement(iterations = 10, time = 3)
public class ColorizerBenchmarks {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");
    private static final char ALT_COLOR_CHAR = '&';
    private static final char MC_COLOR_CHAR = '§';

    private List<String> testStrings;

    @Setup
    public void setup() {
        this.testStrings = new ArrayList<>(1000);

        for (int i = 0; i < 1000; i++) {
            this.testStrings.add(this.generateTestString());
        }
    }

    @Benchmark
    public int colorizeOld() {
        int sum = 0;
        for (int i = 0, size = this.testStrings.size(); i < size; i++) {
            sum += this.colorizeOld(this.testStrings.get(i)).length();
        }
        return sum;
    }

    @Benchmark
    public int colorizeGuessCapacity() {
        int sum = 0;
        for (int i = 0, size = this.testStrings.size(); i < size; i++) {
            sum += this.colorizeGuessCapacity(this.testStrings.get(i)).length();
        }
        return sum;
    }

    @Benchmark
    public int colorizeReplace() {
        int sum = 0;
        for (int i = 0, size = this.testStrings.size(); i < size; i++) {
            sum += this.colorizeReplace(this.testStrings.get(i)).length();
        }
        return sum;
    }

    @Benchmark
    public int colorizeReplaceAndGuessCapacity() {
        int sum = 0;
        for (int i = 0, size = this.testStrings.size(); i < size; i++) {
            sum += this.colorizeReplaceAndGuessCapacity(this.testStrings.get(i)).length();
        }
        return sum;
    }

    private String generateTestString() {
        final Random random = ThreadLocalRandom.current();
        final int colorCodesCount = random.nextInt(5) + 1;
        final StringBuilder sb = new StringBuilder(128);
        final int segmentsCount = colorCodesCount + 1;

        for (int i = 0; i < segmentsCount; i++) {
            final int textLength = random.nextInt(20) + 5;
            for (int j = 0; j < textLength; j++) {
                sb.append((char) ('a' + random.nextInt(26)));
            }

            if (i < colorCodesCount) {
                sb.append("&#");
                for (int j = 0; j < 6; j++) {
                    final int digit = random.nextInt(16);
                    sb.append(digit < 10 ? (char) ('0' + digit) : (char) ('a' + digit - 10));
                }
            }
        }

        return sb.toString();
    }

    public String colorizeOld(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuilder builder = new StringBuilder(message.length() + 32);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(builder,
                    MC_COLOR_CHAR + "x" +
                            MC_COLOR_CHAR + group.charAt(0) +
                            MC_COLOR_CHAR + group.charAt(1) +
                            MC_COLOR_CHAR + group.charAt(2) +
                            MC_COLOR_CHAR + group.charAt(3) +
                            MC_COLOR_CHAR + group.charAt(4) +
                            MC_COLOR_CHAR + group.charAt(5));

        }

        return matcher.appendTail(builder).toString();
    }

    public String colorizeGuessCapacity(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuilder builder = new StringBuilder(guessCapacity(message));
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(builder,
                    MC_COLOR_CHAR + "x" +
                            MC_COLOR_CHAR + group.charAt(0) +
                            MC_COLOR_CHAR + group.charAt(1) +
                            MC_COLOR_CHAR + group.charAt(2) +
                            MC_COLOR_CHAR + group.charAt(3) +
                            MC_COLOR_CHAR + group.charAt(4) +
                            MC_COLOR_CHAR + group.charAt(5));

        }

        return matcher.appendTail(builder).toString();
    }

    public String colorizeReplace(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuilder builder = new StringBuilder(message.length() + 32);
        final char[] codes = this.createStartCodes();

        int lastEnd = 0;
        while (matcher.find()) {
            builder.append(message, lastEnd, matcher.start());

            final String group = matcher.group(1);
            codes[3] = group.charAt(0);
            codes[5] = group.charAt(1);
            codes[7] = group.charAt(2);
            codes[9] = group.charAt(3);
            codes[11] = group.charAt(4);
            codes[13] = group.charAt(5);

            builder.append(codes);

            lastEnd = matcher.end();
        }

        builder.append(message, lastEnd, message.length());
        return builder.toString();
    }

    public String colorizeReplaceAndGuessCapacity(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuilder builder = new StringBuilder(guessCapacity(message));
        final char[] codes = this.createStartCodes();

        int lastEnd = 0;
        while (matcher.find()) {
            builder.append(message, lastEnd, matcher.start());

            final String group = matcher.group(1);
            codes[3] = group.charAt(0);
            codes[5] = group.charAt(1);
            codes[7] = group.charAt(2);
            codes[9] = group.charAt(3);
            codes[11] = group.charAt(4);
            codes[13] = group.charAt(5);

            builder.append(codes);

            lastEnd = matcher.end();
        }

        builder.append(message, lastEnd, message.length());
        return builder.toString();
    }

    private int guessCapacity(String message) {
        final char[] chars = message.toCharArray();

        int capacity = 0;
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == ALT_COLOR_CHAR && chars[++i] == '#') {
                // &#rrggbb ->
                // §x§r§r§g§g§b§v
                capacity += 6;
            }
        }

        return message.length() + capacity;
    }

    private char[] createStartCodes() {
        return new char[]{
                MC_COLOR_CHAR, 'x',
                MC_COLOR_CHAR, '0',
                MC_COLOR_CHAR, '1',
                MC_COLOR_CHAR, '2',
                MC_COLOR_CHAR, '3',
                MC_COLOR_CHAR, '4',
                MC_COLOR_CHAR, '5',
        };
    }
}
