package com.github.groundbreakingmc.mylib.menu.items.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.conditions.MenuCondition;
import com.github.groundbreakingmc.mylib.menu.items.MenuItem;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MenuItemGroup implements MenuItem {

    private final List<MenuItem> items;

    private MenuItemGroup(@NotNull List<MenuItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.items = items;
    }

    @Override
    public @Nullable ItemStack getDisplay() {
        return null;
    }

    @Override
    public boolean canSee(@NotNull MenuContext context) {
        return false;
    }

    @Override
    public @Nullable ItemStack getDisplay(@NotNull MenuContext context) {
        final MenuItem menuItem = this.getMenuItemByPriority(context);
        return menuItem != null ? menuItem.getDisplay(context) : null;
    }

    @Override
    public int getPriority() {
        return -1;
    }

    @Override
    public @NotNull Map<ClickType, List<Action<MenuContext>>> getClickActions() {
        return Map.of();
    }

    @Override
    public @NotNull Set<MenuCondition<MenuContext>> getSeeConditions() {
        return Set.of();
    }

    @Override
    public @NotNull Map<ClickType, Set<MenuCondition<MenuContext>>> getClickConditions() {
        return Map.of();
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event, @NotNull MenuContext context) {
        final MenuItem menuItem = this.getMenuItemByPriority(context);
        if (menuItem != null) {
            menuItem.handleClick(event, context);
        }
    }

    private MenuItem getMenuItemByPriority(@NotNull MenuContext context) {
        outside_loop:
        for (final MenuItem menuItem : this.items) {
            for (final MenuCondition<MenuContext> condition : menuItem.getSeeConditions()) {
                if (!condition.test(context)) {
                    continue outside_loop;
                }

            }
            return menuItem;
        }
        return null;
    }

    public static class MenuItemGroupBuilder implements MenuItem.MenuItemGroupBuilder {

        private final List<MenuItem> builder = new ObjectArrayList<>();

        @Override
        public MenuItem.MenuItemGroupBuilder add(@NotNull MenuItem menuItem) {
            return this.add(menuItem, menuItem.getPriority());
        }

        @Override
        public MenuItem.MenuItemGroupBuilder add(@NotNull MenuItem menuItem, int priority) {
            if (menuItem instanceof MenuItemGroup) {
                throw new IllegalArgumentException();
            }
            this.builder.add(Math.min(priority, this.builder.size()), menuItem);
            return this;
        }

        @Override
        public MenuItem build() {
            return new MenuItemGroup(ImmutableList.copyOf(this.builder));
        }
    }
}
