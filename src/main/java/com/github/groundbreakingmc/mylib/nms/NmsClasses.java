package com.github.groundbreakingmc.mylib.nms;

import com.github.groundbreakingmc.mylib.reflect.JavaInvoker;

import java.util.function.Function;

/**
 * Registry of commonly used NMS/OBC classes and pre-built frequent operations.
 */
public final class NmsClasses {

    // ─── World ────────────────────────────────────────────────────────────────
    private static volatile Class<?> entity;
    private static volatile Class<?> entityPlayer;
    private static volatile Class<?> serverLevel;

    // ─── Server ───────────────────────────────────────────────────────────────
    private static volatile Class<?> minecraftServer;

    // ─── CraftBukkit ──────────────────────────────────────────────────────────
    private static volatile Class<?> craftEntity;
    private static volatile Class<?> craftPlayer;
    private static volatile Class<?> craftWorld;

    // ─── Pre-built operations ─────────────────────────────────────────────────
    private static volatile Function<Object, Object> getEntityHandle;

    private NmsClasses() {
    }

    public static Class<?> entity() {
        if (entity == null) entity = requireNms("world.entity.Entity", "Entity");
        return entity;
    }

    public static Class<?> entityPlayer() {
        if (entityPlayer == null) entityPlayer = requireNms("server.level.ServerPlayer", "EntityPlayer");
        return entityPlayer;
    }

    public static Class<?> serverLevel() {
        if (serverLevel == null) serverLevel = requireNms("server.level.ServerLevel", "WorldServer");
        return serverLevel;
    }

    public static Class<?> minecraftServer() {
        if (minecraftServer == null) minecraftServer = requireNms("server.MinecraftServer", "MinecraftServer");
        return minecraftServer;
    }

    public static Class<?> craftEntity() {
        if (craftEntity == null) craftEntity = requireObc("entity.CraftEntity");
        return craftEntity;
    }

    public static Class<?> craftPlayer() {
        if (craftPlayer == null) craftPlayer = requireObc("entity.CraftPlayer");
        return craftPlayer;
    }

    public static Class<?> craftWorld() {
        if (craftWorld == null) craftWorld = requireObc("CraftWorld");
        return craftWorld;
    }

    // ─── Common operations ────────────────────────────────────────────────────

    /**
     * CraftEntity/CraftPlayer → NMS Entity handle
     */
    public static Object getHandle(Object craftEntity) {
        if (getEntityHandle == null) {
            getEntityHandle = JavaInvoker
                    .virtual(craftEntity(), "getHandle", entity())
                    .asFunction();
        }
        return getEntityHandle.apply(craftEntity);
    }

    /**
     * Convenience overload for Bukkit API entity
     */
    public static Object getHandle(org.bukkit.entity.Entity entity) {
        return getHandle((Object) entity);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static Class<?> requireNms(String modern, String legacy) {
        Class<?> c = NmsUtils.nmsClass(modern, legacy);
        if (c == null) throw new IllegalStateException("NMS class not found: " + modern + " / " + legacy);
        return c;
    }

    private static Class<?> requireObc(String name) {
        Class<?> c = NmsUtils.obcClass(name);
        if (c == null) throw new IllegalStateException("OBC class not found: " + name);
        return c;
    }
}
