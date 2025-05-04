package com.github.groundbreakingmc.mylib.colorizer;

import com.github.groundbreakingmc.mylib.colorizer.component.ComponentColorizer;
import com.github.groundbreakingmc.mylib.colorizer.component.MiniMessagesComponentColorizer;
import com.github.groundbreakingmc.mylib.colorizer.component.VanillaComponentColorizer;
import com.github.groundbreakingmc.mylib.colorizer.legacy.*;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class ColorizerFactory {

    @Nullable
    public static ComponentColorizer createComponentColorizer(final String mode) {
        if (mode == null) {
            return null;
        }

        return switch (mode.toUpperCase()) {
            case "MINI_MESSAGES", "MINIMESSAGES" -> new MiniMessagesComponentColorizer();
            default -> new VanillaComponentColorizer();
        };
    }

    @Nullable
    public static StringColorizer createStringColorizer(final String mode) {
        if (mode == null) {
            return null;
        }

        return switch (mode.toUpperCase()) {
            case "LEGACY" -> new LegacyStringColorizer();
            case "LEGACY_ADVANCED" -> new LegacyAdvancedColorizer();
            case "MINI_MESSAGES", "MINIMESSAGES" -> new MiniMessagesStringColorizer();
            default -> new VanillaStringColorizer();
        };
    }

    @Nullable
    @Deprecated
    public static StringColorizer createColorizer(final String mode) {
        return createStringColorizer(mode);
    }
}
