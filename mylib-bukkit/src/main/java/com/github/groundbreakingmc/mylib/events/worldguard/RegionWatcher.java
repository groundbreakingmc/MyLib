package com.github.groundbreakingmc.mylib.events.worldguard;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.session.handler.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@SuppressWarnings("unused")
public class RegionWatcher extends Handler {

    public static final RegionWatcherFactory REGION_WATCHER_FACTORY;

    private static boolean registered;

    private RegionWatcher(final Session session) {
        super(session);
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer wgPlayer,
                                   Location oldLocation, Location newLocation,
                                   ApplicableRegionSet toSet,
                                   Set<ProtectedRegion> entered, Set<ProtectedRegion> exited,
                                   MoveType moveType) {
        return this.onEntry(wgPlayer, entered) && this.onLeave(wgPlayer, exited);
    }

    private boolean onEntry(LocalPlayer wgPlayer, Set<ProtectedRegion> regions) {
        final PlayerRegionEntryEvent event = new PlayerRegionEntryEvent(wgPlayer, regions);
        return event.callEvent();
    }

    private boolean onLeave(LocalPlayer wgPlayer, Set<ProtectedRegion> regions) {
        final PlayerRegionLeaveEvent event = new PlayerRegionLeaveEvent(wgPlayer, regions);
        return event.callEvent();
    }

    public static boolean register() {
        if (registered) {
            return false;
        }

        getSessionManager().registerHandler(REGION_WATCHER_FACTORY, null);
        registered = true;
        return true;
    }

    public static boolean unregister() {
        if (!registered) {
            return false;
        }

        getSessionManager().unregisterHandler(REGION_WATCHER_FACTORY);
        registered = false;
        return true;
    }

    private static SessionManager getSessionManager() {
        return WorldGuard.getInstance()
                .getPlatform()
                .getSessionManager();
    }

    public final static class RegionWatcherFactory extends Handler.Factory<RegionWatcher> {

        @Override
        public RegionWatcher create(@NotNull Session session) {
            return new RegionWatcher(session);
        }
    }

    static {
        REGION_WATCHER_FACTORY = new RegionWatcherFactory();
    }
}
