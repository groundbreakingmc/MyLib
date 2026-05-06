package com.github.groundbreakingmc.mylib.config.serializer.bukkit;

import com.github.groundbreakingmc.mylib.config.ConfigSerializer;
import com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Deserializes a {@link Location} from a config section represented as {@link Map Map&lt;?,?&gt;}.
 *
 * <p>Expected map shape:
 * <pre>
 * world: world
 * x: 100.0
 * y: 64.0
 * z: 200.0
 * yaw: 0.0    # optional, defaults to 0
 * pitch: 0.0  # optional, defaults to 0
 * </pre>
 *
 * <p>Throws {@link SerializerTypeMismatchException} if the raw value is not a {@link Map}.
 */
public final class LocationSerializer implements ConfigSerializer<Location> {

    public static final LocationSerializer INSTANCE = new LocationSerializer();

    private LocationSerializer() {
    }

    @Override
    @Nullable
    public Location deserialize(Object value, String path) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new SerializerTypeMismatchException(path, Map.class, value.getClass());
        }

        final Object worldRaw = map.get("world");
        if (!(worldRaw instanceof String worldName)) return null;

        final World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        return new Location(
                world,
                doubleVal(map, "x"),
                doubleVal(map, "y"),
                doubleVal(map, "z"),
                (float) doubleVal(map, "yaw"),
                (float) doubleVal(map, "pitch")
        );
    }

    private static double doubleVal(Map<?, ?> map, String key) {
        final Object v = map.get(key);
        if (v instanceof Number numb) return numb.doubleValue();
        return 0.0;
    }
}
