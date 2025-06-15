package com.github.groundbreakingmc.mylib.utils.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
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
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@SuppressWarnings("unused")
@UtilityClass
public final class WorldGuardUtils {

    public static final RegionContainer REGION_CONTAINER;
    private static final Field INDEX_FIELD;

    /**
     * Gets the applicable regions at the given Bukkit {@link Location}.
     *
     * @param location the location to check
     * @return the applicable region set, or {@code null} if no region manager is available
     */
    public @Nullable ApplicableRegionSet getApplicableRegions(@NotNull("Location can not be null, but it is!") Location location) {
        return getApplicableRegions(BukkitAdapter.adapt(location.getWorld()), BukkitAdapter.asBlockVector(location));
    }

    /**
     * Gets the applicable regions at a specific block position in a WorldEdit world.
     *
     * @param world        the WorldEdit world
     * @param blockVector3 the block position
     * @return the applicable region set, or {@code null} if no region manager is available
     */
    public @Nullable ApplicableRegionSet getApplicableRegions(@NotNull("World can not be null, but it is!") com.sk89q.worldedit.world.World world,
                                                              @NotNull("Block vector can not be null, but it is!") BlockVector3 blockVector3) {
        final RegionManager regionManager = REGION_CONTAINER.get(world);
        if (regionManager == null || regionManager.getRegions().isEmpty()) {
            return null;
        }

        return regionManager.getApplicableRegions(blockVector3);
    }

    /**
     * Creates a cuboid region centered at the given location with the same radius in all directions.
     *
     * @param regionName     the name of the region
     * @param centerLocation the center location
     * @param radius         the radius in each direction (X, Y, Z)
     * @return the created region
     */
    public @NotNull ProtectedRegion createRegion(@NotNull("Region name can not be null, but it is!") String regionName,
                                                 @NotNull("Location can not be null, but it is!") Location centerLocation,
                                                 int radius) {
        return createRegion(regionName, centerLocation, radius, radius, radius);
    }

    /**
     * Creates a cuboid region centered at the given location with custom radii for each axis.
     *
     * @param regionName     the name of the region
     * @param centerLocation the center location
     * @param radiusX        radius on the X axis
     * @param radiusY        radius on the Y axis
     * @param radiusZ        radius on the Z axis
     * @return the created region
     */
    public @NotNull ProtectedRegion createRegion(@NotNull("Region name can not be null, but it is!") String regionName,
                                                 @NotNull("Location can not be null, but it is!") Location centerLocation,
                                                 int radiusX, int radiusY, int radiusZ) {
        return createRegion(regionName, centerLocation.subtract(radiusX, radiusY, radiusZ), centerLocation.add(radiusX, radiusY, radiusZ));
    }

    /**
     * Creates a cuboid region between two Bukkit locations.
     *
     * @param regionName the name of the region
     * @param location1  the first corner
     * @param location2  the second corner
     * @return the created region
     * @throws UnsupportedOperationException if the locations are in different worlds
     */
    public @NotNull ProtectedRegion createRegion(@NotNull("Region name can not be null, but it is!") String regionName,
                                                 @NotNull("Location #1 can not be null, but it is!") Location location1,
                                                 @NotNull("Location #2 can not be null, but it is!") Location location2) {
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

        return createRegion(regionName, location1.getWorld(), minPoint, maxPoint);
    }

    /**
     * Creates a cuboid region between two block points in a given world.
     *
     * @param regionName the name of the region
     * @param world      the Bukkit world
     * @param minPoint   the minimum (lower) block point
     * @param maxPoint   the maximum (upper) block point
     * @return the created region
     */
    public @NotNull ProtectedRegion createRegion(@NotNull("Region name can not be null, but it is!") String regionName,
                                                 @NotNull("World can not be null, but it is!") World world,
                                                 @NotNull("Min point can not be null, but it is!") BlockVector3 minPoint,
                                                 @NotNull("Max point can not be null, but it is!") BlockVector3 maxPoint) {
        final RegionManager regionManager = getRegionManagerSafe(world);

        final ProtectedCuboidRegion protectedRegion = new ProtectedCuboidRegion(regionName, minPoint, maxPoint);
        regionManager.addRegion(protectedRegion);

        return protectedRegion;
    }

