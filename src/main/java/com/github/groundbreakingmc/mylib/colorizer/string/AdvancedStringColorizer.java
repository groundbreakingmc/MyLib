package com.github.groundbreakingmc.mylib.colorizer.string;

import com.github.groundbreakingmc.mylib.colorizer.ColorCodesTranslator;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Advanced colorizer with support for multiple hex formats:
 * - &amp;#rrggbb (6-digit hex)
 * - &amp;##rgb (3-digit hex, doubled)
 * - Standard color codes
 * <p>
 * This colorizer extends the basic legacy format by supporting:
 * <ul>
 *   <li><b>&amp;#rrggbb</b> - Full 6-digit hex color codes</li>
 *   <li><b>&amp;##rgb</b> - Short 3-digit hex codes (each digit is doubled)</li>
 *   <li><b>&amp;[0-9a-fA-Fk-oK-OrRxX]</b> - Standard Minecraft color codes</li>
 * </ul>
 * <p>
 * Examples:
 * <pre>
 * "&amp;#ff5555Hello"  → "§x§f§f§5§5§5§5Hello"
 * "&amp;##f5aWorld"    → "§x§f§f§5§5§a§aWorld"
 * "&amp;aTest"         → "§aTest"
 * </pre>
 * <p>
 * <b>Implementation:</b> Uses a state machine approach for efficient single-pass processing
 * without regex overhead. Handles incomplete color codes gracefully by preserving them in output.
 *
 * @author groundbreakingmc
 * @see HexStringColorizer
 * @since 1.0.0
 */
public final class AdvancedStringColorizer implements StringColorizer {

    /**
     * Colorizes the message using advanced legacy format processing.
     * <p>
     * The method uses a state machine with four states:
     * <ul>
     *   <li>Normal - processing regular text</li>
     *   <li>isColor - encountered '&amp;', expecting color code</li>
     *   <li>isHashtag - encountered '&amp;#', expecting hex or second '#'</li>
     *   <li>isDoubleTag - encountered '&amp;##', expecting 3-digit hex</li>
     * </ul>
     * <p>
     * Invalid color sequences are preserved in the output (e.g., "&amp;z" remains "&amp;z").
     *
     * @param message the message to colorize, may be null or empty
     * @return the colorized message, or the original if null/empty
     */
    @Override
    public String colorize(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final StringBuilder builder = new StringBuilder();
        final char[] messageChars = message.toCharArray();

        boolean isColor = false;
        boolean isHashtag = false;
        boolean isDoubleTag = false;

        for (int index = 0; index < messageChars.length; ) {
            final char currentChar = messageChars[index];
            if (isDoubleTag) {
                isDoubleTag = false;
                if (this.processDoubleTag(builder, messageChars, index)) {
                    index += 3;
                    continue;
                }

                builder.append("&##");

            } else if (isHashtag) {
                isHashtag = false;
                if (currentChar == '#') {
                    isDoubleTag = true;
                    index++;
                    continue;
                }

                if (this.processSingleTag(builder, messageChars, index)) {
                    index += 6;
                    continue;
                }

                builder.append("&#");

            } else if (isColor) {
                isColor = false;
                if (currentChar == '#') {
                    isHashtag = true;
                    index++;
                    continue;
                }

                if (ColorCodesTranslator.isColorCharacter(currentChar)) {
                    builder.append(ColorCodesTranslator.MC_COLOR_CHAR).append(currentChar);
                    index++;
                    continue;
                }

                builder.append('&');

            } else if (currentChar == '&') {
                isColor = true;
                index++;
            } else {
                builder.append(currentChar);
                index++;
            }
        }

        this.appendRemainingColorTags(builder, isColor, isHashtag, isDoubleTag);

        return builder.toString();
    }

