package com.github.groundbreakingmc.mylib.config.serializer.bukkit;

import com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

final class LocationSerializerTest {

    private final LocationSerializer serializer = LocationSerializer.INSTANCE;

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void deserialize_validMap_returnsLocation() {
        final World world = mock(World.class);
        try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);

            final Map<String, Object> map = Map.of(
                    "world", "world",
                    "x", 1.0,
                    "y", 64.0,
                    "z", -1.0,
                    "yaw", 90.0,
                    "pitch", 10.0
            );

            final Location location = serializer.deserialize(map, "spawn");

            assertNotNull(location);
            assertSame(world, location.getWorld());
            assertEquals(1.0, location.getX(), 1e-9);
            assertEquals(64.0, location.getY(), 1e-9);
            assertEquals(-1.0, location.getZ(), 1e-9);
            assertEquals(90f, location.getYaw(), 1e-4f);
            assertEquals(10f, location.getPitch(), 1e-4f);
        }
    }

    @Test
    void deserialize_missingYawAndPitch_defaultToZero() {
        final World world = mock(World.class);
        try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);

            final Map<String, Object> map = Map.of("world", "world", "x", 0.0, "y", 0.0, "z", 0.0);
            final Location location = serializer.deserialize(map, "spawn");

            assertNotNull(location);
            assertEquals(0f, location.getYaw(), 1e-4f);
            assertEquals(0f, location.getPitch(), 1e-4f);
        }
    }

    @Test
    void deserialize_integerCoords_handledAsNumber() {
        final World world = mock(World.class);
        try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);

            // YAML often gives ints for whole numbers — must not throw
            final Map<String, Object> map = Map.of("world", "world", "x", 10, "y", 64, "z", -5);
            final Location location = serializer.deserialize(map, "spawn");

            assertNotNull(location);
            assertEquals(10.0, location.getX(), 1e-9);
            assertEquals(64.0, location.getY(), 1e-9);
            assertEquals(-5.0, location.getZ(), 1e-9);
        }
    }

    // ── null / absent cases ───────────────────────────────────────────────────

    @Test
    void deserialize_worldKeyMissing_returnsNull() {
        final Location location = this.serializer.deserialize(Map.of("x", 0.0, "y", 0.0, "z", 0.0), "spawn");
        assertNull(location);
    }

    @Test
    void deserialize_worldNotFound_returnsNull() {
        try (final MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("ghost")).thenReturn(null);

            final Map<String, Object> map = Map.of("world", "ghost", "x", 0.0, "y", 0.0, "z", 0.0);
            assertNull(this.serializer.deserialize(map, "spawn"));
        }
    }

    // ── type mismatch ─────────────────────────────────────────────────────────

    @Test
    void deserialize_notAMap_throws() {
        final SerializerTypeMismatchException ex =
                assertThrows(SerializerTypeMismatchException.class,
                        () -> this.serializer.deserialize("not-a-map", "spawn"));
        assertEquals("spawn", ex.getPath());
        assertEquals(Map.class, ex.getExpectedType());
        assertEquals(String.class, ex.getActualType());
    }
}
