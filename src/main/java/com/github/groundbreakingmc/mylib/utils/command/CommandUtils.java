package com.github.groundbreakingmc.mylib.utils.command;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public final class CommandUtils {

    public static int getLength(final String input) {
        int amount = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == ' ') {
                amount++;
            }
        }

        return amount;
    }

    @Nullable
    public static String getArgument(final String input, final int length, final int index) {
        if (length <= index) {
            return null;
        }

        int wordStart = -1;
        int wordEnd;
        int wordCount = 0;

        for (int i = input.indexOf(' '); i < input.length(); i++) {
            char c = input.charAt(i);

            if (Character.isWhitespace(c) || c == '\t' || c == '\n') {
                if (wordStart != -1) {
                    wordEnd = i;
                    if (wordCount == index) {
                        return input.substring(wordStart, wordEnd);
                    }
                    wordStart = -1;
                    wordCount++;
                }
            } else {
                if (wordStart == -1) {
                    wordStart = i;
                }
            }
        }

        if (wordStart != -1) {
            wordEnd = input.length();
            if (wordCount == index) {
                return input.substring(wordStart, wordEnd);
            }
        }

        return "";
    }

    /**
     * @deprecated Use {@link org.bukkit.util.StringUtil.startsWithIgnoreCase(String, String) StringUtil.startsWithIgnoreCase(String)} instead
     *
     * @param input
     * @param completion
     * @return
     */
    @Deprecated
    public static boolean startsWithIgnoreCase(final String input, final String completion) {
        if (completion == null || input == null) {
            return false;
        }

        return completion.regionMatches(true, 0, input, 0, input.length());
    }

    public static boolean isUsedBefore(final String buffer, final String search, final int length) {
        if (length > 2) {
            int i = buffer.lastIndexOf(' ');
            if (i == -1) {
                return false;
            }

            i--;
            for (int searchIndex = search.length() - 1; i >= 0; i--, searchIndex--) {
                if (buffer.charAt(i) == ' ') {
                    return true;
                }

                if (buffer.charAt(i) != search.charAt(searchIndex)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }
}
