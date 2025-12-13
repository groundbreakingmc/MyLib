package com.github.groundbreakingmc.mylib.actions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.actions.context.ActionContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class SoundAction<C extends ActionContext> implements Action<C> {

    private final Sound sound;

    public SoundAction(@NotNull String raw) {
        final String[] params = raw.split(";");
        if (params.length == 0 || params[0].isEmpty()) {
            throw new IllegalArgumentException("Sound name is missing");
        }
        @Subst("minecraft:entity.experience_orb.pickup") final String name = params[0].toLowerCase();
        final float volume = params.length > 1 ? Float.parseFloat(params[1]) : 1f;
        final float pitch = params.length > 2 ? Float.parseFloat(params[2]) : 1f;
        final Sound.Source source = params.length > 3 ? Sound.Source.valueOf(params[3]) : Sound.Source.MASTER;

        this.sound = Sound.sound(Key.key(name), source, volume, pitch);
    }

    @Override
    public void execute(@NotNull C context) {
        final Player player = context.getPlayer();
        if (player != null) {
            player.playSound(this.sound);
        }
    }
}
