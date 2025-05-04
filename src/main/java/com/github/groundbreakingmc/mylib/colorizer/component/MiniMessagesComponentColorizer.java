package com.github.groundbreakingmc.mylib.colorizer.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Nullable;

public final class MiniMessagesComponentColorizer implements ComponentColorizer {

    @Override
    public Component colorize(@Nullable String message) {
        if (message == null) {
            return null;
        }
        if (message.isEmpty()) {
            return Component.empty();
        }

        return MiniMessage.miniMessage().deserialize(message);
    }
}
