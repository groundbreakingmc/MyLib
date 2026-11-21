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

    private static final VisibleChecker VISIBLE_CHECKER = setup();

    @Nullable
    public static VisibleChecker create() {
        return VISIBLE_CHECKER;
    }

    @Nullable
    private static VisibleChecker setup() {
        final PluginManager pluginManager = Bukkit.getPluginManager();
        if (pluginManager.getPlugin("UltimateVanish") != null) {
            return new UltimateVanishChecker();
        }
        if (pluginManager.getPlugin("SuperVanish") != null) {
            return new SuperVanishChecker();
        }
        if (pluginManager.getPlugin("Essentials") != null) {
            return new PermissionChecker("essentials.vanish.see");
        }
        if (pluginManager.getPlugin("CMI") != null) {
            return new PermissionChecker("cmi.seevanished");
        }
        if (pluginManager.getPlugin("SunLight") != null) {
            return new PermissionChecker("sunlight.vanish.bypass.see");
        }

        return null;
    }
}
