package com.github.groundbreakingmc.mylib.menu.listeners;

import com.github.groundbreakingmc.mylib.MyLib;
import com.github.groundbreakingmc.mylib.menu.menus.Menu;
import com.github.groundbreakingmc.mylib.menu.utils.MenuUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Global event listener for handling menu interactions.
 * <p>
 * This singleton class listens for {@link InventoryClickEvent} and {@link InventoryCloseEvent}
 * to delegate interaction logic to custom {@link Menu} implementations.
 * <p>
 * Register this listener once during plugin startup using {@link #register(Plugin)}.
 * </p>
 *
 * @see Menu
 * @see MenuUtils
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MenuListener implements Listener {

    /** Singleton instance of the listener */
    private static MenuListener instance;

    /**
     * Handles inventory click events for active menus.
     * <p>
     * If a valid {@link Menu} is found for the clicked inventory and it is marked
     * as {@link Menu#handle() handleable}, the click is delegated to the menu and the event is cancelled.
     * </p>
     *
     * @param event the inventory click event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onClick(final InventoryClickEvent event) {
        final Inventory inventory = event.getClickedInventory();
        final Menu menu = MenuUtils.extractMenu(inventory);
        if (menu != null && menu.handle()) {
            menu.handleClick(event, menu.getContext((Player) event.getWhoClicked()));
            event.setCancelled(true);
        }
    }

    /**
     * Handles inventory close events for active menus.
     * <p>
     * If a valid {@link Menu} is found for the closed inventory and it is marked
     * as {@link Menu#handle() handleable}, the close is delegated to the menu.
     * </p>
     *
     * @param event the inventory close event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onClose(final InventoryCloseEvent event) {
        final Inventory inventory = event.getInventory();
        final Menu menu = MenuUtils.extractMenu(inventory);
        if (menu != null && menu.handle()) {
            menu.handleClose(menu.getContext((Player) event.getPlayer()));
        }
    }

    /**
     * Registers the {@link MenuListener} with the given plugin.
     *
     * This method should be called once during plugin initialization. If
     * {@link MyLib} is present, it will be used as the plugin reference.
     *
     * @param plugin the plugin instance to register with
     * @return {@code true} if the listener was registered, {@code false} if already registered
     */
    public static boolean register(@NotNull Plugin plugin) {
        if (instance != null) {
            return false;
        }

        Bukkit.getPluginManager().registerEvents(
                instance = new MenuListener(),
                MyLib.getInstance() != null ? MyLib.getInstance() : plugin
        );
        return true;
    }

    /**
     * Unregisters the {@link MenuListener}, if it was previously registered.
     *
     * @return {@code true} if the listener was unregistered, {@code false} if it was not registered
     */
    public static boolean unregister() {
        if (instance == null) {
            return false;
        }

        HandlerList.unregisterAll(instance);
        instance = null;
        return true;
    }
}
