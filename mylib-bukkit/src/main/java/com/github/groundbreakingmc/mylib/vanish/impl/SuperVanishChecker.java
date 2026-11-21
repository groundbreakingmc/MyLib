package com.github.groundbreakingmc.mylib.vanish.impl;

import com.github.groundbreakingmc.mylib.vanish.VisibleChecker;
import de.myzelyam.api.vanish.VanishAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SuperVanishChecker implements VisibleChecker {

    @Override
    public boolean canSee(@NotNull Player viewer, @NotNull Player target) {
        return VanishAPI.canSee(viewer, target);
    }
}
