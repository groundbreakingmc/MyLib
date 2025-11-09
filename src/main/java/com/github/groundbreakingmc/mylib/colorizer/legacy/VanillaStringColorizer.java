package com.github.groundbreakingmc.mylib.colorizer.legacy;

import com.github.groundbreakingmc.mylib.colorizer.ColorCodesTranslator;
import org.jetbrains.annotations.Nullable;

public final class VanillaStringColorizer implements StringColorizer {

    @Override
    public String colorize(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        return ColorCodesTranslator.translateAlternateColorCodes(message);
    }
}
