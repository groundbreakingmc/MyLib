package com.github.groundbreakingmc.mylib.database.kv.encoders;

import com.github.groundbreakingmc.mylib.database.kv.KeyValueEncoder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

import static com.github.groundbreakingmc.mylib.database.kv.encoders.utils.ByteUtils.*;

public final class LocationEncoder implements KeyValueEncoder<Location> {

    private final UUIDEncoder uuidEncoder = new UUIDEncoder();

    @Override
    public byte[] encode(Location location) {
        final byte[] result = new byte[16 + 8 + 8 + 8 + 4 + 4]; // UUID + x + y + z + yaw + pitch

        int offset = 0;

        // world UUID (16 bytes)
        final byte[] worldBytes = this.uuidEncoder.encode(location.getWorld().getUID());
        System.arraycopy(worldBytes, 0, result, offset, 16);
        offset += 16;

        // coordinates (x, y, z as doubles = 24 bytes)
        putDouble(result, offset, location.getX());
        offset += 8;
        putDouble(result, offset, location.getY());
        offset += 8;
        putDouble(result, offset, location.getZ());
        offset += 8;

        // rotation (yaw, pitch as floats = 8 bytes)
        putFloat(result, offset, location.getYaw());
        offset += 4;
        putFloat(result, offset, location.getPitch());

        return result;
    }

    @Override
    public Location decode(byte[] data) {
        int offset = 0;

        // decode world UUID
        final byte[] worldBytes = new byte[16];
        System.arraycopy(data, offset, worldBytes, 0, 16);
        final UUID worldId = this.uuidEncoder.decode(worldBytes);
        offset += 16;

        final World world = Bukkit.getWorld(worldId);
        if (world == null) {
            return null;
        }

        // decode coordinates
        final double x = bytesToDouble(data, offset);
        offset += 8;
        final double y = bytesToDouble(data, offset);
        offset += 8;
        final double z = bytesToDouble(data, offset);
        offset += 8;

        final float yaw = bytesToFloat(data, offset);
        offset += 4;
        final float pitch = bytesToFloat(data, offset);

        return new Location(world, x, y, z, yaw, pitch);
    }
}
