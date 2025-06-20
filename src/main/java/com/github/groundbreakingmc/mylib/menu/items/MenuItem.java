package com.github.groundbreakingmc.mylib.menu.items;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.conditions.MenuCondition;
import com.github.groundbreakingmc.mylib.menu.items.impl.MenuItemGroup;
import com.github.groundbreakingmc.mylib.menu.items.impl.MenuItemImpl;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a configurable item inside a menu.
 * <p>
 * A {@code MenuItem} defines how an item is displayed, whether it can be seen,
 * how it reacts to clicks, and what conditions must be met for interaction.
 */
@SuppressWarnings("unused")
public interface MenuItem {

    /**
     * Returns the static display of this item, used as a fallback.
     * May be {@code null} if dynamic rendering is preferred.
     *
     * @return the static {@link ItemStack} display, or {@code null}
     */
    @Nullable
    ItemStack getDisplay();

    /**
     * Checks whether the item should be visible for the given context.
     * <p>
     * This method evaluates the item's see conditions.
     *
     * @param context the menu context
     * @return {@code true} if visible, {@code false} otherwise
     */
    boolean canSee(@NotNull MenuContext context);

    /**
     * Returns the rendered {@link ItemStack} for the player,
     * with placeholder substitution applied to the display and lore.
     *
     * @param context the menu context
     * @return the rendered item stack
     */
    @Nullable
    ItemStack getDisplay(@NotNull MenuContext context);

    /**
     * Returns the priority of this item relative to others (higher = displayed first).
     *
     * @return the item priority
     */
    int getPriority();

    /**
     * Returns the actions mapped to each {@link ClickType}.
     *
     * @return an immutable map of click actions
     */
    @NotNull
    Map<ClickType, List<Action<MenuContext>>> getClickActions();

    /**
     * Returns the conditions that determine whether the item is visible.
     *
     * @return a non-null set of see conditions
     */
    @NotNull
    Set<MenuCondition<MenuContext>> getSeeConditions();

    /**
     * Returns the conditions that must be met for each {@link ClickType}
     * to trigger its associated actions.
     *
     * @return an immutable map of click-specific conditions
     */
    @NotNull
    Map<ClickType, Set<MenuCondition<MenuContext>>> getClickConditions();

    /**
     * Handles an {@link InventoryClickEvent} for this item.
     * <p>
     * This checks click conditions and executes the appropriate actions.
     *
     * @param event   the Bukkit click event
     * @param context the current menu context
     */
    void handleClick(@NotNull InventoryClickEvent event, @NotNull MenuContext context);

    /**
     * Returns a new {@link MenuItemBuilder} instance.
     *
     * @return a builder for single items
     */
    static MenuItemBuilder builder() {
        return new MenuItemImpl.MenuItemBuilder();
    }

    /**
     * Returns a new {@link MenuItemGroupBuilder} instance.
     *
     * @return a builder for grouped menu items
     */
    static MenuItemGroupBuilder groupBuilder() {
        return new MenuItemGroup.MenuItemGroupBuilder();
    }

    /**
     * Builder for individual {@link MenuItem} instances.
     */
    interface MenuItemBuilder {

        /**
         * Sets the base display {@link ItemStack} for this item.
         *
         * @param display the base display item
         * @return the current builder instance
         */
        MenuItemBuilder display(@NotNull ItemStack display);

        /**
         * @return the current display {@link ItemStack}
         */
        ItemStack display();

        /**
         * Sets the action map for different click types.
         *
         * @param actions the map of actions
         * @return the current builder instance
         */
        MenuItemBuilder clickActions(@NotNull Map<ClickType, List<Action<MenuContext>>> actions);

        /**
         * @return the current map of click actions
         */
        Map<ClickType, List<Action<MenuContext>>> clickActions();

        /**
         * Sets the conditions that must pass for the item to be visible.
         *
         * @param conditions the see conditions
         * @return the current builder instance
         */
        MenuItemBuilder seeConditions(@NotNull Collection<MenuCondition<MenuContext>> conditions);

        /**
         * @return the set of see conditions
         */
        Set<MenuCondition<MenuContext>> seeConditions();

        /**
         * Sets the conditions per click type that must be satisfied before executing click actions.
         *
         * @param conditions the click-specific conditions
         * @return the current builder instance
         */
        MenuItemBuilder clickConditions(@NotNull Map<ClickType, Set<MenuCondition<MenuContext>>> conditions);

        /**
         * @return the map of click conditions
         */
        Map<ClickType, Set<MenuCondition<MenuContext>>> clickConditions();

        /**
         * Sets the priority of this item.
         *
         * @param priority an integer priority (higher = higher placement)
         * @return the current builder instance
         */
        MenuItemBuilder priority(int priority);

        /**
         * @return the current item priority
         */
        int priority();

        /**
         * Builds and returns the final {@link MenuItem} instance.
         *
         * @return a constructed menu item
         */
        MenuItem build();
    }

    /**
     * Builder for {@link MenuItem} groups, which selects
     * one of the registered items based on visibility and priority.
     */
    interface MenuItemGroupBuilder {

        /**
         * Adds an item to the group using its internal priority.
         *
         * @param menuItem the menu item
         * @return the current group builder
         */
        MenuItemGroupBuilder add(@NotNull MenuItem menuItem);

        /**
         * Adds an item to the group with a manually assigned priority.
         *
         * @param menuItem the menu item
         * @param priority the priority to apply
         * @return the current group builder
         */
        MenuItemGroupBuilder add(@NotNull MenuItem menuItem, int priority);

        /**
         * Builds the group into a single {@link MenuItem} that will resolve
         * dynamically at runtime based on see conditions and priority.
         *
         * @return the grouped menu item
         */
        MenuItem build();
    }
}
