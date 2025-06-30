package com.github.groundbreakingmc.mylib.colorizer.component;

import com.github.groundbreakingmc.mylib.colorizer.legacy.LegacyAdvancedColorizer;
import com.github.groundbreakingmc.mylib.colorizer.legacy.StringColorizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VanillaComponentColorizer implements ComponentColorizer {

    private final StringColorizer colorizer;

    public VanillaComponentColorizer() {
        this(new LegacyAdvancedColorizer());
    }

    public VanillaComponentColorizer(@NotNull StringColorizer colorizer) {
        this.colorizer = colorizer;
    }

    @Override
    public Component colorize(@Nullable String message) {
        if (message == null) {
            return Component.text("null");
        }
        if (message.isEmpty()) {
            return Component.empty();
        }

        return LegacyComponentSerializer.legacySection().deserialize(
                this.colorizer.colorize(message)
        );
    }

    @Override
    public @NotNull StringColorizer getStringColorizer() {
        return this.colorizer;
    }
}
