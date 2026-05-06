package com.github.groundbreakingmc.mylib.config.serializer.bukkit;

import com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException;
import org.bukkit.Sound;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import static org.junit.jupiter.api.Assertions.*;

final class SoundSerializerTest {

    private final SoundSerializer serializer = SoundSerializer.INSTANCE;

    @BeforeEach
    void setup() {
        MockBukkit.mock();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void deserialize_validName_returnsSound() {
        assertEquals(Sound.ENTITY_PLAYER_LEVELUP, this.serializer.deserialize("ENTITY_PLAYER_LEVELUP", "sound"));
    }

    @Test
    void deserialize_unknownName_returnsNull() {
        assertNull(this.serializer.deserialize("NOT_A_SOUND", "sound"));
    }

    // ── type mismatch ─────────────────────────────────────────────────────────

    @Test
    void deserialize_nonString_throws() {
        final SerializerTypeMismatchException ex =
                assertThrows(SerializerTypeMismatchException.class,
                        () -> this.serializer.deserialize(3.14, "sound"));
        assertEquals("sound", ex.getPath());
        assertEquals(String.class, ex.getExpectedType());
        assertEquals(Double.class, ex.getActualType());
    }
}
