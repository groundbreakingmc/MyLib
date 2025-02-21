package com.github.groundbreakingmc.mylib.actions;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class EffectAllAction extends EffectAction {

    public EffectAllAction(Plugin plugin, Colorizer colorizer, String action) {
        super(plugin, colorizer, action);
    }

    @Override
    public void execute(@NotNull Player player) {
        for (final Player target : Bukkit.getOnlinePlayers()) {
            super.execute(target);
        }
    }
}
