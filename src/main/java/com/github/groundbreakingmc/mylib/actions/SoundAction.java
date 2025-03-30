package com.github.groundbreakingmc.mylib.actions;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SoundAction extends Action.ActionExecutor {

    private final SoundSettings settings;

    public SoundAction(Plugin plugin, Colorizer colorizer, String action) {
        super(plugin, colorizer, action);

        this.settings = SoundSettings.fromString(action);
    }

    @Override
    public void execute(@NotNull Player player) {
        PlayerUtils.playSound(player, this.settings);
    }
}
