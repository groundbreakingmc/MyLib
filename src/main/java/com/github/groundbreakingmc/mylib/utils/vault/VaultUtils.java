package com.github.groundbreakingmc.mylib.utils.vault;

import lombok.experimental.UtilityClass;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

@UtilityClass
public final class VaultUtils {

    private static Chat chat;
    private static Economy economy;

    public static Chat getChatProvider(final ServicesManager servicesManager) {
        if (chat != null) {
            return chat;
        }

        return (chat = getProvider(servicesManager, Chat.class));
    }

    public static Economy getEconomyProvider(final ServicesManager servicesManager) {
        if (economy != null) {
            return economy;
        }

        return (economy = getProvider(servicesManager, Economy.class));
    }

    public static <T> T getProvider(final ServicesManager servicesManager, final Class<T> clazz) {
        final RegisteredServiceProvider<T> provider = servicesManager.getRegistration(clazz);
        return provider != null ? provider.getProvider() : null;
    }

}
