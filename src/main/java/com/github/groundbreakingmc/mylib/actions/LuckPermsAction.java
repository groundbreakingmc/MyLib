package com.github.groundbreakingmc.mylib.actions;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.utils.luckperms.LuckPermsUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class LuckPermsAction extends Action.ActionExecutor {

    private final String groupName;
    private final Duration duration;

    public LuckPermsAction(Plugin plugin, Colorizer colorizer, String action) {
        super(plugin, colorizer, action);

        final String[] params = action.split(";");
        this.groupName = params[0];
        this.duration = params.length > 1 && !params[1].equals("infinity")
                ? Duration.ofDays(Long.parseLong(params[1]))
                : null;
    }

    @Override
    public void execute(@NotNull Player player) {
        LuckPermsUtils.setPlayerGroup(player.getUniqueId(), this.groupName, this.duration);
    }
}
