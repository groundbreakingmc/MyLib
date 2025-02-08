package com.github.groundbreakingmc.mylib.utils.vault;

import com.github.groundbreakingmc.mylib.utils.bukkit.BukkitProviderUtils;
import lombok.experimental.UtilityClass;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicesManager;

@UtilityClass
public final class VaultUtils {

    private static Chat chat;
    private static Economy economy;
    private static Permission permission;

    public static Chat getChatProvider() {
        return getChatProvider(Bukkit.getServicesManager());
    }

    public static Chat getChatProvider(final ServicesManager servicesManager) {
        if (chat != null) {
            return chat;
        }

        return (chat = BukkitProviderUtils.getProvider(servicesManager, Chat.class));
    }

    public static Economy getEconomyProvider() {
        return getEconomyProvider(Bukkit.getServicesManager());
    }

    public static Economy getEconomyProvider(final ServicesManager servicesManager) {
        if (economy != null) {
            return economy;
        }

        return (economy = BukkitProviderUtils.getProvider(servicesManager, Economy.class));
    }

    public static Permission getPermissionProvider() {
        return getPermissionProvider(Bukkit.getServicesManager());
    }

    public static Permission getPermissionProvider(final ServicesManager servicesManager) {
        if (permission != null) {
            return permission;
        }

        return (permission = BukkitProviderUtils.getProvider(servicesManager, Permission.class));
    }
}
