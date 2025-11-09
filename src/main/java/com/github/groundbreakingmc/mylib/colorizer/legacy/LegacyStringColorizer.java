package com.github.groundbreakingmc.mylib.colorizer.legacy;

import com.github.groundbreakingmc.mylib.colorizer.ColorCodesTranslator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyStringColorizer implements StringColorizer {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

    @Override
    public String colorize(String message) {
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
        return ColorCodesTranslator.translateAlternateColorCodes(builder.toString());
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
}
