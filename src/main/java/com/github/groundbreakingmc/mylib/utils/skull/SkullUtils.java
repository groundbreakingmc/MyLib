package com.github.groundbreakingmc.mylib.utils.skull;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;

import java.lang.reflect.Field;
import java.util.UUID;

@UtilityClass
public final class SkullUtils {

    public static void setSkin(final String texture, final Block block) {
        setSkin(texture, getSkull(block));
    }

    public static void setSkin(final GameProfile texture, final Block block) {
        setSkin(texture, getSkull(block));
    }

    public static void setSkin(final String texture, final Skull skull) {
        final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "");
        gameProfile.getProperties().put("textures", new Property("textures", texture));
    }

    public static void setSkin(final GameProfile texture, final Skull skull) {
        try {
            final Field field = skull.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(skull, texture);
        } catch (final NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        skull.update();
    }

    public static Skull getSkull(final Block block) {
        block.setType(Material.PLAYER_HEAD);
        return (Skull) block.getState();
    }
}