    /**
     * Deletes a region by name from a Bukkit world.
     *
     * @param regionName the name of the region
     * @param world      the Bukkit world
     * @return the removed region(s), or {@code null} if not found
     * @throws NoSuchElementException if the region does not exist
     */
    public @Nullable Set<ProtectedRegion> deleteRegion(@NotNull("Region name can not be null, but it is!") String regionName,
                                                       @NotNull("World can not be null, but it is!") World world) {
        return deleteRegion(regionName, BukkitAdapter.adapt(world));
    }

    /**
     * Deletes a region by name from a WorldEdit world.
     *
     * @param regionName the name of the region
     * @param world      the WorldEdit world
     * @return the removed region(s), or {@code null} if not found
     * @throws NoSuchElementException if the region does not exist
     */
    public @Nullable Set<ProtectedRegion> deleteRegion(@NotNull("Region name can not be null, but it is!") String regionName,
                                                       @NotNull("World can not be null, but it is!") com.sk89q.worldedit.world.World world) {
        final RegionManager regionManager = getRegionManagerSafe(world);
        if (regionManager.hasRegion(regionName)) {
            return regionManager.removeRegion(regionName);
        } else {
            throw new NoSuchElementException("Region with name \"" + regionName + "\" was not found!");
        }
    }

    /**
     * Calculates the center of a region based on its min and max points.
     *
     * @param region the region
     * @return the center as a {@link BlockVector3}
     */
    public @NotNull BlockVector3 getRegionCenter(@NotNull("Region can not be null, but it is!") ProtectedRegion region) {
        final BlockVector3 minPoint = region.getMinimumPoint();
        final BlockVector3 maxPoint = region.getMaximumPoint();

        final double centerX = (minPoint.getX() + maxPoint.getX()) / 2.0;
        final double centerY = (minPoint.getY() + maxPoint.getY()) / 2.0;
        final double centerZ = (minPoint.getZ() + maxPoint.getZ()) / 2.0;

        return BlockVector3.at(centerX, centerY, centerZ);
    }

    /**
     * Retrieves a region by name from a Bukkit world.
     *
     * @param regionName the name of the region
     * @param world      the Bukkit world
     * @return the found region, or {@code null} if not found
     */
    public @Nullable ProtectedRegion getProtectedRegion(@NotNull("Region name can not be null, but it is!") String regionName,
                                                        @NotNull("World can not be null, but it is!") World world) {
        return getProtectedRegion(regionName, BukkitAdapter.adapt(world));
    }

    /**
     * Retrieves a region by name from a WorldEdit world.
     *
     * @param regionName the name of the region
     * @param world      the WorldEdit world
     * @return the found region, or {@code null} if not found
     */
    public @Nullable ProtectedRegion getProtectedRegion(@NotNull("Region name can not be null, but it is!") String regionName,
                                                        @NotNull("World can not be null, but it is!") com.sk89q.worldedit.world.World world) {
        final RegionManager regionManager = getRegionManagerSafe(world);
        return regionManager.getRegion(regionName);
    }

    /**
     * Retrieves all regions in a world that are owned by a specific player.
     *
     * @param whoseUUID the UUID of the player
     * @param world     the WorldEdit world
     * @return the set of regions owned by the player
     */
    public @NotNull Set<ProtectedRegion> getAllProtectedRegions(@NotNull("Whose UUID can not be null, but it is!") UUID whoseUUID,
                                                                @NotNull("World can not be null, but it is!") com.sk89q.worldedit.world.World world) {
        final RegionManager regionManager = getRegionManagerSafe(world);
        final Set<ProtectedRegion> regions = new ObjectOpenHashSet<>();

        try {
            final ConcurrentRegionIndex index = (ConcurrentRegionIndex) INDEX_FIELD.get(regionManager);
            for (final ProtectedRegion region : index.values()) {
                if (isOwner(region, whoseUUID)) {
                    regions.add(region);
                }
            }
        } catch (final IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }

        return regions;
    }

    /**
     * Gets the {@link RegionManager} for a Bukkit world.
     *
     * @param world the Bukkit world
     * @return the region manager, or {@code null} if not available
     */
    public @Nullable RegionManager getRegionManager(@NotNull("World can not be null, but it is!") World world) {
        return REGION_CONTAINER.get(BukkitAdapter.adapt(world));
    }

