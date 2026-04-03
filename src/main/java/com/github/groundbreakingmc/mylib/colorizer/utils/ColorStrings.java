package com.github.groundbreakingmc.mylib.colorizer.utils;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Utility methods for measuring and stripping color codes from raw strings.
 * <p>
 * All methods operate on the <b>raw input</b> (with {@code &} codes), not on
 * already-colorized output (with {@code §} codes).
 */
@UtilityClass
public final class ColorStrings {

    /**
     * Returns the visual length of a string with basic {@code &x} color codes stripped.
     * <p>
     * Strips pairs {@code &[0-9a-fk-orA-FK-OR]} — each pair contributes 0 visible characters.
     * Non-matching {@code &} characters are counted normally.
     * <p>
     * Example: {@code "&aHello &cWorld"} → 11
     *
     * @param message raw message with {@code &} color codes
     * @return visual character count
     */
    public static int basicVisualLength(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }

        final char[] chars = message.toCharArray();
        int length = 0;
        int i = 0;

        while (i < chars.length) {
            if (chars[i] == ColorCodesTranslator.ALT_COLOR_CHAR
                    && i + 1 < chars.length
                    && ColorCodesTranslator.isColorCharacter(chars[i + 1])) {
                i += 2; // skip &x
            } else {
                length++;
                i++;
            }
        }

