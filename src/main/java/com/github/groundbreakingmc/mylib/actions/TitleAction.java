package com.github.groundbreakingmc.mylib.actions;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import com.github.groundbreakingmc.mylib.utils.player.settings.TitleSettings;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class TitleAction extends Action.ActionExecutor {

    private final TitleSettings settings;

    public TitleAction(Plugin plugin, Colorizer colorizer, String action) {
        super(plugin, colorizer, action);

        this.settings = TitleSettings.fromString(super.action);
    }

    @Override
    public void execute(@NotNull Player player) {
        PlayerUtils.showTitle(player, this.settings);
    }
}
