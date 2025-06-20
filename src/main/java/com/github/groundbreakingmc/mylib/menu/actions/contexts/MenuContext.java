package com.github.groundbreakingmc.mylib.menu.actions.contexts;

import com.github.groundbreakingmc.mylib.actions.context.ActionContext;
import com.github.groundbreakingmc.mylib.menu.menus.Menu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class MenuContext implements ActionContext {

    private final Player player;
    private final Player placeholderPlayer;
    private final Menu menu;

    public MenuContext(@Nullable Player player) {
        this.player = player;
        this.placeholderPlayer = player;
        this.menu = null;
    }

    public MenuContext(@Nullable Player player, @Nullable Menu menu) {
        this.player = player;
        this.placeholderPlayer = player;
        this.menu = menu;
    }

    public MenuContext(@Nullable Player player, @Nullable Player placeholderPlayer, @Nullable Menu menu) {
        this.player = player;
        this.menu = menu;
        this.placeholderPlayer = placeholderPlayer != null
                ? placeholderPlayer
                : player;
    }

    @Override
    public @Nullable Player getPlayer() {
        return this.player;
    }

    @Override
    public @Nullable Player getPlaceholderPlayer() {
        return this.placeholderPlayer;
    }

    public @Nullable Menu getMenu() {
        return this.menu;
    }
}
