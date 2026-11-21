package com.github.groundbreakingmc.mylib.database.kv.encoders;

import com.github.groundbreakingmc.mylib.database.kv.KeyValueEncoder;

import java.util.UUID;

import static com.github.groundbreakingmc.mylib.database.kv.encoders.utils.ByteUtils.bytesToLong;
import static com.github.groundbreakingmc.mylib.database.kv.encoders.utils.ByteUtils.putLong;

public final class UUIDEncoder implements KeyValueEncoder<UUID> {

    @Override
    public byte[] encode(UUID value) {
        byte[] result = new byte[16];
        putLong(result, 0, value.getMostSignificantBits());
        putLong(result, 8, value.getLeastSignificantBits());
        return result;
    }

    @Override
    public UUID decode(byte[] data) {
        return new UUID(
                bytesToLong(data, 0),
                bytesToLong(data, 8)
        );
    }
}