    /**
     * Gets the {@link RegionManager} for a WorldEdit world.
     *
     * @param world the WorldEdit world
     * @return the region manager, or {@code null} if not available
     */
    public @Nullable RegionManager getRegionManager(@NotNull("World can not be null, but it is!") com.sk89q.worldedit.world.World world) {
        return REGION_CONTAINER.get(world);
    }

    /**
     * Gets the {@link RegionManager} for a Bukkit world, throwing if missing.
     *
     * @param world the Bukkit world
     * @return the region manager
     * @throws UnsupportedOperationException if the manager is not available
     */
    public @NotNull RegionManager getRegionManagerSafe(@NotNull("World can not be null, but it is!") World world) {
        return getRegionManagerSafe(BukkitAdapter.adapt(world));
    }

    /**
     * Gets the {@link RegionManager} for a WorldEdit world, throwing if missing.
     *
     * @param world the WorldEdit world
     * @return the region manager
     * @throws UnsupportedOperationException if the manager is not available
     */
    public @NotNull RegionManager getRegionManagerSafe(@NotNull("World can not be null, but it is!") com.sk89q.worldedit.world.World world) {
        final RegionManager regionManager = REGION_CONTAINER.get(world);
        if (regionManager == null) {
            throw new UnsupportedOperationException("Region manager is not available for world: " + world.getName());
        }

        return regionManager;
    }

    /**
     * Gets the number of regions a Bukkit player owns in a world.
     *
     * @param whose the player
     * @param world the Bukkit world
     * @return the number of regions
     */
    public int getRegionCount(@NotNull("Player can not be null, but it is!") Player whose,
                              @NotNull("World can not be null, but it is!") World world) {
        return getRegionCount(new BukkitPlayer(WorldGuardPlugin.inst(), whose), BukkitAdapter.adapt(world));
    }

    /**
     * Gets the number of regions a WorldGuard player owns in a world.
     *
     * @param whose the WorldGuard player
     * @param world the WorldEdit world
     * @return the number of regions
     */
    public int getRegionCount(@NotNull("Player can not be null, but it is!") BukkitPlayer whose,
                              @NotNull("World can not be null, but it is!") com.sk89q.worldedit.world.World world) {
        return getRegionManagerSafe(world).getRegionCountOfPlayer(whose);
    }

    /**
     * Gets all regions overlapping a given region in a Bukkit world.
     *
     * @param region the target region
     * @param world  the Bukkit world
     * @return the applicable region set
     */
    public @NotNull ApplicableRegionSet getOverlappingRegion(@NotNull("Region can not be null, but it is!") ProtectedRegion region,
                                                             @NotNull("World can not be null, but it is!") World world) {
        return getRegionManagerSafe(world).getApplicableRegions(region);
    }

    /**
     * Checks whether a player is the owner of the region or any of its parent regions.
     *
     * @param region     the region
     * @param playerUUID the UUID of the player
     * @return {@code true} if the player is the owner
     */
    public boolean isOwner(@NotNull("Region can not be null, but it is!") ProtectedRegion region,
                           @NotNull("Player UUID can not be null, but it is!") UUID playerUUID) {
        return checkRegion(region, (parent) -> parent.getOwners().contains(playerUUID));
    }

    /**
     * Checks whether a player is a member of the region or any of its parent regions.
     *
     * @param region     the region
     * @param playerUUID the UUID of the player
     * @return {@code true} if the player is a member
     */
    public boolean isMember(@NotNull("Region can not be null, but it is!") ProtectedRegion region,
                            @NotNull("Player UUID can not be null, but it is!") UUID playerUUID) {
        return checkRegion(region, (parent) -> parent.getMembers().contains(playerUUID));
    }

    /**
     * Recursively checks a condition on the region and its parent hierarchy.
     *
     * @param region the region
     * @param check  a predicate to apply to each region
     * @return {@code true} if any region in the hierarchy passes the check
     */
    public boolean checkRegion(@NotNull("Region can not be null, but it is!") ProtectedRegion region,
                               @NotNull("Check can not be null, but it is!") Predicate<ProtectedRegion> check) {
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

        try {
            INDEX_FIELD = RegionManager.class.getDeclaredField("index");
            INDEX_FIELD.setAccessible(true);
        } catch (final NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }
}
