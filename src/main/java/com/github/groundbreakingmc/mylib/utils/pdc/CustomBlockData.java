package com.github.groundbreakingmc.mylib.utils.pdc;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class CustomBlockData {

    private final Block block;
    private final NamespacedKey containerKey;

    public CustomBlockData(@NotNull String namespace, @NotNull Block block) {
        this.block = block;
        this.containerKey = PDCUtils.getBlockKey(namespace, block);
    }

    public <T, Z> Z get(@NotNull NamespacedKey dataKey, @NotNull PersistentDataType<T, Z> dataType) {
        return PDCUtils.getBlockData(this.block.getChunk(), this.containerKey, dataKey, dataType);
    }

    public <T, Z> void set(@NotNull NamespacedKey dataKey,
                           @NotNull PersistentDataType<T, Z> dataType,
                           @NotNull Z value) {
        PDCUtils.setBlockData(this.block.getChunk(), this.containerKey, dataKey, dataType, value);
    }

    public void remove(@NotNull NamespacedKey dataKey) {
        PDCUtils.removeBlockData(this.block.getChunk(), this.containerKey, dataKey);
    }
}
