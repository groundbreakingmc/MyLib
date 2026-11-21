package com.github.groundbreakingmc.mylib.config.serializer.bukkit;

import com.github.groundbreakingmc.mylib.config.ConfigSerializer;
import com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public final class PotionEffectTypeSerializer implements ConfigSerializer<PotionEffectType> {

    public static final PotionEffectTypeSerializer INSTANCE = new PotionEffectTypeSerializer();

    private PotionEffectTypeSerializer() {
    }

    @Override
    @Nullable
    @SuppressWarnings("deprecation")
    public PotionEffectType deserialize(Object value, String path) {
        if (!(value instanceof String s)) {
            throw new SerializerTypeMismatchException(path, String.class, value.getClass());
        }
        return PotionEffectType.getByName(s);
    }
}
