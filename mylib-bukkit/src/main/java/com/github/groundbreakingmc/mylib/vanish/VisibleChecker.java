package com.github.groundbreakingmc.mylib.vanish;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface VisibleChecker {

    @SuppressWarnings("unused")
    boolean canSee(@NotNull Player viewer, @NotNull Player target);
}
