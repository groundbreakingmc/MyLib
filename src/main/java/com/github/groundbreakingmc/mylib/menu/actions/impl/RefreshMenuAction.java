package com.github.groundbreakingmc.mylib.menu.actions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.menus.Menu;
import org.jetbrains.annotations.NotNull;

public final class RefreshMenuAction<C extends MenuContext> implements Action<C> {

    private final int slot;

    public RefreshMenuAction(int slot) {
        this.slot = slot;
    }

    @Override
    public void execute(@NotNull C context) {
        final Menu menu = context.getMenu();
        if (menu != null) {
            if (this.slot > 0) {
                menu.refresh(this.slot, context);
            } else {
                menu.refresh(context);
            }
        }
    }
}
