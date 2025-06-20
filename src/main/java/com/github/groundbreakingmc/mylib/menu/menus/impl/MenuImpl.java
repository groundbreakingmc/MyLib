package com.github.groundbreakingmc.mylib.menu.menus.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.conditions.MenuCondition;
import com.github.groundbreakingmc.mylib.menu.items.MenuItem;
import com.github.groundbreakingmc.mylib.menu.menus.Menu;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class MenuImpl implements Menu {

    private final String name;
    private final Component title;
    private final int size;
    private final InventoryType type;
    private final boolean handle;
    private final Set<MenuCondition<MenuContext>> openConditions;
    private final List<Action<MenuContext>> openActions;
    private final List<Action<MenuContext>> closeActions;
    private final MenuItem[] defaultItems;
    private final MenuItem[] items;
    private final Inventory inventory;
    private final Player viewer;

    private MenuImpl(@NotNull RawMenu rawMenu, @NotNull Player viewer) {
        this.name = rawMenu.name;
        this.title = rawMenu.title != null ? rawMenu.title : rawMenu.type.defaultTitle();
        this.size = rawMenu.type == InventoryType.CHEST ? rawMenu.size : rawMenu.type.getDefaultSize();
        if (rawMenu.type == InventoryType.CHEST && rawMenu.size % 9 != 0) {
            throw new UnsupportedOperationException();
        }
        this.type = rawMenu.type;
        this.handle = rawMenu.handle;
        this.openConditions = rawMenu.openConditions;
        this.openActions = rawMenu.openActions;
        this.closeActions = rawMenu.closeActions;
        this.defaultItems = rawMenu.items;
        if (this.defaultItems.length != this.size) {
            throw new UnsupportedOperationException();
        }
        this.items = Arrays.copyOf(rawMenu.items, rawMenu.items.length);
        this.inventory = rawMenu.type != InventoryType.CHEST ? Bukkit.createInventory(this, this.type, this.title) : Bukkit.createInventory(this, this.size, this.title);
        this.viewer = viewer;
    }

    @Override
    public void open(@NotNull MenuContext context) {
        for (final MenuCondition<MenuContext> condition : this.openConditions) {
            if (!condition.test(context)) {
                for (final Action<MenuContext> action : condition.getDenyActions()) {
                    action.execute(context);
                }
                break;
            }
        }

        this.refresh(context);

        for (final Action<MenuContext> action : this.openActions) {
            action.execute(context);
        }

        this.viewer.openInventory(this.inventory);
    }

    @Override
    public void handleClose(@NotNull MenuContext context) {
        for (final Action<MenuContext> action : this.closeActions) {
            action.execute(context);
        }
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public @NotNull Component getTitle() {
        return this.title;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public @NotNull InventoryType getType() {
        return this.type;
    }

    public boolean handle() {
        return this.handle;
    }

    @Override
    public @Nullable MenuItem getDefaultItem(int slot) {
        return slot > 0 && slot < this.items.length
                ? this.defaultItems[slot]
                : null;
    }

    @Override
    public @Nullable MenuItem getItem(int slot) {
        return slot >= 0 && slot < this.items.length
                ? this.items[slot]
                : null;
    }

    @Override
    public void setItem(int slot, @NotNull MenuItem menuItem, @NotNull MenuContext context) {
        this.items[slot] = menuItem;
        this.inventory.setItem(slot, menuItem.getDisplay(context));
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event, @NotNull MenuContext context) {
        final MenuItem item = this.getItem(event.getSlot());
        if (item != null) {
            item.handleClick(event, context);
        }
    }

    @Override
    public void refresh(@NotNull MenuContext context) {
        final Player player = context.getPlayer();
        if (player != null) {
            this.inventory.clear();
            for (int i = 0; i < this.defaultItems.length; i++) {
                this.refresh(i, context);
            }
        }
    }

    @Override
    public void refresh(int slot, @NotNull MenuContext context) {
        final MenuItem menuItem = this.defaultItems[slot];
        this.inventory.setItem(slot, menuItem != null ? menuItem.getDisplay(context) : null);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    public static final class MenuBuilder implements Menu.MenuBuilder {

        private @NotNull String name;
        private @Nullable Component title;
        private int size = 9;
        private @NotNull InventoryType type = InventoryType.CHEST;
        private boolean handle = true;
        private @NotNull Set<MenuCondition<MenuContext>> openConditions = ImmutableSet.of();
        private @NotNull List<Action<MenuContext>> openActions = ImmutableList.of();
        private @NotNull List<Action<MenuContext>> closeActions = ImmutableList.of();
        private @NotNull MenuItem[] items;

        @Override
        public MenuBuilder openConditions(@NotNull Set<MenuCondition<MenuContext>> openConditions) {
            this.openConditions = ImmutableSet.copyOf(openConditions);
            return this;
        }

        @Override
        public MenuBuilder openActions(@NotNull List<Action<MenuContext>> openActions) {
            this.openActions = ImmutableList.copyOf(openActions);
            return this;
        }

        @Override
        public MenuBuilder closeActions(@NotNull List<Action<MenuContext>> closeActions) {
            this.closeActions = ImmutableList.copyOf(closeActions);
            return this;
        }

        @Override
        public MenuBuilder items(@NotNull MenuItem[] items) {
            this.items = Arrays.copyOf(items, items.length);
            this.size = items.length;
            return this;
        }

        @Override
        public MenuItem[] items() {
            return Arrays.copyOf(this.items, this.items.length);
        }

        @Override
        public Menu.RawMenu build() {
            return new RawMenu(this);
        }
    }

    public static final class RawMenu implements Menu.RawMenu {

        private final @NotNull String name;
        private final @Nullable Component title;
        private final int size;
        private final @NotNull InventoryType type;
        private final boolean handle;
        private final @NotNull Set<MenuCondition<MenuContext>> openConditions;
        private final @NotNull List<Action<MenuContext>> openActions;
        private final @NotNull List<Action<MenuContext>> closeActions;
        private final @NotNull MenuItem[] items;

        private RawMenu(@NotNull MenuBuilder builder) {
            this.name = builder.name;
            this.title = builder.title;
            this.size = builder.size;
            this.handle = builder.handle;
            this.type = builder.type;
            this.openConditions = builder.openConditions;
            this.openActions = builder.openActions;
            this.closeActions = builder.closeActions;
            this.items = builder.items;
        }

        @Override
        public @NotNull String getName() {
            return this.name;
        }

        @Override
        public @Nullable Component getTitle() {
            return this.title;
        }

        @Override
        public int getSize() {
            return this.size;
        }

        @Override
        public @NotNull InventoryType getType() {
            return this.type;
        }

        @Override
        public boolean handle() {
            return this.handle;
        }

        @Override
        public @NotNull Set<MenuCondition<MenuContext>> getOpenConditions() {
            return this.openConditions;
        }

        @Override
        public @NotNull List<Action<MenuContext>> getOpenActions() {
            return this.openActions;
        }

        @Override
        public @NotNull List<Action<MenuContext>> getCloseActions() {
            return this.closeActions;
        }

        @Override
        public @Nullable MenuItem getItem(int slot) {
            return slot >= 0 && slot < this.items.length
                    ? this.items[slot]
                    : null;
        }

        @Override
        public void open(@NotNull Player player) {
            final MenuImpl menu = new MenuImpl(this, player);
            menu.open(menu.getContext(player));
        }
    }
}
