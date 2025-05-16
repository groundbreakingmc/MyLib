package com.github.groundbreakingmc.mylib.colorizer;

import com.github.groundbreakingmc.mylib.colorizer.component.ComponentColorizer;
import com.github.groundbreakingmc.mylib.colorizer.component.MiniMessagesComponentColorizer;
import com.github.groundbreakingmc.mylib.colorizer.component.VanillaComponentColorizer;
import com.github.groundbreakingmc.mylib.colorizer.legacy.*;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class ColorizerFactory {

    @NotNull
    public static ComponentColorizer createComponentColorizer(@NotNull String mode) {
        return switch (mode.toUpperCase()) {
            case "MINI_MESSAGES", "MINIMESSAGES" -> new MiniMessagesComponentColorizer();
            default -> new VanillaComponentColorizer(createStringColorizer(mode));
        };
    }

    @NotNull
    public static StringColorizer createStringColorizer(@NotNull String mode) {
        return switch (mode.toUpperCase()) {
            case "LEGACY" -> new LegacyStringColorizer();
            case "LEGACY_ADVANCED" -> new LegacyAdvancedColorizer();
            case "MINI_MESSAGES", "MINIMESSAGES" -> new MiniMessagesStringColorizer();
            default -> new VanillaStringColorizer();
        };
    }

    @Deprecated
    public static StringColorizer createColorizer(final String mode) {
        return createStringColorizer(mode);
    }
}
