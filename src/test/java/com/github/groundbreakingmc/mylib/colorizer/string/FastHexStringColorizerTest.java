package com.github.groundbreakingmc.mylib.colorizer.string;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FastHexStringColorizerTest {

    private FastHexStringColorizer colorizer;

    @BeforeEach
    void setUp() {
        this.colorizer = new FastHexStringColorizer();
    }

    @Test
    void testNullMessage() {
        assertNull(this.colorizer.colorize(null));
    }

    @Test
    void testEmptyMessage() {
        assertEquals("", this.colorizer.colorize(""));
    }

    @Test
    void testBasicHexColor() {
        String input = "&#ff5555Hello";
        String expected = "§x§f§f§5§5§5§5Hello";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testMultipleHexColors() {
        String input = "&#ff5555Hello &#00ff00World";
        String expected = "§x§f§f§5§5§5§5Hello §x§0§0§f§f§0§0World";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testUppercaseHexColor() {
        String input = "&#FF5555Text";
        String expected = "§x§F§F§5§5§5§5Text";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testMixedCaseHexColor() {
        String input = "&#Ff5A3bTest";
        String expected = "§x§F§f§5§A§3§bTest";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testInvalidHexColorTooShort() {
        String input = "&#ff55Short";
        assertEquals(input, this.colorizer.colorize(input));
    }

    @Test
    void testInvalidHexColorNonHexChars() {
        String input = "&#gghhiiInvalid";
        assertEquals(input, this.colorizer.colorize(input));
    }

    @Test
    void testBasicColorCodes() {
        String input = "&aGreen &cRed";
        String expected = "§aGreen §cRed";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testFormatCodes() {
        String input = "&lBold &mStrike &nUnderline";
        String expected = "§lBold §mStrike §nUnderline";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testAmpersandXFormat() {
        String input = "&x&f&f&0&0&0&0Yellow";
        String expected = "§x§f§f§0§0§0§0Yellow";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testMixedHexAndBasicColors() {
        String input = "&aGreen &#ff0000Red &bBlue";
        String expected = "§aGreen §x§f§f§0§0§0§0Red §bBlue";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testMixedHexFormats() {
        String input = "&#ff5555Hash &x&0&0&f&f&0&0Ampersand";
        String expected = "§x§f§f§5§5§5§5Hash §x§0§0§f§f§0§0Ampersand";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testConsecutiveHexColors() {
        String input = "&#ff0000&#00ff00&#0000ff";
        String expected = "§x§f§f§0§0§0§0§x§0§0§f§f§0§0§x§0§0§0§0§f§f";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testHexColorAtEnd() {
        String input = "Text&#ffffff";
        String expected = "Text§x§f§f§f§f§f§f";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testInvalidAmpersandXTooShort() {
        String input = "&x&f&fShort";
        String expected = "&x§f§fShort";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testStandaloneAmpersand() {
        String input = "Test & text";
        assertEquals(input, this.colorizer.colorize(input));
    }

    @Test
    void testComplexMessage() {
        String input = "&l&a&#00ff00Bold Green &#ff0000Red &r&oItalic";
        String expected = "§l§a§x§0§0§f§f§0§0Bold Green §x§f§f§0§0§0§0Red §r§oItalic";
        assertEquals(expected, this.colorizer.colorize(input));
    }

    @Test
    void testNoColorCodes() {
        String input = "Plain text without colors";
        assertEquals(input, this.colorizer.colorize(input));
    }
}