package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.config.exception.ConfigMissingPathException;
import com.github.groundbreakingmc.mylib.config.exception.SerializerNotFoundException;
import com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class ConfigTest {

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private Config config;

    @BeforeEach
    void setUp() {
        final Map<String, Object> data = Map.of(
                "name", "test",
                "enabled", true,
                "count", 42,
                "limit", 100L,
                "ratio", 3.14,
                "tags", List.of("a", "b", "c"),
                "section", Map.of("nested", "value")
        );
        config = new Config(new MapConfigSource(data), new SerializerRegistry());
    }

    // ── String ────────────────────────────────────────────────────────────────

    @Test
    void findStr_returnsValue() {
        assertEquals("test", config.findStr("name"));
    }

    @Test
    void findStr_missingPath_throws() {
        assertThrows(ConfigMissingPathException.class, () -> config.findStr("missing"));
    }

    @Test
    void strOr_returnsValue() {
        assertEquals("test", config.strOr("name", "fallback"));
    }

    @Test
    void strOr_missingPath_returnsDefault() {
        assertEquals("fallback", config.strOr("missing", "fallback"));
    }

    // ── Boolean ───────────────────────────────────────────────────────────────

    @Test
    void findBool_returnsValue() {
        assertTrue(config.findBool("enabled"));
    }

    @Test
    void findBool_missingPath_throws() {
        assertThrows(ConfigMissingPathException.class, () -> config.findBool("missing"));
    }

    @Test
    void boolOr_missingPath_returnsDefault() {
        assertFalse(config.boolOr("missing", false));
    }

    // ── Integer ───────────────────────────────────────────────────────────────

    @Test
    void findInt_returnsValue() {
        assertEquals(42, config.findInt("count"));
    }

    @Test
    void findInt_missingPath_throws() {
        assertThrows(ConfigMissingPathException.class, () -> config.findInt("missing"));
    }

    @Test
    void intOr_missingPath_returnsDefault() {
        assertEquals(-1, config.intOr("missing", -1));
    }

    // ── Long ──────────────────────────────────────────────────────────────────

    @Test
    void findLong_returnsValue() {
        assertEquals(100L, config.findLong("limit"));
    }

    @Test
    void longOr_missingPath_returnsDefault() {
        assertEquals(-1L, config.longOr("missing", -1L));
    }

    // ── Double ────────────────────────────────────────────────────────────────

    @Test
    void findDouble_returnsValue() {
        assertEquals(3.14, config.findDouble("ratio"), 1e-9);
    }

    @Test
    void doubleOr_missingPath_returnsDefault() {
        assertEquals(0.0, config.doubleOr("missing", 0.0), 1e-9);
    }

    // ── List ──────────────────────────────────────────────────────────────────

    @Test
    void findList_returnsValue() {
        assertEquals(List.of("a", "b", "c"), config.findList("tags"));
    }

    @Test
    void findList_missingPath_throws() {
        assertThrows(ConfigMissingPathException.class, () -> config.findList("missing"));
    }

    @Test
    void listOr_missingPath_returnsDefault() {
        final List<?> def = List.of("x");
        assertEquals(def, config.listOr("missing", def));
    }

    // ── Section ───────────────────────────────────────────────────────────────

    @Test
    void findSection_returnsWrappedChild() {
        final Config section = config.findSection("section");
        assertEquals("value", section.findStr("nested"));
    }

    @Test
    void findSection_missingPath_throws() {
        assertThrows(ConfigMissingPathException.class, () -> config.findSection("missing"));
    }

    @Test
    void sectionOr_missingPath_returnsDefault() {
        final Config def = new Config(new MapConfigSource(Map.of()), new SerializerRegistry());
        assertSame(def, config.sectionOr("missing", def));
    }

    // ── Custom typed ──────────────────────────────────────────────────────────

    @Test
    void get_noSerializer_throws() {
        assertThrows(SerializerNotFoundException.class, () -> config.get(Integer.class, "count"));
    }

    @Test
    void get_missingPath_returnsNull() {
        final SerializerRegistry registry = new SerializerRegistry();
        registry.register(String.class, (value, path) -> value.toString());
        final Config cfg = new Config(new MapConfigSource(Map.of()), registry);
        assertNull(cfg.get(String.class, "missing"));
    }

    @Test
    void get_typeMismatch_throwsFromSerializer() {
        final SerializerRegistry registry = new SerializerRegistry();
        registry.register(String.class, (value, path) -> {
            if (!(value instanceof String s)) {
                throw new SerializerTypeMismatchException(path, String.class, value.getClass());
            }
            return s;
        });
        final Config cfg = new Config(new MapConfigSource(Map.of("key", 123)), registry);
        final SerializerTypeMismatchException ex =
                assertThrows(SerializerTypeMismatchException.class, () -> cfg.get(String.class, "key"));
        assertEquals("key", ex.getPath());
        assertEquals(String.class, ex.getExpectedType());
        assertEquals(Integer.class, ex.getActualType());
    }

    @Test
    void find_returnsValue() {
        final SerializerRegistry registry = new SerializerRegistry();
        registry.register(String.class, (value, path) -> value.toString());
        final Config cfg = new Config(new MapConfigSource(Map.of("key", "hello")), registry);
        assertEquals("hello", cfg.find(String.class, "key"));
    }

    @Test
    void find_serializerReturnsNull_throws() {
        final SerializerRegistry registry = new SerializerRegistry();
        registry.register(String.class, (value, path) -> null);
        final Config cfg = new Config(new MapConfigSource(Map.of("key", "hello")), registry);
        assertThrows(ConfigMissingPathException.class, () -> cfg.find(String.class, "key"));
    }

    @Test
    void getOr_serializerReturnsNull_returnsDefault() {
        final SerializerRegistry registry = new SerializerRegistry();
        registry.register(String.class, (value, path) -> null);
        final Config cfg = new Config(new MapConfigSource(Map.of("key", "hello")), registry);
        assertEquals("fallback", cfg.getOr(String.class, "key", "fallback"));
    }

    // ── List of custom typed ──────────────────────────────────────────────────

    @Test
    void findListOf_deserializesAllElements() {
        final SerializerRegistry registry = new SerializerRegistry();
        registry.register(String.class, (value, path) -> value.toString().toUpperCase());
        final Config cfg = new Config(
                new MapConfigSource(Map.of("keys", List.of("a", "b", "c"))),
                registry
        );
        assertEquals(List.of("A", "B", "C"), cfg.findListOf(String.class, "keys"));
    }

    @Test
    void findListOf_skipsNullResults() {
        final SerializerRegistry registry = new SerializerRegistry();
        // returns null for "skip", value otherwise
        registry.register(String.class, (value, path) -> "skip".equals(value) ? null : value.toString());
        final Config cfg = new Config(
                new MapConfigSource(Map.of("keys", List.of("keep", "skip", "keep2"))),
                registry
        );
        assertEquals(List.of("keep", "keep2"), cfg.findListOf(String.class, "keys"));
    }

    @Test
    void findListOf_missingPath_throws() {
        final SerializerRegistry registry = new SerializerRegistry();
        registry.register(String.class, (value, path) -> value.toString());
        final Config cfg = new Config(new MapConfigSource(Map.of()), registry);
        assertThrows(ConfigMissingPathException.class, () -> cfg.findListOf(String.class, "missing"));
    }

    @Test
    void listOfOr_missingPath_returnsDefault() {
        final SerializerRegistry registry = new SerializerRegistry();
        registry.register(String.class, (value, path) -> value.toString());
        final Config cfg = new Config(new MapConfigSource(Map.of()), registry);
        final List<String> def = List.of("x");
        assertEquals(def, cfg.listOfOr(String.class, "missing", def));
    }

    @Test
    void findListOf_noSerializer_throws() {
        final Config cfg = new Config(
                new MapConfigSource(Map.of("keys", List.of("a"))),
                new SerializerRegistry()
        );
        assertThrows(SerializerNotFoundException.class, () -> cfg.findListOf(String.class, "keys"));
    }
}
