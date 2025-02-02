package com.github.groundbreakingmc.mylib.utils.player.settings;

import lombok.Builder;
import org.bukkit.Sound;
import org.jetbrains.annotations.Nullable;

@Builder
public class SoundSettings {
    public final Sound sound;
    @Builder.Default
    public final float volume = 1.0f;
    @Builder.Default
    public final float pitch = 1.0f;

    @Nullable
    public static SoundSettings get(final String string) {
        if (string == null) {
            return null;
        }

        final String[] params = string.split(";");
        final SoundSettingsBuilder builder = builder();
        builder.sound(Sound.valueOf(params[0].toUpperCase()));
        if (params.length > 1) {
            builder.volume(Float.parseFloat(params[1]));
        }
        if (params.length > 2) {
            builder.pitch(Float.parseFloat(params[2]));
        }

        return builder.build();
    }

    @Override
    public String toString() {
        return this.sound.name() + ";" + this.volume + ";" + this.pitch;
    }
}
