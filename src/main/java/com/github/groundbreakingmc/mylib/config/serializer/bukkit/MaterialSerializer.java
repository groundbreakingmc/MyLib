package com.github.groundbreakingmc.mylib.config.serializer.bukkit;

import com.github.groundbreakingmc.mylib.config.ConfigSerializer;
import com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public final class MaterialSerializer implements ConfigSerializer<Material> {

    public static final MaterialSerializer INSTANCE = new MaterialSerializer();

    private MaterialSerializer() {
    }

    @Override
    @Nullable
    public Material deserialize(Object value, String path) {
        if (!(value instanceof String s)) {
            throw new SerializerTypeMismatchException(path, String.class, value.getClass());
        }
        return Material.getMaterial(s);
    }
}
