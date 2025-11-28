package com.github.groundbreakingmc.mylib.logger.console.colorizer;

public class AnsiColorTranslator {

    private static final char ALT_COLOR_CHAR = '&';
    private static final char MC_COLOR_CHAR = '§';
    public static final char ANSI_COLOR_CHAR = '\u001B';

    public static final char[] EMPTY = new char[0];

    public static final char[] RESET = new char[]{'[', '0', 'm'};

    public static final char[] BLACK = new char[]{'[', '3', '0', 'm'}; // &0 | §0
    public static final char[] DARK_BLUE = new char[]{'[', '3', '4', 'm'}; // &1 | §1
    public static final char[] DARK_GREEN = new char[]{'[', '3', '2', 'm'}; // &2 | §2
    public static final char[] DARK_TURQUOISE = new char[]{'[', '3', '6', 'm'}; // &3 | §3
    public static final char[] DARK_RED = new char[]{'[', '3', '1', 'm'}; // &4 | §4
    public static final char[] PURPLE = new char[]{'[', '3', '5', 'm'}; // &5 | §5
    public static final char[] DARK_YELLOW = new char[]{'[', '3', '3', 'm'}; // &6 | §6
    public static final char[] LIGHT_GRAY = new char[]{'[', '3', '7', 'm'}; // &7 | §7
    public static final char[] DARK_GRAY = new char[]{'[', '9', '0', 'm'}; // &8 | §8
    public static final char[] LIGHT_BLUE = new char[]{'[', '9', '4', 'm'}; // &9 | §9

    public static final char[] LIGHT_GREEN = new char[]{'[', '9', '2', 'm'}; // &a | §a
    public static final char[] LIGHT_TURQUOISE = new char[]{'[', '9', '6', 'm'}; // &b | §b
    public static final char[] LIGHT_RED = new char[]{'[', '9', '1', 'm'}; // &c | §c
    public static final char[] MAGENTA = new char[]{'[', '9', '5', 'm'}; // &d | §d
    public static final char[] LIGHT_YELLOW = new char[]{'[', '9', '3', 'm'}; // &e | §e
    public static final char[] WHITE = new char[]{'[', '9', '7', 'm'}; // &f | §f

    public static final char[] BOLD = new char[]{'[', '1', 'm'}; // &l | §l
    public static final char[] UNDERLINE = new char[]{'[', '4', 'm'}; // &n | §n
    public static final char[] ITALIC = new char[]{'[', '3', 'm'}; // &o | §o
    public static final char[] STRIKETHROUGH = new char[]{'[', '9', 'm'}; // &m | §m

    public static String translate(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final char[] chars = message.toCharArray();
        final int length = chars.length;

        final StringBuilder builder = new StringBuilder(length + 32);

        int start = 0, end;
        for (int i = 0; i < length - 1; ) {
            final char ch = chars[i];
            if (ch == ALT_COLOR_CHAR || ch == MC_COLOR_CHAR) {
                final char[] ansi = toAnsi(chars[++i]);
                if (ansi.length > 0) {
                    end = i - 1;
                    builder.append(chars, start, end - start);
                    builder.append(ANSI_COLOR_CHAR);
                    builder.append(ansi);
                    start = i + 1;
                }
            }
            ++i;
        }

        builder.append(chars, start, length - start);
        builder.append(ANSI_COLOR_CHAR).append(RESET);
        return builder.toString();
    }

    private static char[] toAnsi(char ch) {
        return switch (ch) {
            case '0' -> BLACK;
            case '1' -> DARK_BLUE;
            case '2' -> DARK_GREEN;
            case '3' -> DARK_TURQUOISE;
            case '4' -> DARK_RED;
            case '5' -> PURPLE;
            case '6' -> DARK_YELLOW;
            case '7' -> LIGHT_GRAY;
            case '8' -> DARK_GRAY;
            case '9' -> LIGHT_BLUE;
            case 'a', 'A' -> LIGHT_GREEN;
            case 'b', 'B' -> LIGHT_TURQUOISE;
            case 'c', 'C' -> LIGHT_RED;
            case 'd', 'D' -> MAGENTA;
            case 'e', 'E' -> LIGHT_YELLOW;
            case 'f', 'F' -> WHITE;
            case 'r', 'R' -> RESET;
            case 'l', 'L' -> BOLD;
            case 'm', 'M' -> STRIKETHROUGH;
            case 'n', 'N' -> UNDERLINE;
            case 'o', 'O' -> ITALIC;
            default -> EMPTY;
        };
    }
}
