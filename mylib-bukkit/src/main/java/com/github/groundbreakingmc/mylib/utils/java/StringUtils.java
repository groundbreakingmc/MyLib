package com.github.groundbreakingmc.mylib.utils.java;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@UtilityClass
public class StringUtils {

    /**
     * Removes all occurrences of the specified character from the input string.
     *
     * @param input    the original string (may not be {@code null})
     * @param toRemove the character to remove
     * @return a new string with all instances of {@code toRemove} removed;
     * returns {@code input} if input not contains {@code toRemove};
     */
    public static @NotNull String removeChar(@NotNull String input, char toRemove) {
        if (input.indexOf(toRemove) == -1) {
            return input;
        }

        final char[] result = new char[input.length()];
        int length = 0;
        for (int i = 0; i < input.length(); i++) {
            final char current = input.charAt(i);
            if (current != toRemove) {
                result[length++] = current;
            }
        }

        return String.valueOf(Arrays.copyOfRange(result, 0, length));
    }
}
