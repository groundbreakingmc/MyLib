package com.github.groundbreakingmc.mylib.colorizer;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ColorCodesTranslator {

    public static final char ALT_COLOR_CHAR = '&';
    public static final char MC_COLOR_CHAR = '§';

    public static String translateAlternateColorCodes(String textToTranslate) {
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

    public static boolean isColorCharacter(final char c) {
        return (c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'f') ||
                c == 'r' ||
                (c >= 'k' && c <= 'o') ||
                c == 'x' ||
                (c >= 'A' && c <= 'F') ||
                c == 'R' ||
                (c >= 'K' && c <= 'O') ||
                c == 'X';
    }
}
