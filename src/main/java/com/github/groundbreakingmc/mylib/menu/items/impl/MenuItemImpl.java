package com.github.groundbreakingmc.mylib.menu.items.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.conditions.MenuCondition;
import com.github.groundbreakingmc.mylib.menu.items.MenuItem;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public final class MenuItemImpl implements MenuItem {

    private final ItemStack display;
    private final int priority;
    private final Set<MenuCondition<MenuContext>> seeConditions;
    private final Map<ClickType, List<Action<MenuContext>>> clickActions;
    private final Map<ClickType, Set<MenuCondition<MenuContext>>> clickConditions;

    MenuItemImpl(MenuItemBuilder builder) {
        this.display = builder.display;
        this.priority = builder.priority;
        this.seeConditions = builder.seeConditions;
        this.clickActions = builder.clickActions;
        this.clickConditions = builder.clickConditions;
    }

    @Override
    public @NotNull ItemStack getDisplay() {
        return this.display;
    }

    @Override
    public boolean canSee(@NotNull MenuContext context) {
        for (final MenuCondition<MenuContext> condition : this.seeConditions) {
            if (!condition.test(context)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getDisplay(@NotNull MenuContext context) {
        final ItemStack itemStack = this.display.clone();

        final ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(PlaceholderAPI.setPlaceholders(context.getPlayer(), itemMeta.getDisplayName()));

        final List<String> lore = itemMeta.getLore();
        if (lore != null && !lore.isEmpty()) {
            final ImmutableList.Builder<String> newLore = ImmutableList.builder();
            for (final String line : lore) {
                newLore.add(PlaceholderAPI.setPlaceholders(context.getPlayer(), line));
            }
            itemMeta.setLore(newLore.build());
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void handleClick(@NotNull InventoryClickEvent event, @NotNull MenuContext context) {
        List<Action<MenuContext>> actions = this.clickActions.get(event.getClick());

        final Set<MenuCondition<MenuContext>> conditions = this.clickConditions.get(event.getClick());
        if (conditions != null && !conditions.isEmpty()) {
            for (final MenuCondition<MenuContext> condition : conditions) {
                if (!condition.test(context)) {
                    actions = condition.getDenyActions();
                    break;
                }
            }
        }

        if (actions != null && !actions.isEmpty()) {
            for (final Action<MenuContext> action : actions) {
                action.execute(context);
            }
        }
    }

    public static final class MenuItemBuilder implements MenuItem.MenuItemBuilder {

        private ItemStack display;
        private int priority;
        private Set<MenuCondition<MenuContext>> seeConditions = ImmutableSet.of();
        private Map<ClickType, List<Action<MenuContext>>> clickActions = ImmutableMap.of();
        private Map<ClickType, Set<MenuCondition<MenuContext>>> clickConditions = ImmutableMap.of();

        @Override
        public MenuItemBuilder display(@NotNull ItemStack display) {
            this.display = display.clone();
            return this;
        }

        @Override
        public ItemStack display() {
            return this.display;
        }

        @Override
        public MenuItem.MenuItemBuilder priority(int priority) {
            this.priority = priority;
            return this;
        }

        @Override
        public int priority() {
            return this.priority;
        }

        @Override
        public MenuItemBuilder seeConditions(@NotNull Collection<MenuCondition<MenuContext>> conditions) {
            this.seeConditions = ImmutableSet.copyOf(conditions);
            return this;
        }

        @Override
        public Set<MenuCondition<MenuContext>> seeConditions() {
            return this.seeConditions;
        }

        @Override
        public MenuItemBuilder clickActions(@NotNull Map<ClickType, List<Action<MenuContext>>> actions) {
            if (actions.isEmpty()) {
                this.clickActions = ImmutableMap.of();
                return this;
            }

            this.clickActions = actions.entrySet().stream()
                    .collect(ImmutableMap.toImmutableMap(
                            Map.Entry::getKey,
                            entry -> ImmutableList.copyOf(entry.getValue())
                    ));
            return this;
        }

        @Override
        public Map<ClickType, List<Action<MenuContext>>> clickActions() {
            return this.clickActions;
        }

        @Override
        public MenuItemBuilder clickConditions(@NotNull Map<ClickType, Set<MenuCondition<MenuContext>>> conditions) {
            if (conditions.isEmpty()) {
                this.clickConditions = ImmutableMap.of();
                return this;
            }

            this.clickConditions = conditions.entrySet().stream()
                    .collect(ImmutableMap.toImmutableMap(
                            Map.Entry::getKey,
                            entry -> ImmutableSet.copyOf(entry.getValue())
                    ));
            return this;
        }

        @Override
        public Map<ClickType, Set<MenuCondition<MenuContext>>> clickConditions() {
            return this.clickConditions;
        }

        @Override
        public MenuItem build() {
            return new MenuItemImpl(this);
        }
    }
}
