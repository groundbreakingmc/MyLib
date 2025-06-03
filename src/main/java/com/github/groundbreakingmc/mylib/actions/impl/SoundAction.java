package com.github.groundbreakingmc.mylib.actions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.actions.context.ActionContext;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class SoundAction implements Action<ActionContext> {

    private final SoundSettings soundSettings;

    public SoundAction(@NotNull String action) {
        this.soundSettings = SoundSettings.fromString(action);
    }

    @Override
    public void execute(@NotNull ActionContext context) {
        final Player player = context.getPlayer();
        if (player != null) {
            PlayerUtils.playSound(player, this.soundSettings);
        }
    }
}
