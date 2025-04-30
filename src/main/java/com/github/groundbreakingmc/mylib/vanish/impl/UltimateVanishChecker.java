package com.github.groundbreakingmc.mylib.vanish.impl;

import com.github.groundbreakingmc.mylib.vanish.VisibleChecker;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class UltimateVanishChecker implements VisibleChecker {

    @Override
    public boolean canSee(@NotNull Player viewer, @NotNull Player target) {
        return false;
    }
}
