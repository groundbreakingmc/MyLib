package com.github.groundbreakingmc.mylib.utils.vault;

import com.github.groundbreakingmc.mylib.utils.bukkit.BukkitProviderUtils;
import lombok.experimental.UtilityClass;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicesManager;

@UtilityClass
public final class VaultUtils {

    private static Chat chat;
    private static Economy economy;

    public static Chat getChatProvider(final ServicesManager servicesManager) {
        if (chat != null) {
            return chat;
        }

        return (chat = BukkitProviderUtils.getProvider(servicesManager, Chat.class));
    }

    public static Economy getEconomyProvider(final ServicesManager servicesManager) {
        if (economy != null) {
            return economy;
        }

        return (economy = BukkitProviderUtils.getProvider(servicesManager, Economy.class));
    }
}
