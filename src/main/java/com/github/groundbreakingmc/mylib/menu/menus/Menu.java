package com.github.groundbreakingmc.mylib.menu.menus;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.conditions.MenuCondition;
import com.github.groundbreakingmc.mylib.menu.items.MenuItem;
import com.github.groundbreakingmc.mylib.menu.menus.impl.MenuImpl;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Represents a virtual inventory-based menu that supports dynamic content,
 * contextual conditions, and action handling on user interaction.
 */
@SuppressWarnings("unused")
public interface Menu extends InventoryHolder {

    /**
     * Opens the menu to the specified context (usually for a specific player).
     *
     * @param context the menu context containing the player and placeholders
     */
    void open(@NotNull MenuContext context);

    /**
     * Called when the menu is closed by the player.
     * Used to trigger close actions.
     *
     * @param context the menu context
     */
    void handleClose(@NotNull MenuContext context);

    /**
     * Returns the internal name of the menu.
     * This identifier is typically used for tracking or referencing menus programmatically.
     *
     * @return the menu's internal name
     */
    @NotNull String getName();

    /**
     * Returns the title of the menu as shown in the inventory UI.
     *
     * @return the inventory title
     */
    @NotNull Component getTitle();

    /**
     * Returns the number of slots in the inventory.
     * Only relevant for {@link InventoryType#CHEST}.
     *
     * @return the inventory size in slots
     */
    int getSize();

    /**
     * Returns the type of inventory used by the menu.
     *
     * @return the inventory type
     */
    @NotNull InventoryType getType();

    /**
     * Indicates whether MyLib should automatically handle click and close events for this menu.
     * If {@code false}, click and close events will be ignored by MyLib for this menu unless manually processed by yourself.
     *
     * @return {@code true} if clicks are handled by MyLib
     */
    boolean handle();

    /**
     * Returns the default item at the specified slot.
     * This is used for refreshing the menu to its initial state.
     *
     * @param slot the slot index
     * @return the default item at the slot, or {@code null} if not set
     */
    @Nullable MenuItem getDefaultItem(int slot);

    /**
     * Returns the currently active item at the specified slot.
     * This may differ from the default item due to runtime updates.
     *
     * @param slot the slot index
     * @return the current item at the slot, or {@code null} if none
     */
    @Nullable MenuItem getItem(int slot);

    /**
     * Sets the item in the given slot and updates its visual representation.
     *
     * @param slot     the target inventory slot
     * @param menuItem the new item to display
     * @param context  the current menu context
     */
    void setItem(int slot, @NotNull MenuItem menuItem, @NotNull MenuContext context);

    /**
     * Handles an inventory click event inside this menu.
     * Delegates the event to the corresponding {@link MenuItem}, if present.
     *
     * @param event   the click event
     * @param context the menu context
     */
    void handleClick(@NotNull InventoryClickEvent event, @NotNull MenuContext context);

    /**
     * Refreshes the entire menu, resetting all slots to their default items.
     *
     * @param context the menu context
     */
    void refresh(@NotNull MenuContext context);

    /**
     * Refreshes a specific slot by updating its item to the default value.
     *
     * @param slot    the slot index to refresh
     * @param context the menu context
     */
    void refresh(int slot, @NotNull MenuContext context);

    /**
     * Creates a {@link MenuContext} for the given player.
     * The player is used both for inventory display and placeholder parsing.
     *
     * @param player the player viewing the menu
     * @return a new menu context
     */
    default MenuContext getContext(@NotNull Player player) {
        return this.getContext(player, player);
    }

    /**
     * Creates a {@link MenuContext} for the given viewer and placeholder source.
     * This allows using a different player as the source for placeholders (e.g. for targeting).
     *
     * @param player            the viewer
     * @param placeholderPlayer the player used for placeholder resolution
     * @return a new menu context
     */
    default MenuContext getContext(@NotNull Player player, @NotNull Player placeholderPlayer) {
        return new MenuContext(player, placeholderPlayer, this);
    }

    /**
     * Starts building a new {@link Menu} instance using a fluent builder pattern.
     *
     * @return a new {@link MenuBuilder} instance
     */
    static MenuBuilder builder() {
        return new MenuImpl.MenuBuilder();
    }

    /**
     * Builder interface for constructing {@link Menu} instances in a fluent and type-safe way.
     * Allows configuration of the menu's metadata, size, type, actions, and item layout.
     */
    interface MenuBuilder {

        /**
         * Sets the internal name of the menu.
         *
         * @param name the name of the menu
         * @return this builder
         */
        MenuBuilder name(@NotNull String name);

        /**
         * Gets the current internal name set for the menu.
         *
         * @return the name
         */
        String name();

        /**
         * Sets the title component shown in the inventory UI.
         *
         * @param title the inventory title
         * @return this builder
         */
        MenuBuilder title(@NotNull Component title);

