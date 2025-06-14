package com.github.groundbreakingmc.mylib.utils.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.index.ConcurrentRegionIndex;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("unused")
@UtilityClass
public final class WorldGuardUtils {

    public static final RegionContainer REGION_CONTAINER;

    @Nullable
    public static ApplicableRegionSet getApplicableRegions(final Location location) {
        Objects.requireNonNull(location, "Location can not be null!");
        return getApplicableRegions(BukkitAdapter.adapt(location.getWorld()), BukkitAdapter.asBlockVector(location));
    }

    @Nullable
    public static ApplicableRegionSet getApplicableRegions(final com.sk89q.worldedit.world.World world, final BlockVector3 blockVector3) {
        Objects.requireNonNull(world, "World can not be null!");
        Objects.requireNonNull(blockVector3, "Block vector can not be null!");

        final RegionManager regionManager = REGION_CONTAINER.get(world);
        if (regionManager == null || regionManager.getRegions().isEmpty()) {
            return null;
        }

        return regionManager.getApplicableRegions(blockVector3);
    }

    public static ProtectedRegion createRegion(final Location centerLocation,
                                               final int radius,
                                               final String regionName) {
        Objects.requireNonNull(centerLocation, "Location can not be null!");
        Objects.requireNonNull(regionName, "Region name can not be null!");

        final BlockVector3 minPoint = BlockVector3.at(
                centerLocation.getX() - radius,
                centerLocation.getY() - radius,
                centerLocation.getZ() - radius
        );

        final BlockVector3 maxPoint = BlockVector3.at(
                centerLocation.getX() + radius,
                centerLocation.getY() + radius,
                centerLocation.getZ() + radius
        );

        return createRegion(centerLocation.getWorld(), regionName, minPoint, maxPoint);
    }

    public static ProtectedRegion createRegion(final Location centerLocation,
                                               final int radiusX,
                                               final int radiusY,
                                               final int radiusZ,
                                               final String regionName) {
        Objects.requireNonNull(centerLocation, "Location can not be null!");
        Objects.requireNonNull(regionName, "Region name can not be null!");

        final BlockVector3 minPoint = BlockVector3.at(
                centerLocation.getX() - radiusX,
                centerLocation.getY() - radiusY,
                centerLocation.getZ() - radiusZ
        );

        final BlockVector3 maxPoint = BlockVector3.at(
                centerLocation.getX() + radiusX,
                centerLocation.getY() + radiusY,
                centerLocation.getZ() + radiusZ
        );

        return createRegion(centerLocation.getWorld(), regionName, minPoint, maxPoint);
    }

    public static ProtectedRegion createRegion(final Location location1,
                                               final Location location2,
                                               final String regionName) {
        Objects.requireNonNull(location1, "Location #1 can not be null!");
        Objects.requireNonNull(location2, "Location #2 can not be null!");
        Objects.requireNonNull(regionName, "Region name can not be null!");
        if (location1.getWorld() != location2.getWorld()) {
            throw new UnsupportedOperationException("Locations can not have different worlds!");
        }

        final BlockVector3 minPoint = BlockVector3.at(
                Math.min(location1.getX(), location2.getX()),
                Math.min(location1.getY(), location2.getY()),
                Math.min(location1.getZ(), location2.getZ())
        );

        final BlockVector3 maxPoint = BlockVector3.at(
                Math.max(location1.getX(), location2.getX()),
                Math.max(location1.getY(), location2.getY()),
                Math.max(location1.getZ(), location2.getZ())
        );

        return createRegion(location1.getWorld(), regionName, minPoint, maxPoint);
    }

    public static ProtectedRegion createRegion(final World world,
                                               final String regionName,
                                               final BlockVector3 minPoint,
                                               final BlockVector3 maxPoint) {
        final BukkitWorld bukkitWorld = new BukkitWorld(world);
        final RegionManager regionManager = REGION_CONTAINER.get(bukkitWorld);
        final ProtectedCuboidRegion protectedRegion = new ProtectedCuboidRegion(regionName, minPoint, maxPoint);
        regionManager.addRegion(protectedRegion);

        return protectedRegion;
    }

    @Nullable
    public static Set<ProtectedRegion> deleteRegion(final String regionName, final String worldName) {
        Objects.requireNonNull(regionName, "Region name can not be null!");
        Objects.requireNonNull(worldName, "World name can not be null!");

        return deleteRegion(regionName, Bukkit.getWorld(worldName));
    }

    @Nullable
    public static Set<ProtectedRegion> deleteRegion(final String regionName, final World world) {
        Objects.requireNonNull(regionName, "Region name can not be null!");
        Objects.requireNonNull(world, "World can not be null!");

        return deleteRegion(regionName, BukkitAdapter.adapt(world));
    }

    @Nullable
    public static Set<ProtectedRegion> deleteRegion(final String regionName, final com.sk89q.worldedit.world.World world) {
        final RegionManager regionManager = REGION_CONTAINER.get(world);
        if (regionManager.hasRegion(regionName)) {
            return regionManager.removeRegion(regionName);
        } else {
            throw new NoSuchElementException("Region with name \"" + regionName + "\" was not found!");
        }
    }

    @NotNull
    public static BlockVector3 getRegionCenter(final String regionName, final String worldName) {
        return getRegionCenter(getProtectedRegion(regionName, worldName));
    }

    @NotNull
    public static BlockVector3 getRegionCenter(final String regionName, final World world) {
        return getRegionCenter(getProtectedRegion(regionName, world));
    }

    @NotNull
    public static BlockVector3 getRegionCenter(final String regionName, final com.sk89q.worldedit.world.World world) {
        return getRegionCenter(getProtectedRegion(regionName, world));
    }

