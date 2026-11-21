package com.github.groundbreakingmc.mylib.utils.player.settings;

import lombok.Builder;
import org.jetbrains.annotations.Nullable;

@Builder
public class TitleSettings {
    @Builder.Default
    public final String title = "";
    @Builder.Default
    public final String subtitle = "";
    @Builder.Default
    public final int fadeIn = 10;
    @Builder.Default
    public final int stay = 40;
    @Builder.Default
    public final int fadeOut = 10;

    @Nullable
    public static TitleSettings fromString(final String string) {
        if (string == null) {
            return null;
        }

        final String[] params = string.split(";");
        final TitleSettings.TitleSettingsBuilder builder = TitleSettings.builder();
        builder.title(params[0]);
        if (params.length > 1) {
            builder.subtitle(params[1]);
        }
        if (params.length > 2) {
            builder.fadeIn(Integer.parseInt(params[2]));
        }
        if (params.length > 3) {
            builder.stay(Integer.parseInt(params[2]));
        }
        if (params.length > 4) {
            builder.fadeOut(Integer.parseInt(params[2]));
        }

        return builder.build();
    }

    @Override
    public String toString() {
        return this.title + ";" + this.subtitle + ";" + this.fadeIn + ";" + this.stay + ";" + this.fadeOut;
    }
}
