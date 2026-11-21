package com.github.groundbreakingmc.mylib.database.kv.encoders;

import com.github.groundbreakingmc.mylib.database.kv.KeyValueEncoder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.UUID;

import static com.github.groundbreakingmc.mylib.database.kv.encoders.utils.ByteUtils.*;

public final class BlockEncoder implements KeyValueEncoder<Block> {

    @Override
    public byte[] encode(Block block) {
        byte[] result = new byte[8 + 4 + 4 + 4];

        int offset = 0;

        // world UUID (2 long)
        UUID worldId = block.getWorld().getUID();
        putLong(result, offset, worldId.getMostSignificantBits());
        offset += 8;
        putLong(result, offset, worldId.getLeastSignificantBits());
        offset += 8;

        // coords
        putInt(result, offset, block.getX());
        offset += 4;
        putInt(result, offset, block.getY());
        offset += 4;
        putInt(result, offset, block.getZ());

        return result;
    }

    @Override
    public Block decode(byte[] data) {

        int offset = 0;

        long msb = bytesToLong(data, offset);
        offset += 8;

        long lsb = bytesToLong(data, offset);
        offset += 8;

        final World world = Bukkit.getWorld(new UUID(msb, lsb));

        final int x = bytesToInt(data, offset);
        offset += 4;

        final int y = bytesToInt(data, offset);
        offset += 4;

        final int z = bytesToInt(data, offset);

        return world != null ? world.getBlockAt(x, y, z) : null;
    }
}
