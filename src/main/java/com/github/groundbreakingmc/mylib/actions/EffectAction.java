package com.github.groundbreakingmc.mylib.actions;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import com.github.groundbreakingmc.mylib.utils.player.settings.EffectSettings;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class EffectAction extends Action.ActionExecutor {

    private final EffectSettings effect;

    public EffectAction(Plugin plugin, Colorizer colorizer, String action) {
        super(plugin, colorizer, action);
        this.effect = EffectSettings.get(action);
    }

    @Override
    public void execute(@NotNull Player player) {
        PlayerUtils.addPotionEffect(player, this.effect);
    }
}
