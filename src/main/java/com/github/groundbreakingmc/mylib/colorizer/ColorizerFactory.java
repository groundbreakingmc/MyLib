package com.github.groundbreakingmc.mylib.colorizer;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class ColorizerFactory {

    @Nullable
    public static Colorizer createColorizer(final String mode) {
        if (mode == null) {
            return null;
        }

        switch (mode.toUpperCase()) {
            case "LEGACY":
                return new LegacyColorizer();
            case "LEGACY_ADVANCED":
                return new LegacyAdvancedColorizer();
            case "MINI_MESSAGES":
            case "MINIMESSAGES":
                return new MiniMessagesColorizer();
            default:
                return new VanillaColorizer();
        }
    }
}
