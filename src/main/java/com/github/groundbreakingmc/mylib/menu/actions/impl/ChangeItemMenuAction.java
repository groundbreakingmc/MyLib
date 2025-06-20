package com.github.groundbreakingmc.mylib.menu.actions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.menus.Menu;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ChangeItemMenuAction<C extends MenuContext> implements Action<C> {

    private final int slot;
    private final ItemStack newItemStack;

    public ChangeItemMenuAction(int slot, @NotNull ItemStack newItemStack) {
        this.slot = slot;
        this.newItemStack = newItemStack;
    }

    @Override
    public void execute(@NotNull C context) {
        final Menu menu = context.getMenu();
        if (menu != null) {
            menu.getInventory().setItem(this.slot, this.newItemStack);
        }
    }
}
