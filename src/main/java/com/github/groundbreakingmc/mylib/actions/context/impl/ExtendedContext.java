package com.github.groundbreakingmc.mylib.actions.context.impl;

import com.github.groundbreakingmc.mylib.actions.context.ActionContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@SuppressWarnings("unused")
public final class ExtendedContext implements ActionContext {

    private final Player player;
    private final Player placeholderPlayer;
    private final Map<Class<?>, Object> map = new Object2ObjectOpenHashMap<>();

    public ExtendedContext(@Nullable Player player) {
        this.player = player;
        this.placeholderPlayer = player;
    }

    public ExtendedContext(@Nullable Player player, @Nullable Player placeholderPlayer) {
        this.player = player;
        this.placeholderPlayer = placeholderPlayer != null
                ? placeholderPlayer
                : player;
    }

    public <T> void put(@NotNull Class<T> key, @NotNull T value) {
        this.map.put(key, value);
    }

    @Override
    public @Nullable Player getPlayer() {
        return this.player;
    }

    @Override
    public @Nullable Player getPlaceholderPlayer() {
        return this.placeholderPlayer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <T> T get(@NotNull Class<T> type) {
        return (T) this.map.get(type);
    }
}
