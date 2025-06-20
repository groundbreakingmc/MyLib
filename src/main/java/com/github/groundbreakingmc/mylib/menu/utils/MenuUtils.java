package com.github.groundbreakingmc.mylib.menu.utils;

import com.github.groundbreakingmc.mylib.menu.menus.Menu;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for working with {@link Menu} instances in relation to players and inventories.
 * <p>
 * Provides helper methods to retrieve the currently viewed {@link Menu} by a player
 * or to extract a {@link Menu} instance from a given {@link Inventory}.
 */
@UtilityClass
@SuppressWarnings("unused")
public class MenuUtils {

    /**
     * Retrieves the {@link Menu} currently being viewed by the given player.
     *
     * @param player the player whose open menu is to be retrieved, can be null
     * @return the {@link Menu} instance the player is currently viewing, or null if none
     */
    public @Nullable Menu getWatching(@Nullable Player player) {
        if (player == null) return null;
        final Inventory inventory = player.getOpenInventory().getTopInventory();
        return inventory.getHolder() instanceof Menu menu ? menu : null;
    }

    /**
     * Extracts the {@link Menu} instance from the given {@link Inventory}, if its holder is a Menu.
     *
     * @param inventory the inventory from which to extract the Menu, can be null
     * @return the {@link Menu} instance held by the inventory, or null if none
     */
    public @Nullable Menu extractMenu(@Nullable Inventory inventory) {
        if (inventory == null) return null;
        final InventoryHolder holder = inventory.getHolder();
        return holder instanceof Menu menu ? menu : null;
    }
}
