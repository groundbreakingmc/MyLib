package com.github.groundbreakingmc.mylib.utils.bukkit;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@UtilityClass @SuppressWarnings("unused")
public final class BukkitSerializeUtils {

    public static String locationToString(final Location location) {
        return location.getWorld().getName() + ";" +
                location.getX() + ";" +
                location.getY() + ";" +
                location.getZ() + ";" +
                location.getYaw() + ";" +
                location.getPitch();
    }

    public static Location locationFromString(final String string) {
        final String[] params = string.split(";");
        return new Location(
                Bukkit.getWorld(params[0]),
                Double.parseDouble(params[1]), // x
                Double.parseDouble(params[2]), // y
                Double.parseDouble(params[3]), // z
                params.length > 4 ? Float.parseFloat(params[4]) : 0, // yaw
                params.length > 5 ? Float.parseFloat(params[5]) : 0 // pitch
        );
    }
}
