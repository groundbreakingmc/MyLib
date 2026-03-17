package com.github.groundbreakingmc.mylib.database.kv.encoders;

import com.github.groundbreakingmc.mylib.database.kv.KeyValueEncoder;
import org.bukkit.inventory.ItemStack;

public final class ItemStackEncoder implements KeyValueEncoder<ItemStack> {

    @Override
    public byte[] encode(ItemStack item) {
        return item.serializeAsBytes();
    }

    @Override
    public ItemStack decode(byte[] data) {
        return ItemStack.deserializeBytes(data);
    }
}
