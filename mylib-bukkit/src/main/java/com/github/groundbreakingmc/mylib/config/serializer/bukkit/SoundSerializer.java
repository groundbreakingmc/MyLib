package com.github.groundbreakingmc.mylib.config.serializer.bukkit;

import com.github.groundbreakingmc.mylib.config.ConfigSerializer;
import com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException;
import org.bukkit.Sound;
import org.jetbrains.annotations.Nullable;

public final class SoundSerializer implements ConfigSerializer<Sound> {

    public static final SoundSerializer INSTANCE = new SoundSerializer();

    private SoundSerializer() {
    }

    @Override
    @Nullable
    public Sound deserialize(Object value, String path) {
        if (!(value instanceof String s)) {
            throw new SerializerTypeMismatchException(path, String.class, value.getClass());
        }
        try {
            return Sound.valueOf(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
