package com.github.groundbreakingmc.mylib.menu.actions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.items.MenuItem;
import com.github.groundbreakingmc.mylib.menu.menus.Menu;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;

public final class FillMenuAction<C extends MenuContext> implements Action<C> {

    private final Function<C, MenuItem> factory;
    private final IntSet slots;

    public FillMenuAction(@NotNull MenuItem menuItem, @NotNull Set<Integer> slots) {
        this.factory = (context) -> menuItem;
        this.slots = new IntOpenHashSet(slots);
    }

    public FillMenuAction(@NotNull Function<C, MenuItem> factory, @NotNull Set<Integer> slots) {
        this.factory = factory;
        this.slots = new IntOpenHashSet(slots);
    }

    @Override
    public void execute(@NotNull C context) {
        final Player player = context.getPlayer();
        final Menu menu = context.getMenu();
        if (player == null || menu == null) {
            return;
        }

        for (final int i : this.slots) {
            menu.setItem(i, this.factory.apply(context), context);
        }
    }
}
