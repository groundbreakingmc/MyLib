package com.github.groundbreakingmc.mylib.colorizer;

import org.junit.jupiter.api.Test;

import static com.github.groundbreakingmc.mylib.colorizer.ColorCodesTranslator.*;
import static org.junit.jupiter.api.Assertions.*;

class ColorCodesTranslatorTest {

    @Test
    void testBasicColorCodes() {
        assertEquals("§aHello", translateAlternateColorCodes("&aHello"));
        assertEquals("§cWorld", translateAlternateColorCodes("&cWorld"));
        assertEquals("§0§1§2§3§4§5§6§7§8§9", translateAlternateColorCodes("&0&1&2&3&4&5&6&7&8&9"));
    }

    @Test
    void testFormatCodes() {
        assertEquals("§lBold", translateAlternateColorCodes("&lBold"));
        assertEquals("§mStrike", translateAlternateColorCodes("&mStrike"));
        assertEquals("§nUnderline", translateAlternateColorCodes("&nUnderline"));
        assertEquals("§oItalic", translateAlternateColorCodes("&oItalic"));
        assertEquals("§kObfuscated", translateAlternateColorCodes("&kObfuscated"));
        assertEquals("§rReset", translateAlternateColorCodes("&rReset"));
    }

    @Test
    void testUppercaseConversion() {
        assertEquals("§aHello", translateAlternateColorCodes("&AHello"));
        assertEquals("§lBold", translateAlternateColorCodes("&LBold"));
    }

    @Test
    void testValidHexCode() {
        assertEquals("§x§f§f§0§0§0§0Yellow", translateAlternateColorCodes("&x&f&f&0&0&0&0Yellow"));
    }

    @Test
    void testValidHexCodeUppercase() {
        assertEquals("§x§f§f§a§a§b§bBlue", translateAlternateColorCodes("&X&F&F&A&A&B&BBlue"));
    }

    @Test
    void testInvalidHexCodeTooShort() {
        assertEquals("&x§f§fShort", translateAlternateColorCodes("&x&f&fShort"));
    }

    @Test
    void testInvalidHexCodeWrongPattern() {
        assertEquals("&x§r§r&g&g§b§bInvalid", translateAlternateColorCodes("&x&r&r&g&g&b&bInvalid"));
    }

    @Test
    void testMixedCodesAndHex() {
        assertEquals("§aGreen §x§f§f§0§0§0§0Yellow §cRed", translateAlternateColorCodes("&aGreen &x&f&f&0&0&0&0Yellow &cRed"));
    }

    @Test
    void testStandaloneAmpersand() {
        assertEquals("Test & text", translateAlternateColorCodes("Test & text"));
        assertEquals("&", translateAlternateColorCodes("&"));
    }

    @Test
    void testInvalidColorCode() {
        assertEquals("&gInvalid", translateAlternateColorCodes("&gInvalid"));
        assertEquals("&zTest", translateAlternateColorCodes("&zTest"));
    }

    @Test
    void testStandaloneXNotConverted() {
        assertEquals("&xTest", translateAlternateColorCodes("&xTest"));
        assertEquals("&x", translateAlternateColorCodes("&x"));
    }

    @Test
    void testEmptyString() {
        assertEquals("", translateAlternateColorCodes(""));
    }

    @Test
    void testMultipleConsecutiveCodes() {
        assertEquals("§a§l§nText", translateAlternateColorCodes("&a&l&nText"));
    }

    @Test
    void testIsColorCharacter() {
        assertTrue(isColorCharacter('a'));
        assertTrue(isColorCharacter('F'));
        assertTrue(isColorCharacter('l'));
        assertTrue(isColorCharacter('r'));
        assertFalse(isColorCharacter('x'));
        assertFalse(isColorCharacter('g'));
    }

    @Test
    void testIsHexCharacter() {
        assertTrue(isHexCharacter('0'));
        assertTrue(isHexCharacter('9'));
        assertTrue(isHexCharacter('a'));
        assertTrue(isHexCharacter('F'));
        assertFalse(isHexCharacter('g'));
        assertFalse(isHexCharacter('x'));
    }
}