package com.github.groundbreakingmc.mylib.config.serializer.bukkit;

import com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class PotionEffectTypeSerializerTest {

    private final PotionEffectTypeSerializer serializer = PotionEffectTypeSerializer.INSTANCE;

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void deserialize_validName_returnsEffect() {
        assertEquals(PotionEffectType.SPEED, this.serializer.deserialize("SPEED", "effect"));
    }

    @Test
    void deserialize_unknownName_returnsNull() {
        assertNull(this.serializer.deserialize("NOT_AN_EFFECT", "effect"));
    }

    // ── type mismatch ─────────────────────────────────────────────────────────

    @Test
    void deserialize_nonString_throws() {
        final SerializerTypeMismatchException ex =
                assertThrows(SerializerTypeMismatchException.class,
                        () -> this.serializer.deserialize(List.of(), "effect"));
        assertEquals("effect", ex.getPath());
        assertEquals(String.class, ex.getExpectedType());
    }
}