        return length;
    }

    /**
     * Returns the visual length of a string with legacy hex AND basic color codes stripped.
     * <p>
     * Additionally strips:
     * <ul>
     *   <li>{@code &#rrggbb} — 8 characters, 0 visible</li>
     *   <li>{@code &x&r&r&g&g&b&b} — 14 characters, 0 visible</li>
     * </ul>
     * <p>
     * Example: {@code "&#ff5555Hello &aWorld"} → 11
     *
     * @param message raw message with {@code &} and {@code &#} color codes
     * @return visual character count
     */
    public static int hexVisualLength(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }

        final char[] chars = message.toCharArray();
        int length = 0;
        int i = 0;

        while (i < chars.length) {
            if (chars[i] != ColorCodesTranslator.ALT_COLOR_CHAR) {
                length++;
                i++;
                continue;
            }

            // &
            if (i + 1 >= chars.length) {
                length++; // trailing & with nothing after
                i++;
                continue;
            }

            final char next = chars[i + 1];

            if (next == '#' && i + 7 < chars.length && isValidHex(chars, i + 2, 6)) {
                i += 8; // skip &#rrggbb
            } else if ((next == 'x' || next == 'X') && isLegacyHexSequence(chars, i + 1)) {
                i += 14; // skip &x&r&r&g&g&b&b
            } else if (ColorCodesTranslator.isColorCharacter(next)) {
                i += 2; // skip &x
            } else {
                length++; // bare &, not a valid code
                i++;
            }
        }

        return length;
    }

    /**
     * Returns the visual length of a string with advanced hex codes stripped.
     * <p>
     * Additionally, strips the short {@code &##rgb} format (6 characters, 0 visible)
     * on top of everything {@link #hexVisualLength(String)} strips.
     * <p>
     * Example: {@code "&##f5aHello"} → 5
     *
     * @param message raw message with advanced color codes
     * @return visual character count
     */
    public static int advancedVisualLength(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }

        final char[] chars = message.toCharArray();
        int length = 0;
        int i = 0;

        while (i < chars.length) {
            if (chars[i] != ColorCodesTranslator.ALT_COLOR_CHAR) {
                length++;
                i++;
                continue;
            }

            if (i + 1 >= chars.length) {
                length++;
                i++;
                continue;
            }

            final char next = chars[i + 1];

            if (next == '#') {
                if (i + 2 < chars.length && chars[i + 2] == '#'
                        && i + 5 < chars.length && isValidHex(chars, i + 3, 3)) {
                    i += 6; // skip &##rgb
                } else if (i + 7 < chars.length && isValidHex(chars, i + 2, 6)) {
                    i += 8; // skip &#rrggbb
                } else {
                    length++;
                    i++;
                }
            } else if ((next == 'x' || next == 'X') && isLegacyHexSequence(chars, i + 1)) {
                i += 14; // skip &x&r&r&g&g&b&b
            } else if (ColorCodesTranslator.isColorCharacter(next)) {
                i += 2;
            } else {
                length++;
                i++;
            }
        }

        return length;
    }

    /**
     * Returns the visual length of a string with MiniMessage tags stripped.
     * <p>
     * Strips balanced and self-closing tags: {@code <red>}, {@code </red>},
     * {@code <#ff5555>}, {@code <bold>}, etc.
     * <p>
     * Example: {@code "<red>Hello</red> <bold>World</bold>"} → 11
     *
     * @param message raw message with MiniMessage tags
     * @return visual character count
     */
    public static int miniMessageVisualLength(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }


        return miniMessageStrip(message).length();
    }

    // --- strip methods ---

    /**
     * Strips basic {@code &x} color codes from the message, returning plain text.
     * <p>
     * Example: {@code "&aHello &cWorld"} → {@code "Hello World"}
     *
     * @param message raw message with {@code &} color codes; may be null
     * @return plain text without color codes, or {@code null} if input was {@code null}
     */
    public static String basicStrip(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final char[] chars = message.toCharArray();
        final StringBuilder builder = new StringBuilder(chars.length);
        int i = 0;

        while (i < chars.length) {
            if (chars[i] == ColorCodesTranslator.ALT_COLOR_CHAR
                    && i + 1 < chars.length
                    && ColorCodesTranslator.isColorCharacter(chars[i + 1])) {
                i += 2;
            } else {
                builder.append(chars[i++]);
            }
        }

        return builder.toString();
    }

    /**
     * Strips legacy hex ({@code &#rrggbb}, {@code &x&r&r&g&g&b&b}) and basic
     * {@code &x} color codes from the message, returning plain text.
     * <p>
     * Example: {@code "&#ff5555Hello &aWorld"} → {@code "Hello World"}
     *
     * @param message raw message; may be null
     * @return plain text without color codes, or {@code null} if input was {@code null}
     */
    public static String hexStrip(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final char[] chars = message.toCharArray();
        final StringBuilder builder = new StringBuilder(chars.length);
        int i = 0;

        while (i < chars.length) {
            if (chars[i] != ColorCodesTranslator.ALT_COLOR_CHAR) {
                builder.append(chars[i++]);
                continue;
            }

            if (i + 1 >= chars.length) {
                builder.append(chars[i++]);
                continue;
            }

            final char next = chars[i + 1];

            if (next == '#' && i + 7 < chars.length && isValidHex(chars, i + 2, 6)) {
                i += 8;
            } else if ((next == 'x' || next == 'X') && isLegacyHexSequence(chars, i + 1)) {
                i += 14;
            } else if (ColorCodesTranslator.isColorCharacter(next)) {
                i += 2;
            } else {
                builder.append(chars[i++]);
            }
        }

        return builder.toString();
    }

    /**
     * Strips advanced hex ({@code &##rgb}, {@code &#rrggbb}) and basic
     * {@code &x} color codes from the message, returning plain text.
     * <p>
     * Example: {@code "&##f5aHello"} → {@code "Hello"}
     *
     * @param message raw message; may be null
     * @return plain text without color codes, or {@code null} if input was {@code null}
     */
    public static String advancedStrip(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final char[] chars = message.toCharArray();
        final StringBuilder builder = new StringBuilder(chars.length);
        int i = 0;

        while (i < chars.length) {
            if (chars[i] != ColorCodesTranslator.ALT_COLOR_CHAR) {
                builder.append(chars[i++]);
                continue;
            }

            if (i + 1 >= chars.length) {
                builder.append(chars[i++]);
                continue;
            }

            final char next = chars[i + 1];

            if (next == '#') {
                if (i + 2 < chars.length && chars[i + 2] == '#'
                        && i + 5 < chars.length && isValidHex(chars, i + 3, 3)) {
                    i += 6;
                } else if (i + 7 < chars.length && isValidHex(chars, i + 2, 6)) {
                    i += 8;
                } else {
                    builder.append(chars[i++]);
                }
            } else if ((next == 'x' || next == 'X') && isLegacyHexSequence(chars, i + 1)) {
                i += 14;
            } else if (ColorCodesTranslator.isColorCharacter(next)) {
                i += 2;
            } else {
                builder.append(chars[i++]);
            }
        }

        return builder.toString();
    }

    /**
     * Strips MiniMessage {@code <tag>} patterns from the message, returning plain text.
     * <p>
     * Example: {@code "<red>Hello</red> <bold>World</bold>"} → {@code "Hello World"}
     *
     * @param message raw message with MiniMessage tags; may be null
     * @return plain text without tags, or {@code null} if input was {@code null}
     */
    public static String miniMessageStrip(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        return PlainTextComponentSerializer.plainText().serialize(
                MiniMessage.miniMessage().deserialize(message)
        );
    }

    // ===== HELPER METHODS =====

    private static boolean isValidHex(char[] chars, int start, int len) {
        if (start + len > chars.length) return false;
        for (int i = start; i < start + len; i++) {
            if (!ColorCodesTranslator.isHexCharacter(chars[i])) return false;
        }
        return true;
    }

    /**
     * Checks whether the sequence starting at {@code xPos} is a full
     * {@code x&h&h&h&h&h&h} legacy hex sequence (13 characters including the 'x').
     *
     * @param chars source array
     * @param xPos  position of the 'x'/'X' character
     */
    private static boolean isLegacyHexSequence(char[] chars, int xPos) {
        if (xPos + 13 > chars.length) return false;
        int pos = xPos + 1;
        for (int j = 0; j < 6; j++, pos += 2) {
            if (chars[pos] != ColorCodesTranslator.ALT_COLOR_CHAR) return false;
            if (!ColorCodesTranslator.isHexCharacter(chars[pos + 1])) return false;
        }
        return true;
    }

    /**
     * Returns the index of the closing {@code >} of a MiniMessage tag,
     * or {@code -1} if the tag is malformed (empty, contains spaces other than
     * in quoted arguments, or is not properly closed).
     *
     * @param chars source array
     * @param start position after the opening {@code <}
     */
    private static int indexOfTagClose(char[] chars, int start) {
        if (start >= chars.length || chars[start] == '>') return -1;
        for (int i = start; i < chars.length; i++) {
            final char c = chars[i];
            if (c == '>') return i;
            if (c == '<') return -1; // nested < before > → malformed
        }
        return -1; // no closing >
    }
}
