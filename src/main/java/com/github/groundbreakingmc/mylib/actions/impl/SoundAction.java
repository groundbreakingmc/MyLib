package com.github.groundbreakingmc.mylib.actions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.actions.context.ActionContext;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class SoundAction<C extends ActionContext> implements Action<C> {

    private final SoundSettings soundSettings;

    public SoundAction(@NotNull String action) {
        this.soundSettings = SoundSettings.fromString(action);
    }

    @Override
    public void execute(@NotNull C context) {
        final Player player = context.getPlayer();
        if (player != null) {
            PlayerUtils.playSound(player, this.soundSettings);
        }
    }
}
