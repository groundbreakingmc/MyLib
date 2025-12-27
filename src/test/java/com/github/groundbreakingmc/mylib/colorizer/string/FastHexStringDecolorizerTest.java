package com.github.groundbreakingmc.mylib.colorizer.string;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FastHexStringDecolorizerTest {

    @Test
    void testNullMessage() {
        assertNull(FastHexStringDecolorizer.decolorize(null));
    }

    @Test
    void testEmptyMessage() {
        assertEquals("", FastHexStringDecolorizer.decolorize(""));
    }

    @Test
    void testBasicHexColor() {
        String input = "§x§f§f§5§5§5§5Hello";
        String expected = "&#ff5555Hello";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testMultipleHexColors() {
        String input = "§x§f§f§5§5§5§5Hello §x§0§0§f§f§0§0World";
        String expected = "&#ff5555Hello &#00ff00World";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testUppercaseHexColor() {
        String input = "§x§F§F§5§5§5§5Text";
        String expected = "§x§F§F&5&5&5&5Text";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testMixedCaseHexColor() {
        String input = "§x§F§f§5§A§3§bTest";
        String expected = "§x§F&f&5§A&3&bTest";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testBasicColorCodes() {
        String input = "§aGreen §cRed";
        String expected = "&aGreen &cRed";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testFormatCodes() {
        String input = "§lBold §mStrike §nUnderline";
        String expected = "&lBold &mStrike &nUnderline";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testMixedHexAndBasicColors() {
        String input = "§aGreen §x§f§f§0§0§0§0Red §bBlue";
        String expected = "&aGreen &#ff0000Red &bBlue";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testConsecutiveHexColors() {
        String input = "§x§f§f§0§0§0§0§x§0§0§f§f§0§0§x§0§0§0§0§f§f";
        String expected = "&#ff0000&#00ff00&#0000ff";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testHexColorAtEnd() {
        String input = "Text§x§f§f§f§f§f§f";
        String expected = "Text&#ffffff";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testHexColorAtStart() {
        String input = "§x§f§f§f§f§f§fText";
        String expected = "&#ffffffText";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testStandaloneMinecraftColorChar() {
        String input = "Test § text";
        assertEquals(input, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testComplexMessage() {
        String input = "§l§a§x§0§0§f§f§0§0Bold Green §x§f§f§0§0§0§0Red §r§oItalic";
        String expected = "&l&a&#00ff00Bold Green &#ff0000Red &r&oItalic";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testNoColorCodes() {
        String input = "Plain text without colors";
        assertEquals(input, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testAllBasicColors() {
        String input = "§0§1§2§3§4§5§6§7§8§9§a§b§c§d§e§fColors";
        String expected = "&0&1&2&3&4&5&6&7&8&9&a&b&c&d&e&fColors";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testAllFormatCodes() {
        String input = "§l§m§n§o§r§kFormat";
        String expected = "&l&m&n&o&r&kFormat";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testMixedValidAndInvalidHex() {
        String input = "§x§f§f§5§5§5§5Valid §x§gInvalid";
        String expected = "&#ff5555Valid §x§gInvalid";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testHexWithZeros() {
        String input = "§x§0§0§0§0§0§0Black";
        String expected = "&#000000Black";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testMultipleConsecutiveBasicColors() {
        String input = "§a§l§mText";
        String expected = "&a&l&mText";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testSingleCharacter() {
        String input = "§aA";
        String expected = "&aA";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }

    @Test
    void testOnlyColorCodes() {
        String input = "§a§b§c";
        String expected = "&a&b&c";
        assertEquals(expected, FastHexStringDecolorizer.decolorize(input));
    }
}