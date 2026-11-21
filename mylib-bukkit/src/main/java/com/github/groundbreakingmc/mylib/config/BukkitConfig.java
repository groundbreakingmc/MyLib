package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.config.exception.SerializerNotFoundException;
import com.github.groundbreakingmc.mylib.config.serializer.bukkit.*;
import com.github.groundbreakingmc.mylib.config.source.ConfigSource;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Config} subclass for Bukkit platforms.
 *
 * <p>Automatically registers serializers for:
 * {@link Location}, {@link Material}, {@link Sound}, {@link World}, {@link PotionEffectType}.
 * User-supplied serializers (registered via {@link SerializerRegistry#register} before construction)
 * are never overwritten.
 *
 * <p>Sub-sections returned by {@link #findSection} / {@link #sectionOr} are also
 * {@code BukkitConfig} instances — the {@link #wrap} override ensures that.
 */
public final class BukkitConfig extends Config {

    public BukkitConfig(ConfigSource source, SerializerRegistry registry) {
        super(source, registry);
        this.registerBuiltins();
    }

    // ── Auto-registration ─────────────────────────────────────────────────────

    private void registerBuiltins() {
        this.registry.registerIfAbsent(Location.class, LocationSerializer.INSTANCE);
        this.registry.registerIfAbsent(Material.class, MaterialSerializer.INSTANCE);
        this.registry.registerIfAbsent(Sound.class, SoundSerializer.INSTANCE);
        this.registry.registerIfAbsent(World.class, WorldSerializer.INSTANCE);
        this.registry.registerIfAbsent(PotionEffectType.class, PotionEffectTypeSerializer.INSTANCE);
    }

    // ── Sub-sections stay BukkitConfig ────────────────────────────────────────

    @Override
    protected Config wrap(ConfigSource source) {
        return new BukkitConfig(source, this.registry);
    }

    // ── Location ──────────────────────────────────────────────────────────────

    public Location findLocation(String path) {
        return this.find(Location.class, path);
    }

    public Location locationOr(String path, Location def) {
        return this.getOr(Location.class, path, def);
    }

    // ── Material ──────────────────────────────────────────────────────────────

    public Material findMaterial(String path) {
        return this.find(Material.class, path);
    }

    public Material materialOr(String path, Material def) {
        return this.getOr(Material.class, path, def);
    }

    @Nullable
    public List<Material> materialList(String path) {
        return this.deserializeList(Material.class, path);
    }

    public List<Material> materialListOr(String path, List<Material> def) {
        final List<Material> value = this.materialList(path);
        return value != null ? value : def;
    }

    // ── Sound ─────────────────────────────────────────────────────────────────

    public Sound findSound(String path) {
        return this.find(Sound.class, path);
    }

    public Sound soundOr(String path, Sound def) {
        return this.getOr(Sound.class, path, def);
    }

    @Nullable
    public List<Sound> soundList(String path) {
        return this.deserializeList(Sound.class, path);
    }

    public List<Sound> soundListOr(String path, List<Sound> def) {
        final List<Sound> value = this.soundList(path);
        return value != null ? value : def;
    }

    // ── World ─────────────────────────────────────────────────────────────────

    public World findWorld(String path) {
        return this.find(World.class, path);
    }

    public World worldOr(String path, World def) {
        return this.getOr(World.class, path, def);
    }

    @Nullable
    public List<World> worldList(String path) {
        return this.deserializeList(World.class, path);
    }

    public List<World> worldListOr(String path, List<World> def) {
        final List<World> value = this.worldList(path);
        return value != null ? value : def;
    }

    // ── PotionEffectType ──────────────────────────────────────────────────────

    public PotionEffectType findEffect(String path) {
        return this.find(PotionEffectType.class, path);
    }

    public PotionEffectType effectOr(String path, PotionEffectType def) {
        return this.getOr(PotionEffectType.class, path, def);
    }

    @Nullable
    public List<PotionEffectType> effectList(String path) {
        return this.deserializeList(PotionEffectType.class, path);
    }

    public List<PotionEffectType> effectListOr(String path, List<PotionEffectType> def) {
        final List<PotionEffectType> value = this.effectList(path);
        return value != null ? value : def;
    }

    // ── Shared list helper ────────────────────────────────────────────────────

    @Nullable
    private <T> List<T> deserializeList(Class<T> type, String path) {
        final List<?> raw = this.source.list(path);
        if (raw == null) return null;

        final ConfigSerializer<T> serializer = this.registry.get(type);
        if (serializer == null) throw new SerializerNotFoundException(path, type);

        final List<T> result = new ArrayList<>(raw.size());
        for (final Object element : raw) {
            if (element == null) continue;
            final T value = serializer.deserialize(element, path);
            if (value != null) result.add(value);
        }
        return result;
    }
}
