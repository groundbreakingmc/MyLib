package com.github.groundbreakingmc.mylib.colorizer.component;

import com.github.groundbreakingmc.mylib.colorizer.legacy.LegacyAdvancedColorizer;
import com.github.groundbreakingmc.mylib.colorizer.legacy.StringColorizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MiniMessagesComponentColorizer implements ComponentColorizer {

    private final StringColorizer colorizer;

    public MiniMessagesComponentColorizer() {
        this.colorizer = new LegacyAdvancedColorizer();
    }

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

    @Override
    public @NotNull StringColorizer getStringColorizer() {
        return this.colorizer;
    }
}
