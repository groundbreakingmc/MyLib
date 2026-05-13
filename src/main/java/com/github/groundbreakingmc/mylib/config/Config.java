package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.config.exception.ConfigMissingPathException;
import com.github.groundbreakingmc.mylib.config.exception.SerializerNotFoundException;
import com.github.groundbreakingmc.mylib.config.source.ConfigSource;
import com.github.groundbreakingmc.mylib.config.source.MapSource;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Platform-agnostic config wrapper.
 *
 * <p>Delegates raw reads to a {@link ConfigSource} (the backend layer),
 * and exposes two access patterns for every type:
 * <ul>
 *   <li>{@code findX(path)} — returns the value, throws {@link ConfigMissingPathException} if absent.</li>
 *   <li>{@code xOr(path, def)} — returns the value, or {@code def} if absent.</li>
 * </ul>
 *
 * <p>Custom types are resolved through a {@link SerializerRegistry}.
 * Subclasses (e.g. {@link BukkitConfig}) override {@link #wrap} to ensure sub-sections
 * are also returned as the right wrapper type, and may register platform-specific serializers.
 */
public class Config {

    protected final ConfigSource source;
    protected final SerializerRegistry registry;

    public Config(ConfigSource source, SerializerRegistry registry) {
        this.source = source;
        this.registry = registry;
    }

    // ── String ───────────────────────────────────────────────────────────────

    public String findStr(String path) {
        final String value = this.source.str(path);
        if (value == null) throw new ConfigMissingPathException(path);
        return value;
    }

    public String strOr(String path, String def) {
        final String value = this.source.str(path);
        return value != null ? value : def;
    }

    // ── Boolean ──────────────────────────────────────────────────────────────

    public boolean findBool(String path) {
        final Boolean value = this.source.bool(path);
        if (value == null) throw new ConfigMissingPathException(path);
        return value;
    }

    public boolean boolOr(String path, boolean def) {
        final Boolean value = this.source.bool(path);
        return value != null ? value : def;
    }

    // ── Integer ──────────────────────────────────────────────────────────────

    public int findInt(String path) {
        final Integer value = this.source.integer(path);
        if (value == null) throw new ConfigMissingPathException(path);
        return value;
    }

    public int intOr(String path, int def) {
        final Integer value = this.source.integer(path);
        return value != null ? value : def;
    }

    // ── Long ─────────────────────────────────────────────────────────────────

    public long findLong(String path) {
        final Long value = this.source.lng(path);
        if (value == null) throw new ConfigMissingPathException(path);
        return value;
    }

    public long longOr(String path, long def) {
        final Long value = this.source.lng(path);
        return value != null ? value : def;
    }

    // ── Double ───────────────────────────────────────────────────────────────

    public double findDouble(String path) {
        final Double value = this.source.decimal(path);
        if (value == null) throw new ConfigMissingPathException(path);
        return value;
    }

    public double doubleOr(String path, double def) {
        final Double value = this.source.decimal(path);
        return value != null ? value : def;
    }

    // ── List ─────────────────────────────────────────────────────────────────

    public List<?> findList(String path) {
        final List<?> value = this.source.list(path);
        if (value == null) throw new ConfigMissingPathException(path);
        return value;
    }

    public List<?> listOr(String path, List<?> def) {
        final List<?> value = this.source.list(path);
        return value != null ? value : def;
    }

    // ── String list ───────────────────────────────────────────────────────────

    public List<String> findStrList(String path) {
        return toStrList(this.findList(path));
    }

    public List<String> strListOr(String path, List<String> def) {
        final List<?> raw = this.source.list(path);
        return raw != null ? toStrList(raw) : def;
    }

    private static List<String> toStrList(List<?> raw) {
        final List<String> result = new ArrayList<>(raw.size());
        for (final Object e : raw) {
            if (e != null) result.add(e.toString());
        }
        return result;
    }

    // ── Int list (fastutil) ───────────────────────────────────────────────────

    public IntList findIntList(String path) {
        return toIntList(this.findList(path));
    }

    public IntList intListOr(String path, IntList def) {
        final List<?> raw = this.source.list(path);
        return raw != null ? toIntList(raw) : def;
    }

    private static IntList toIntList(List<?> raw) {
        final IntList result = new IntArrayList(raw.size());
        for (final Object e : raw) {
            if (e instanceof Number n) result.add(n.intValue());
        }
        return result;
    }

    // ── Long list (fastutil) ──────────────────────────────────────────────────

    public LongList findLongList(String path) {
        return toLongList(this.findList(path));
    }

    public LongList longListOr(String path, LongList def) {
        final List<?> raw = this.source.list(path);
        return raw != null ? toLongList(raw) : def;
    }

    private static LongList toLongList(List<?> raw) {
        final LongList result = new LongArrayList(raw.size());
        for (final Object e : raw) {
            if (e instanceof Number n) result.add(n.longValue());
        }
        return result;
    }

    // ── Double list (fastutil) ────────────────────────────────────────────────

    public DoubleList findDoubleList(String path) {
        return toDoubleList(this.findList(path));
    }

    public DoubleList doubleListOr(String path, DoubleList def) {
        final List<?> raw = this.source.list(path);
        return raw != null ? toDoubleList(raw) : def;
    }

    private static DoubleList toDoubleList(List<?> raw) {
        final DoubleList result = new DoubleArrayList(raw.size());
        for (final Object e : raw) {
            if (e instanceof Number n) result.add(n.doubleValue());
        }
        return result;
    }

    // ── Map ──────────────────────────────────────────────────────────────────

    public Map<?, ?> findMap(String path) {
        final Map<?, ?> value = this.source.map(path);
        if (value == null) throw new ConfigMissingPathException(path);
        return value;
    }

    public Map<?, ?> mapOr(String path, Map<String, ?> def) {
        final Map<?, ?> value = this.source.map(path);
        return value != null ? value : def;
    }

    // ── Section ──────────────────────────────────────────────────────────────

    public Config findSection(String path) {
        final ConfigSource section = this.source.section(path);
        if (section == null) throw new ConfigMissingPathException(path);
        return this.wrap(section);
    }

    public Config sectionOr(String path, Config def) {
        final ConfigSource section = this.source.section(path);
        return section != null ? this.wrap(section) : def;
    }

    // ── Section list ──────────────────────────────────────────────────────────

    public List<Config> findSectionList(String path) {
        return toSectionList(this.findList(path));
    }

    public List<Config> sectionListOr(String path, List<Config> def) {
        final List<?> raw = this.source.list(path);
        return raw != null ? toSectionList(raw) : def;
    }

    private List<Config> toSectionList(List<?> raw) {
        final List<Config> result = new ArrayList<>(raw.size());
        for (final Object e : raw) {
            if (e instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked") final Config section = this.wrap(new MapSource((Map<String, ?>) map));
                result.add(section);
            }
        }
        return result;
    }

    /**
     * Creates a child wrapper of the same type over the given source.
     * Override in subclasses to preserve the subtype across section traversal.
     */
    protected Config wrap(ConfigSource source) {
        return new Config(source, this.registry);
    }

    // ── Keys ─────────────────────────────────────────────────────────────────

    public Set<String> keys(boolean deep) {
        return this.source.keys(deep);
    }

    // ── Custom typed (via SerializerRegistry) ─────────────────────────────────

    /**
     * Reads the raw value at {@code path} from the source and passes it to the registered
     * serializer. Returns {@code null} if the path is absent or the serializer returns {@code null}.
     *
     * @throws SerializerNotFoundException                                                        if no serializer is registered for {@code type}
     * @throws com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException if the raw value's type is incompatible
     */
    @Nullable
    public <T> T get(Class<T> type, String path) {
        final ConfigSerializer<T> serializer = this.registry.get(type);
        if (serializer == null) throw new SerializerNotFoundException(path, type);
        final Object raw = this.source.raw(path);
        if (raw == null) return null;
        return serializer.deserialize(raw, path);
    }

    /**
     * Like {@link #get} but throws {@link ConfigMissingPathException} if the result is {@code null}.
     */
    public <T> T find(Class<T> type, String path) {
        final T value = this.get(type, path);
        if (value == null) throw new ConfigMissingPathException(path);
        return value;
    }

    /**
     * Like {@link #get} but returns {@code def} instead of {@code null}.
     */
    public <T> T getOr(Class<T> type, String path, T def) {
        final T value = this.get(type, path);
        return value != null ? value : def;
    }

    // ── List of custom typed ─────────────────────────────────────────────────

    /**
     * Deserializes each element of the list at {@code path} via the registered serializer,
     * skipping elements that serialize to {@code null}.
     *
     * @throws ConfigMissingPathException  if the path is missing
     * @throws SerializerNotFoundException if no serializer is registered for {@code type}
     */
    public <T> List<T> findListOf(Class<T> type, String path) {
        return this.deserializeList(type, path, this.findList(path));
    }

    /**
     * Like {@link #findListOf} but returns {@code def} if the path is missing.
     */
    public <T> List<T> listOfOr(Class<T> type, String path, List<T> def) {
        final List<?> raw = this.source.list(path);
        return raw != null ? this.deserializeList(type, path, raw) : def;
    }

    private <T> List<T> deserializeList(Class<T> type, String path, List<?> raw) {
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
