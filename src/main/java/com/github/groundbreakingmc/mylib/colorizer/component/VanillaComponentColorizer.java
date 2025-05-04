package com.github.groundbreakingmc.mylib.colorizer.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;

public final class VanillaComponentColorizer implements ComponentColorizer {

    @Override
    public Component colorize(@Nullable String message) {
        if (message == null) {
            return Component.text("null");
        }
        if (message.isEmpty()) {
            return Component.empty();
        }

        return LegacyComponentSerializer.legacySection().deserialize(message);
    }
}