    /**
     * Decolorizes the message by converting Minecraft color format back to advanced format.
     * <p>
     * Converts:
     * <ul>
     *   <li>§x§r§r§g§g§b§b → &amp;#rrggbb (full hex codes)</li>
     *   <li>§a, §l, etc. → &amp;a, &amp;l, etc. (standard codes)</li>
     * </ul>
     * <p>
     * Note: Cannot distinguish between original &amp;#rrggbb and &amp;##rgb formats,
     * always outputs full 6-digit hex format.
     *
     * @param colorized the colorized message to decolorize, may be null or empty
     * @return the message with &amp; color codes, or the original if null/empty
     */
    @Override
    public @UnknownNullability String decolorize(@Nullable String colorized) {
        return FastHexStringDecolorizer.decolorize(colorized);
    }

    /**
     * Processes a double-tag hex code (&##rgb format).
     * <p>
     * Converts 3-digit hex to Minecraft format by doubling each digit:
     * {@code &##f5a} → {@code §x§f§f§5§5§a§a}
     *
     * @param builder      the string builder to append to
     * @param messageChars the complete message character array
     * @param index        the current position (start of hex digits)
     * @return true if a valid 3-digit hex code was processed, false otherwise
     */
    private boolean processDoubleTag(final StringBuilder builder, final char[] messageChars, final int index) {
        if (index + 3 <= messageChars.length && this.isValidHexCode(messageChars, index, 3)) {
            builder.append(ColorCodesTranslator.MC_COLOR_CHAR).append('x');
            for (int i = index; i < index + 3; i++) {
                builder.append(ColorCodesTranslator.MC_COLOR_CHAR)
                        .append(messageChars[i])
                        .append(ColorCodesTranslator.MC_COLOR_CHAR)
                        .append(messageChars[i]);
            }

            return true;
        }

        return false;
    }

    /**
     * Processes a single-tag hex code (&#rrggbb format).
     * <p>
     * Converts 6-digit hex to Minecraft format:
     * {@code &#ff5555} → {@code §x§f§f§5§5§5§5}
     *
     * @param builder      the string builder to append to
     * @param messageChars the complete message character array
     * @param index        the current position (start of hex digits)
     * @return true if a valid 6-digit hex code was processed, false otherwise
     */
    private boolean processSingleTag(final StringBuilder builder, final char[] messageChars, final int index) {
        if (index + 6 <= messageChars.length && this.isValidHexCode(messageChars, index, 6)) {
            builder.append(ColorCodesTranslator.MC_COLOR_CHAR).append('x');
            for (int i = index; i < index + 6; i++) {
                builder.append(ColorCodesTranslator.MC_COLOR_CHAR).append(messageChars[i]);
            }

            return true;
        }

        return false;
    }

    /**
     * Validates if the specified character sequence is a valid hex code.
     * <p>
     * Accepts hex digits 0-9, a-f, A-F.
     *
     * @param chars  the character array to check
     * @param start  the starting index
     * @param length the number of characters to validate
     * @return true if all characters are valid hex digits, false otherwise
     */
    private boolean isValidHexCode(final char[] chars, final int start, final int length) {
        for (int i = start; i < start + length; i++) {
            char tmp = chars[i];
            if (!((tmp >= '0' && tmp <= '9') || (tmp >= 'a' && tmp <= 'f') || (tmp >= 'A' && tmp <= 'F'))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Appends any incomplete color tags that remained at the end of the string.
     * <p>
     * This ensures that incomplete sequences like trailing "&" or "&#" are preserved
     * in the output rather than being silently dropped.
     *
     * @param builder     the string builder to append to
     * @param isColor     true if ended in '&' state
     * @param isHashtag   true if ended in '&#' state
     * @param isDoubleTag true if ended in '&##' state
     */
    private void appendRemainingColorTags(final StringBuilder builder, final boolean isColor, final boolean isHashtag, final boolean isDoubleTag) {
        if (isColor) {
            builder.append('&');
        } else if (isHashtag) {
            builder.append("&#");
        } else if (isDoubleTag) {
            builder.append("&##");
        }
    }
}
