package com.github.groundbreakingmc.mylib.config.serializer.bukkit;

import com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

final class WorldSerializerTest {

    private final WorldSerializer serializer = WorldSerializer.INSTANCE;

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void deserialize_knownWorld_returnsWorld() {
        final World world = mock(World.class);
        try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);
            assertSame(world, this.serializer.deserialize("world", "world-path"));
        }
    }

    @Test
    void deserialize_unknownWorld_returnsNull() {
        try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("ghost")).thenReturn(null);
            assertNull(this.serializer.deserialize("ghost", "world-path"));
        }
    }

    // ── type mismatch ─────────────────────────────────────────────────────────

    @Test
    void deserialize_nonString_throws() {
        final SerializerTypeMismatchException ex =
                assertThrows(SerializerTypeMismatchException.class,
                        () -> this.serializer.deserialize(true, "world-path"));
        assertEquals("world-path", ex.getPath());
        assertEquals(String.class, ex.getExpectedType());
        assertEquals(Boolean.class, ex.getActualType());
    }
}
