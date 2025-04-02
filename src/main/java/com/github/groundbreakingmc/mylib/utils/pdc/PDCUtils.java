package com.github.groundbreakingmc.mylib.utils.pdc;

import lombok.experimental.UtilityClass;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@UtilityClass
public class PDCUtils {

    private final int X_PRIME = 40;
    private final int Y_PRIME = 20;

    @Nullable
    public <T, Z> Z getBlockData(@NotNull Block block,
                                 @NotNull String namespace,
                                 @NotNull NamespacedKey dataKey,
                                 @NotNull PersistentDataType<T, Z> dataType) {
        return getBlockData(block.getChunk(), getBlockKey(namespace, block), dataKey, dataType);
    }

    @Nullable
    public <T, Z> Z getBlockData(@NotNull Chunk chunk,
                                 @NotNull NamespacedKey containerKey,
                                 @NotNull NamespacedKey dataKey,
                                 @NotNull PersistentDataType<T, Z> dataType) {
        final PersistentDataContainer blockContainer = getContainerForRead(chunk, containerKey);
        return blockContainer != null ? blockContainer.get(dataKey, dataType) : null;
    }

    public <T, Z> void setBlockData(@NotNull Block block,
                                    @NotNull String namespace,
                                    @NotNull NamespacedKey dataKey,
                                    @NotNull PersistentDataType<T, Z> dataType,
                                    @NotNull Z value) {
        setBlockData(block.getChunk(), getBlockKey(namespace, block), dataKey, dataType, value);
    }

    public <T, Z> void setBlockData(@NotNull Chunk chunk,
                                    @NotNull NamespacedKey containerKey,
                                    @NotNull NamespacedKey dataKey,
                                    @NotNull PersistentDataType<T, Z> dataType,
                                    @NotNull Z value) {
        final PersistentDataContainer chunkContainer = chunk.getPersistentDataContainer();
        final PersistentDataContainer blockContainer = getContainerForWrite(chunk, containerKey);

        blockContainer.set(dataKey, dataType, value);

        if (!blockContainer.isEmpty()) {
            chunkContainer.set(containerKey, PersistentDataType.TAG_CONTAINER, blockContainer);
        } else {
            chunkContainer.remove(containerKey);
        }
    }

    public void removeBlockData(@NotNull Block block,
                                @NotNull String namespace,
                                @NotNull NamespacedKey dataKey) {
        removeBlockData(block.getChunk(), getBlockKey(namespace, block), dataKey);
    }

    public void removeBlockData(@NotNull Chunk chunk,
                                @NotNull NamespacedKey containerKey,
                                @NotNull NamespacedKey dataKey) {
        final PersistentDataContainer blockContainer = getContainerForRead(chunk, containerKey);
        if (blockContainer != null) {
            blockContainer.remove(dataKey);

            final PersistentDataContainer chunkContainer = chunk.getPersistentDataContainer();
            if (!blockContainer.isEmpty()) {
                chunkContainer.set(containerKey, PersistentDataType.TAG_CONTAINER, blockContainer);
            } else {
                chunkContainer.remove(containerKey);
            }
        }
    }

    @NotNull
    public NamespacedKey getBlockKey(String namespace, Block block) {
        return new NamespacedKey(namespace, generateBlockHash(block));
    }

    @Nullable
    public PersistentDataContainer getContainerForRead(@NotNull Block block, @NotNull String namespace) {
        return getContainerForRead(block.getChunk(), getBlockKey(namespace, block));
    }

    @Nullable
    public PersistentDataContainer getContainerForRead(@NotNull Chunk chunk,
                                                       @NotNull NamespacedKey containerKey) {
        return chunk.getPersistentDataContainer()
                .get(containerKey, PersistentDataType.TAG_CONTAINER);
    }

    @NotNull
    public PersistentDataContainer getContainerForWrite(@NotNull Block block, @NotNull String namespace) {
        return getContainerForWrite(block.getChunk(), getBlockKey(namespace, block));
    }

    @NotNull
    public PersistentDataContainer getContainerForWrite(@NotNull Chunk chunk,
                                                        @NotNull NamespacedKey containerKey) {
        final PersistentDataContainer chunkContainer = chunk.getPersistentDataContainer();
        final PersistentDataContainer blockContainer = chunkContainer.get(containerKey, PersistentDataType.TAG_CONTAINER);
        return blockContainer != null ? blockContainer : chunkContainer.getAdapterContext().newPersistentDataContainer();
    }

    @NotNull
    public String generateBlockHash(@NotNull Block block) {
        return Long.toString((((long) block.getX() & 0xFFFFFFL) << X_PRIME)
                | (((long) block.getY() & 0xFFFFFL) << Y_PRIME)
                | ((long) block.getZ() & 0xFFFFFL)
        );
    }
}