        /**
         * Gets the current title set for the menu.
         *
         * @return the title component
         */
        Component title();

        /**
         * Sets the size (number of slots) for chest-based inventories.
         * Ignored for non-chest {@link InventoryType}s.
         *
         * @param size the number of slots (must be a multiple of 9)
         * @return this builder
         */
        MenuBuilder size(int size);

        /**
         * Gets the size currently configured in the builder.
         *
         * @return the size
         */
        int size();

        /**
         * Sets the type of inventory to use (e.g. CHEST, HOPPER).
         *
         * @param type the inventory type
         * @return this builder
         */
        MenuBuilder type(@NotNull InventoryType type);

        /**
         * Gets the configured inventory type.
         *
         * @return the inventory type
         */
        InventoryType type();

        /**
         * Sets whether MyLib should automatically handle inventory click and close events
         * for this menu instance. If set to {@code true}, the menu will internally process
         * {@link InventoryClickEvent} and {@link InventoryCloseEvent} events.
         *
         * @param handle {@code true} to enable automatic event handling; {@code false} to disable
         * @return this builder for chaining
         * @see Menu#handle()
         */
        MenuBuilder handle(boolean handle);

        /**
         * Returns whether this menu is configured to automatically handle inventory click and close events.
         *
         * @return {@code true} if automatic event handling is enabled; {@code false} otherwise
         * @see Menu#handle()
         */
        boolean handle();

        /**
         * Sets the open conditions required to display the menu.
         * If any condition fails, the menu will not open and deny actions will run.
         *
         * @param openConditions set of open conditions
         * @return this builder
         */
        MenuBuilder openConditions(@NotNull Set<MenuCondition<MenuContext>> openConditions);

        /**
         * Gets the open conditions currently configured.
         *
         * @return the set of conditions
         */
        Set<MenuCondition<MenuContext>> openConditions();

        /**
         * Sets the actions to run when the menu is successfully opened.
         *
         * @param openActions list of open actions
         * @return this builder
         */
        MenuBuilder openActions(@NotNull List<Action<MenuContext>> openActions);

        /**
         * Gets the open actions currently configured.
         *
         * @return the list of open actions
         */
        List<Action<MenuContext>> openActions();

        /**
         * Sets the actions to run when the menu is closed.
         *
         * @param closeActions list of close actions
         * @return this builder
         */
        MenuBuilder closeActions(@NotNull List<Action<MenuContext>> closeActions);

        /**
         * Gets the close actions currently configured.
         *
         * @return the list of close actions
         */
        List<Action<MenuContext>> closeActions();

        /**
         * Sets the item layout of the menu.
         * The array length must match the menu size.
         *
         * @param items the array of menu items
         * @return this builder
         */
        MenuBuilder items(@NotNull MenuItem[] items);

        /**
         * Gets the current item layout set for the menu.
         *
         * @return array of items
         */
        MenuItem[] items();

        /**
         * Builds the raw immutable menu representation, which can be used to open the menu.
         *
         * @return the constructed {@link RawMenu}
         */
        RawMenu build();
    }

    /**
     * Represents a fully configured menu definition that can be used to construct
     * and open a {@link Menu} instance for a player.
     * Immutable and created via a {@link MenuBuilder}.
     */
    interface RawMenu {

        /**
         * Gets the internal name of the menu.
         *
         * @return the name
         */
        @NotNull String getName();

        /**
         * Gets the display title for the inventory.
         *
         * @return the title, or null for default
         */
        @Nullable Component getTitle();

        /**
         * Gets the size of the menu in slots (only for CHEST type).
         *
         * @return the size
         */
        int getSize();

        /**
         * Gets the type of inventory.
         *
         * @return the inventory type
         */
        @NotNull InventoryType getType();

        /**
         * Whether this menu should handle inventory clicks automatically.
         *
         * @return true if handled
         */
        boolean handle();

        /**
         * Gets the set of conditions that must be satisfied to open the menu.
         *
         * @return set of open conditions
         */
        @NotNull Set<MenuCondition<MenuContext>> getOpenConditions();

        /**
         * Gets the list of actions executed when the menu is opened.
         *
         * @return list of open actions
         */
        @NotNull List<Action<MenuContext>> getOpenActions();

        /**
         * Gets the list of actions executed when the menu is closed.
         *
         * @return list of close actions
         */
        @NotNull List<Action<MenuContext>> getCloseActions();

        /**
         * Returns the menu item at the specified slot.
         *
         * @param slot the inventory slot
         * @return the menu item, or null if not set
         */
        @Nullable MenuItem getItem(int slot);

        /**
         * Opens the menu for the given player by constructing a {@link MenuImpl}
         * and initializing it with this raw definition.
         *
         * @param player the player to show the menu to
         */
        void open(@NotNull Player player);
    }
}
