package com.github.groundbreakingmc.mylib.config.serializer.bukkit;

import com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class MaterialSerializerTest {

    private final MaterialSerializer serializer = MaterialSerializer.INSTANCE;

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void deserialize_validName_returnsMaterial() {
        assertEquals(Material.STONE, this.serializer.deserialize("STONE", "material"));
    }

    @Test
    void deserialize_unknownName_returnsNull() {
        assertNull(this.serializer.deserialize("NOT_A_MATERIAL", "material"));
    }

    // ── type mismatch ─────────────────────────────────────────────────────────

    @Test
    void deserialize_nonString_throws() {
        final SerializerTypeMismatchException ex =
                assertThrows(SerializerTypeMismatchException.class,
                        () -> this.serializer.deserialize(42, "material"));
        assertEquals("material", ex.getPath());
        assertEquals(String.class, ex.getExpectedType());
        assertEquals(Integer.class, ex.getActualType());
    }
}
