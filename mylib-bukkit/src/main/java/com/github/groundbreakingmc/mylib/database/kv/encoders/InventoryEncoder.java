package com.github.groundbreakingmc.mylib.database.kv.encoders;

import com.github.groundbreakingmc.mylib.database.kv.KeyValueEncoder;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

import static com.github.groundbreakingmc.mylib.database.kv.encoders.utils.ByteUtils.bytesToInt;
import static com.github.groundbreakingmc.mylib.database.kv.encoders.utils.ByteUtils.putInt;

public final class InventoryEncoder implements KeyValueEncoder<Inventory> {

    private final ItemStackEncoder itemStackEncoder = new ItemStackEncoder();

    @Override
    public byte[] encode(Inventory inventory) {
        final byte[] typeBytes = inventory.getType().name().getBytes(StandardCharsets.UTF_8);
        final @Nullable ItemStack[] contents = inventory.getContents();
        final byte[][] serializedItems = new byte[contents.length][];
        int totalAmount = 0;
        int totalSize = 0;

        for (int i = 0; i < contents.length; i++) {
            final ItemStack item = contents[i];
            if (item != null) {
                final byte[] itemBytes = this.itemStackEncoder.encode(item);
                serializedItems[i] = itemBytes;
                totalAmount++;
                totalSize += itemBytes.length;
            }
        }

        int offset = 0;

        final byte[] bytes = new byte[
                4 + typeBytes.length + 4 +  // type name length + type name + inventory size
                        (4 * totalAmount) + // slot indices
                        (4 * totalAmount) + // item lengths
                        totalSize           // item data
                ];

        putInt(bytes, offset, typeBytes.length);
        offset += 4;
        System.arraycopy(typeBytes, 0, bytes, offset, typeBytes.length);
        offset += typeBytes.length;

        putInt(bytes, offset, inventory.getSize());
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
    public Inventory decode(byte[] data) {
        int offset = 0;

        int typeLength = bytesToInt(data, offset);
        offset += 4;
        String typeName = new String(data, offset, typeLength, StandardCharsets.UTF_8);
        offset += typeLength;

        int invSize = bytesToInt(data, offset);
        offset += 4;

        final InventoryType type = InventoryType.valueOf(typeName);
        final Inventory inv = type == InventoryType.CHEST
                ? Bukkit.createInventory(null, invSize)
                : Bukkit.createInventory(null, type);

        while (offset < data.length) {
            final int slot = bytesToInt(data, offset);
            offset += 4;

            final int length = bytesToInt(data, offset);
            offset += 4;

            final byte[] itemBytes = new byte[length];
            System.arraycopy(data, offset, itemBytes, 0, length);
            offset += length;

            inv.setItem(slot, this.itemStackEncoder.decode(itemBytes));
        }

        return inv;
    }
}
