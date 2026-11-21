package com.github.groundbreakingmc.mylib.vanish.impl;

import com.github.groundbreakingmc.mylib.utils.player.PlayerUtils;
import com.github.groundbreakingmc.mylib.vanish.VisibleChecker;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PermissionChecker implements VisibleChecker {

    private final String bypass;

    public PermissionChecker(@NotNull String bypass) {
        this.bypass = bypass;
    }

    @Override
    public boolean canSee(@NotNull Player viewer, @NotNull Player target) {
        return !PlayerUtils.isVanished(target) || viewer.hasPermission(bypass);
    }
}
