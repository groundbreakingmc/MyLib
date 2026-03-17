package com.github.groundbreakingmc.mylib.database.kv.encoders;

import com.github.groundbreakingmc.mylib.database.kv.KeyValueEncoder;
import org.bukkit.inventory.ItemStack;

import static com.github.groundbreakingmc.mylib.database.kv.encoders.utils.ByteUtils.bytesToInt;
import static com.github.groundbreakingmc.mylib.database.kv.encoders.utils.ByteUtils.putInt;

public final class ItemStackArrayEncoder implements KeyValueEncoder<ItemStack[]> {

    private final ItemStackEncoder itemStackEncoder = new ItemStackEncoder();

    @Override
    public byte[] encode(ItemStack[] items) {
        final byte[][] serializedItems = new byte[items.length][];
        int totalAmount = 0;
        int totalSize = 0;

        for (int i = 0; i < items.length; i++) {
            final ItemStack item = items[i];
            if (item != null) {
                final byte[] itemBytes = this.itemStackEncoder.encode(item);
                serializedItems[i] = itemBytes;
                totalAmount++;
                totalSize += itemBytes.length;
            }
        }

        int offset = 0;

        final byte[] bytes = new byte[
                4 +                         // array size
                        (4 * totalAmount) + // slot indices
                        (4 * totalAmount) + // item lengths
                        totalSize           // item data
                ];

        putInt(bytes, offset, items.length);
        offset += 4;

        for (int i = 0; i < serializedItems.length; i++) {
            final byte[] itemBytes = serializedItems[i];
            if (itemBytes != null) {
                putInt(bytes, offset, i);
                offset += 4;
                putInt(bytes, offset, itemBytes.length);
                offset += 4;
                System.arraycopy(itemBytes, 0, bytes, offset, itemBytes.length);
                offset += itemBytes.length;
            }
        }

        return bytes;
    }

    @Override
    public ItemStack[] decode(byte[] data) {
        int offset = 0;

        int arraySize = bytesToInt(data, offset);
        offset += 4;

        final ItemStack[] items = new ItemStack[arraySize];

        while (offset < data.length) {
            final int slot = bytesToInt(data, offset);
            offset += 4;

            final int length = bytesToInt(data, offset);
            offset += 4;

            final byte[] itemBytes = new byte[length];
            System.arraycopy(data, offset, itemBytes, 0, length);
            offset += length;

            items[slot] = this.itemStackEncoder.decode(itemBytes);
        }

        return items;
    }
}
