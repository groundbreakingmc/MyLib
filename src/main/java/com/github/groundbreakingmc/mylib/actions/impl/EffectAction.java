package com.github.groundbreakingmc.mylib.actions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.actions.context.ActionContext;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import com.github.groundbreakingmc.mylib.utils.player.settings.EffectSettings;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class EffectAction<C extends ActionContext> implements Action<C> {

    private final EffectSettings effectSettings;

    public EffectAction(@NotNull String action) {
        this.effectSettings = EffectSettings.fromString(action);
    }

    @Override
    public void execute(@NotNull C context) {
        final Player player = context.getPlayer();
        if (player != null) {
            PlayerUtils.addPotionEffect(player, this.effectSettings);
        }
    }
}
