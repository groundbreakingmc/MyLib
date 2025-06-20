package com.github.groundbreakingmc.mylib.menu.menus.services;

import com.github.groundbreakingmc.mylib.menu.menus.Menu;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Service for registering and retrieving menus by name.
 */
@SuppressWarnings("unused")
public class MenuService {

    private final Map<String, Menu.RawMenu> menus;

    public MenuService(@NotNull Map<String, Menu.RawMenu> menus) {
        this.menus = menus;
    }

    /**
     * Registers a new Menu.
     *
     * @param menu     the menu to register
     * @param override whether to override an existing menu with the same name
     * @return true if the action was registered, false otherwise
     */
    public boolean register(@NotNull Menu.RawMenu menu, boolean override) {
        if (!override) {
            for (final Menu.RawMenu target : this.menus.values()) {
                if (target.getName().equalsIgnoreCase(menu.getName())) {
                    return false;
                }
            }
        }

        this.menus.put(menu.getName(), menu);
        return true;
    }

    /**
     * Parses a string and creates a corresponding Action if any prefix matches.
     *
     * @param name of the menu
     * @return the Menu, or null if no match is found
     */
    @Nullable
    public Menu.RawMenu byName(@NotNull String name) {
        return this.menus.get(name);
    }

    public static MenuService create() {
        return new MenuService(new Object2ObjectOpenHashMap<>());
    }

    public static MenuService createSynchronized() {
        return new MenuService(Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>()));
    }
}
