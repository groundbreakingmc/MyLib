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
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(builder,
                    ColorCodesTranslator.COLOR_CHAR + "x" +
                            ColorCodesTranslator.COLOR_CHAR + group.charAt(0) +
                            ColorCodesTranslator.COLOR_CHAR + group.charAt(1) +
                            ColorCodesTranslator.COLOR_CHAR + group.charAt(2) +
                            ColorCodesTranslator.COLOR_CHAR + group.charAt(3) +
                            ColorCodesTranslator.COLOR_CHAR + group.charAt(4) +
                            ColorCodesTranslator.COLOR_CHAR + group.charAt(5));
        }

        message = matcher.appendTail(builder).toString();
        return ColorCodesTranslator.translateAlternateColorCodes('&', message);
    }
}
