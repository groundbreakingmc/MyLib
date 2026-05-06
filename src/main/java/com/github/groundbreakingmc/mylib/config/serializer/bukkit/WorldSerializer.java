package com.github.groundbreakingmc.mylib.config.serializer.bukkit;

import com.github.groundbreakingmc.mylib.config.ConfigSerializer;
import com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

public final class WorldSerializer implements ConfigSerializer<World> {

    public static final WorldSerializer INSTANCE = new WorldSerializer();

    private WorldSerializer() {
    }

    @Override
    @Nullable
    public World deserialize(Object value, String path) {
        if (!(value instanceof String s)) {
            throw new SerializerTypeMismatchException(path, String.class, value.getClass());
        }
        return Bukkit.getWorld(s);
    }
}
