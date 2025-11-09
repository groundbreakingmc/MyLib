package com.github.groundbreakingmc.mylib;

import com.github.groundbreakingmc.mylib.colorizer.ColorCodesTranslator;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(3)
@Warmup(iterations = 5, time = 3)
@Measurement(iterations = 10, time = 3)
public class HexColorizerBenchmarks {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");
    private static final char ALT_COLOR_CHAR = '&';
    private static final char HEX_MARKER = '#';
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
    public int pattern() {
        int sum = 0;
        for (int i = 0, size = this.testStrings.size(); i < size; i++) {
            sum += this.pattern(this.testStrings.get(i)).length();
        }
        return sum;
    }

    @Benchmark
    public int fast() {
        int sum = 0;
        for (int i = 0, size = this.testStrings.size(); i < size; i++) {
            sum += this.fast(this.testStrings.get(i)).length();
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

    public String pattern(String message) {
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

            lastEnd = matcher.end();
            builder.append(codes);
        }

        builder.append(message, lastEnd, message.length());
        return this.translateAlternateColorCodes(builder.toString());
    }

    public String fast(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final char[] chars = message.toCharArray();
        final int length = chars.length;

        final StringBuilder builder = new StringBuilder(length + 32);
        final char[] hex = new char[14];
        hex[0] = ColorCodesTranslator.MC_COLOR_CHAR;
        hex[1] = 'x';

        int start = 0, end;
        loop:
        for (int i = 0; i < length - 1; ) {
            final char ch = chars[i];
            if (ch == ColorCodesTranslator.ALT_COLOR_CHAR) {
                final char nextChar = chars[++i];
                if (nextChar == HEX_MARKER) {
                    if (i + 6 >= chars.length) break;
                    end = i - 1;
                    for (int j = 0, hexI = 1; j < 6; j++) {
                        final char hexChar = chars[++i];
                        if (!isHexCharacter(hexChar)) {
                            continue loop;
                        }
                        hex[++hexI] = ColorCodesTranslator.MC_COLOR_CHAR;
                        hex[++hexI] = hexChar;
                    }
                    builder.append(chars, start, end - start).append(hex);
                    start = i + 1;
                } else {
                    if (ColorCodesTranslator.isColorCharacter(nextChar)) {
                        chars[i - 1] = ColorCodesTranslator.MC_COLOR_CHAR;
                        chars[i] = (char) (nextChar | 0x20); // quick version of 'to lower case' for character
                    }
                }
            }
            ++i;
        }

        builder.append(chars, start, chars.length - start);
        return builder.toString();
    }

    private char[] createStartCodes() {
        return new char[]{
                ColorCodesTranslator.MC_COLOR_CHAR, 'x',
                ColorCodesTranslator.MC_COLOR_CHAR, '0',
                ColorCodesTranslator.MC_COLOR_CHAR, '1',
                ColorCodesTranslator.MC_COLOR_CHAR, '2',
                ColorCodesTranslator.MC_COLOR_CHAR, '3',
                ColorCodesTranslator.MC_COLOR_CHAR, '4',
                ColorCodesTranslator.MC_COLOR_CHAR, '5',
        };
    }

    private String translateAlternateColorCodes(String textToTranslate) {
        final char[] charArray = textToTranslate.toCharArray();
        int i = 0;
        while (i < charArray.length - 1) {
            if (charArray[i] == ALT_COLOR_CHAR) {
                final char nextChar = charArray[i + 1];
                if (isColorCharacter(nextChar)) {
                    charArray[i] = MC_COLOR_CHAR;
                    charArray[++i] = (char) (nextChar | 0x20);
                }
            }
            i++;
        }

        return new String(charArray);
    }

    public boolean isColorCharacter(final char ch) {
        return switch (ch) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F',
                    'r', 'R', 'k', 'K', 'm', 'M', 'n', 'N', 'o', 'O', 'x', 'X' -> true;
            default -> false;
        };
    }

    public boolean isHexCharacter(final char ch) {
        return switch (ch) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F' -> true;
            default -> false;
        };
    }
}
