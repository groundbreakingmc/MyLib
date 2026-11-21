package com.github.groundbreakingmc.mylib.utils.bukkit;

import lombok.experimental.UtilityClass;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

@UtilityClass
public final class BukkitProviderUtils {

    public static <T> T getProvider(final ServicesManager servicesManager, final Class<T> clazz) {
        final RegisteredServiceProvider<T> provider = servicesManager.getRegistration(clazz);
        return provider != null ? provider.getProvider() : null;
    }
}
