package com.github.groundbreakingmc.mylib.database.kv.encoders;

import com.github.groundbreakingmc.mylib.database.kv.KeyValueEncoder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public final class PlayerEncoder implements KeyValueEncoder<OfflinePlayer> {

    private final UUIDEncoder uuidEncoder = new UUIDEncoder();

    @Override
    public byte[] encode(OfflinePlayer value) {
        return this.uuidEncoder.encode(value.getUniqueId());
    }

    @Override
    public OfflinePlayer decode(byte[] data) {
        return Bukkit.getOfflinePlayer(
                this.uuidEncoder.decode(data)
        );
    }
}
