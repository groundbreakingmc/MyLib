package com.github.groundbreakingmc.mylib.utils.player;

import com.github.groundbreakingmc.mylib.utils.player.settings.EffectSettings;
import com.github.groundbreakingmc.mylib.utils.player.settings.SoundSettings;
import com.github.groundbreakingmc.mylib.utils.player.settings.TitleSettings;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@UtilityClass
public final class PlayerUtils {

    public static boolean isVanished(final @Nullable Player player) {
        if (player == null) {
            return false;
        }

        final List<MetadataValue> metadata = player.getMetadata("vanished");
        return !metadata.isEmpty() && metadata.get(0).asBoolean();
    }

    public static void broadcast(final Collection<? extends Player> players, final String message) {
        for (final Player player : players) {
            player.sendMessage(message);
        }
    }

    public static void broadcastWithSound(final Collection<? extends Player> players,
                                          final String message, final SoundSettings soundSettings) {
        for (final Player player : players) {
            player.sendMessage(message);
            playSound(player, soundSettings);
        }
    }

    public static void showTitle(final Collection<? extends Player> players, final TitleSettings titleSettings) {
        for (final Player player : players) {
            showTitle(player, titleSettings);
        }
    }

    public static void showTitle(final Player player, final TitleSettings titleSettings) {
        player.sendTitle(titleSettings.title, titleSettings.subtitle,
                titleSettings.fadeIn, titleSettings.stay, titleSettings.fadeOut);
    }

    public static void addPotionEffect(final Collection<? extends Player> players, final EffectSettings effectSettings) {
        for (final Player player : players) {
            addPotionEffect(player, effectSettings);
        }
    }

    public static void addPotionEffect(final Player player, final EffectSettings effectSettings) {
        player.addPotionEffect(effectSettings.getPotionEffect());
    }

    public static void addPotionEffect(final Collection<? extends Player> players, final PotionEffect potionEffect) {
        for (final Player player : players) {
            player.addPotionEffect(potionEffect);
        }
    }

    public static void playSound(final Collection<? extends Player> players, final SoundSettings soundSettings) {
        for (final Player player : players) {
            playSound(player, soundSettings);
        }
    }

    public static void playSound(final Player player, final SoundSettings soundSettings) {
        player.playSound(player.getLocation(), soundSettings.sound, soundSettings.volume, soundSettings.pitch);
    }
}
