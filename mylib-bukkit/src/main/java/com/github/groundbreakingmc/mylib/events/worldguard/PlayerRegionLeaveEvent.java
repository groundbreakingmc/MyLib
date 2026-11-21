package com.github.groundbreakingmc.mylib.events.worldguard;

import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@SuppressWarnings("unused")
public class PlayerRegionLeaveEvent extends PlayerEvent implements Cancellable {

    public static final HandlerList HANDLERS;

    private final LocalPlayer localPlayer;
    private final Set<ProtectedRegion> regions;

    private boolean canceled;

    public PlayerRegionLeaveEvent(@NotNull LocalPlayer who, @NotNull Set<ProtectedRegion> regions) {
        super(BukkitAdapter.adapt(who));

        this.localPlayer = who;
        this.regions = ImmutableSet.copyOf(regions);
    }

    @NotNull
    public LocalPlayer getLocalPlayer() {
        return localPlayer;
    }

    @NotNull
    public Set<ProtectedRegion> getRegions() {
        return regions;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        canceled = cancel;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    static {
        HANDLERS = new HandlerList();
    }
}
