package com.github.groundbreakingmc.mylib.menu.actions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.menus.Menu;
import com.github.groundbreakingmc.mylib.menu.menus.services.MenuService;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class OpenMenuAction<C extends MenuContext> implements Action<C> {

    private Menu.RawMenu menu;
    private String name;
    private MenuService service;

    public OpenMenuAction(@NotNull Menu.RawMenu menu) {
        this.menu = menu;
    }

    public OpenMenuAction(@NotNull String name, @NotNull MenuService service) {
        this.name = name;
        this.service = service;
    }

    @Override
    public void execute(@NotNull C context) {
        final Player player = context.getPlayer();
        if (player == null) {
            return;
        }

        final Menu.RawMenu menu = this.menu != null ? this.menu : this.service.byName(this.name);
        if (menu != null) {
            menu.open(player);
        }
    }
}
