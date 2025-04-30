package com.github.groundbreakingmc.mylib.vanish;

import com.github.groundbreakingmc.mylib.vanish.impl.PermissionChecker;
import com.github.groundbreakingmc.mylib.vanish.impl.SuperVanishChecker;
import com.github.groundbreakingmc.mylib.vanish.impl.UltimateVanishChecker;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@UtilityClass
public final class VisibleCheckerFactory {

    @Nullable
    public static VisibleChecker create() {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        if (pluginManager.isPluginEnabled("UltimateVanish")) {
            return new UltimateVanishChecker();
        }
        if (pluginManager.isPluginEnabled("SuperVanish")) {
            return new SuperVanishChecker();
        }
        if (pluginManager.isPluginEnabled("Essentials")) {
            return new PermissionChecker("essentials.vanish.see");
        }
        if (pluginManager.isPluginEnabled("CMI")) {
            return new PermissionChecker("cmi.seevanished");
        }
        if (pluginManager.isPluginEnabled("SunLight")) {
            return new PermissionChecker("sunlight.vanish.bypass.see");
        }

        return null;
    }
}