    @NotNull
    public static BlockVector3 getRegionCenter(final ProtectedRegion region) {
        Objects.requireNonNull(region, "Region can not be null!");

        final BlockVector3 minPoint = region.getMinimumPoint();
        final BlockVector3 maxPoint = region.getMaximumPoint();

        final double centerX = (minPoint.getX() + maxPoint.getX()) / 2.0;
        final double centerY = (minPoint.getY() + maxPoint.getY()) / 2.0;
        final double centerZ = (minPoint.getZ() + maxPoint.getZ()) / 2.0;

        return BlockVector3.at(centerX, centerY, centerZ);
    }

    @NotNull
    public static BlockVector3 getMinPoint(final String regionName, final String worldName) {
        return getMinPoint(getProtectedRegion(regionName, worldName));
    }

    @NotNull
    public static BlockVector3 getMinPoint(final String regionName, final World world) {
        return getMinPoint(getProtectedRegion(regionName, world));
    }

    @NotNull
    public static BlockVector3 getMinPoint(final String regionName, final com.sk89q.worldedit.world.World world) {
        return getMinPoint(getProtectedRegion(regionName, world));
    }

    @NotNull
    public static BlockVector3 getMinPoint(final ProtectedRegion region) {
        Objects.requireNonNull(region, "Region can not be null!");
        return region.getMinimumPoint();
    }

    @NotNull
    public static BlockVector3 getMaxPoint(final String regionName, final String worldName) {
        return getMaxPoint(getProtectedRegion(regionName, worldName));
    }

    @NotNull
    public static BlockVector3 getMaxPoint(final String regionName, final World world) {
        return getMaxPoint(getProtectedRegion(regionName, world));
    }

    @NotNull
    public static BlockVector3 getMaxPoint(final String regionName, final com.sk89q.worldedit.world.World world) {
        return getMaxPoint(getProtectedRegion(regionName, world));
    }

    @NotNull
    public static BlockVector3 getMaxPoint(final ProtectedRegion region) {
        Objects.requireNonNull(region, "Region can not be null!");
        return region.getMaximumPoint();
    }

    @Nullable
    public static ProtectedRegion getProtectedRegion(final String regionName, final String worldName) {
        Objects.requireNonNull(regionName, "Region name can not be null!");
        Objects.requireNonNull(worldName, "World name can not be null!");

        return getProtectedRegion(regionName, Bukkit.getWorld(worldName));
    }

    @Nullable
    public static ProtectedRegion getProtectedRegion(final String regionName, final World world) {
        Objects.requireNonNull(regionName, "Region name can not be null!");
        Objects.requireNonNull(world, "World can not be null!");

        return getProtectedRegion(regionName, BukkitAdapter.adapt(world));
    }

    @Nullable
    public static ProtectedRegion getProtectedRegion(final String regionName, final com.sk89q.worldedit.world.World world) {
        Objects.requireNonNull(regionName, "Region name can not be null!");
        Objects.requireNonNull(world, "World can not be null!");

        final RegionManager regionManager = REGION_CONTAINER.get(world);
        return regionManager.getRegion(regionName);
    }

    @ApiStatus.Experimental
    @NotNull
    public static Set<ProtectedRegion> getAllProtectedRegions(final UUID whoseUUID, final com.sk89q.worldedit.world.World world) {
        Objects.requireNonNull(whoseUUID, "Player UUID can not be null!");
        Objects.requireNonNull(world, "World UUID can not be null!");

        final RegionManager regionManager = REGION_CONTAINER.get(world);
        final HashSet<ProtectedRegion> regions = new HashSet<>();

        try {
            final Field field = regionManager.getClass().getDeclaredField("index");
            final ConcurrentRegionIndex index = (ConcurrentRegionIndex) field.get(regionManager);

            for (final ProtectedRegion region : index.values()) {
                if (isOwner(region, whoseUUID)) {
                    regions.add(region);
                }
            }
        } catch (final NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        return regions;
    }

    public static RegionManager getRegionManager(final World world) {
        return REGION_CONTAINER.get(BukkitAdapter.adapt(world));
    }

    public static RegionManager getRegionManager(final com.sk89q.worldedit.world.World world) {
        return REGION_CONTAINER.get(world);
    }

    public static int getRegionCount(final Player whose, final World world) {
        return getRegionCount(new BukkitPlayer(WorldGuardPlugin.inst(), whose), BukkitAdapter.adapt(world));
    }

    public static int getRegionCount(final BukkitPlayer whose, final com.sk89q.worldedit.world.World world) {
        return REGION_CONTAINER.get(world).getRegionCountOfPlayer(whose);
    }

    public static ApplicableRegionSet getOverlappingRegion(final ProtectedRegion region, final World world) {
        return REGION_CONTAINER.get(BukkitAdapter.adapt(world)).getApplicableRegions(region);
    }

    public static boolean isOwner(@NotNull ProtectedRegion region, @NotNull UUID playerUUID) {
        return checkRegion(region, (parent) -> parent.getOwners().contains(playerUUID));
    }

    public static boolean isMember(@NotNull ProtectedRegion region, @NotNull UUID playerUUID) {
        return checkRegion(region, (parent) -> parent.getMembers().contains(playerUUID));
    }

    public static boolean checkRegion(@NotNull ProtectedRegion region, @NotNull Predicate<ProtectedRegion> check) {
        for (ProtectedRegion parent = region;
             parent != null;
             parent = parent.getParent()) {
            if (check.test(parent)) {
                return true;
            }
        }

        return false;
    }

    static {
        REGION_CONTAINER = Bukkit.getPluginManager().isPluginEnabled("WorldGuard")
                ? WorldGuard.getInstance().getPlatform().getRegionContainer()
                : null;
    }
}
