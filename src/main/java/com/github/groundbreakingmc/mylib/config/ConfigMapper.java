package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.config.annotation.ConfigField;
import com.github.groundbreakingmc.mylib.config.annotation.ConfigSection;
import com.github.groundbreakingmc.mylib.config.exception.ConfigMissingPathException;
import com.github.groundbreakingmc.mylib.config.exception.SerializerNotFoundException;
import com.github.groundbreakingmc.mylib.config.source.ConfigSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Internal reflection-based mapper that instantiates a record from a {@link Config}.
 *
 * <p>Not part of the public API — use {@link Config#as(Class)} as the entry point.
 *
 * <p><strong>Resolution order for {@link ConfigField}:</strong>
 * <ol>
 *   <li>If the path is absent: honour {@code required}, then {@code def}, then zero-value.</li>
 *   <li>If the type is a primitive, boxed primitive, or {@link String}: coerce directly.</li>
 *   <li>If the field's declared type is {@link List}/{@link Set}/{@link Map} <em>and a
 *       {@link ConfigSerializer} is registered for that exact declared type</em> — e.g. fastutil's
 *       {@code IntList} — the raw list/map is handed to that serializer whole, with no per-element
 *       decomposition. Use this for any specialized collection type that needs custom,
 *       allocation-conscious construction rather than generic boxing.</li>
 *   <li>Otherwise, if the type is {@link List} or {@link Set}: the element type is read from the
 *       component's generic signature, e.g. {@code List<String>} → {@code String.class}. Each
 *       element is then coerced (if primitive-like) or run through the registered
 *       {@link ConfigSerializer} for that element type. A raw/wildcard collection (no reified
 *       element type, and no serializer registered for the declared type per the point above) is
 *       rejected.</li>
 *   <li>Otherwise, if the type is {@link Map}: same as above but for the value type; the key type
 *       must be {@link String} (config map keys always are).</li>
 *   <li>Otherwise: look up a {@link ConfigSerializer} in the registry and delegate.</li>
 * </ol>
 *
 * <p><strong>Resolution for {@link ConfigSection}:</strong>
 * always recurses — the registry is never consulted, regardless of whether
 * a serializer exists for the component type.
 */
final class ConfigMapper {

    private ConfigMapper() {
    }

    // ── Entry point ────────────────────────────────────────────────────────────

    static <T> T map(Config config, Class<T> type) {
        if (!type.isRecord()) {
            throw new IllegalArgumentException(
                    "ConfigMapper only supports record types, got: " + type.getName()
            );
        }

        final RecordComponent[] components = type.getRecordComponents();
        final Object[] args = new Object[components.length];
        final Class<?>[] paramTypes = new Class<?>[components.length];

        for (int i = 0; i < components.length; i++) {
            final RecordComponent component = components[i];
            paramTypes[i] = component.getType();

            final ConfigField fieldAnn = component.getAnnotation(ConfigField.class);
            final ConfigSection sectionAnn = component.getAnnotation(ConfigSection.class);

            if (fieldAnn != null && sectionAnn != null) {
                throw new IllegalArgumentException(
                        "Component \"" + component.getName() + "\" in " + type.getName()
                                + " cannot have both @ConfigField and @ConfigSection"
                );
            }

            if (fieldAnn != null) {
                args[i] = resolveField(config, component, fieldAnn);
            } else if (sectionAnn != null) {
                args[i] = resolveSection(config, component, sectionAnn);
            } else {
                throw new IllegalArgumentException(
                        "Component \"" + component.getName() + "\" in " + type.getName()
                                + " is missing @ConfigField or @ConfigSection"
                );
            }
        }

        return construct(type, paramTypes, args);
    }

    // ── @ConfigField ──────────────────────────────────────────────────────────

    private static Object resolveField(Config config, RecordComponent component, ConfigField ann) {
        final String path = ann.value();
        final Class<?> type = component.getType();

        if (List.class.isAssignableFrom(type)) {
            return resolveListLike(config, component, ann, path, false);
        }
        if (Set.class.isAssignableFrom(type)) {
            return resolveListLike(config, component, ann, path, true);
        }
        if (Map.class.isAssignableFrom(type)) {
            return resolveMap(config, component, ann, path);
        }

        final Object raw = config.source.raw(path);

        if (raw == null) {
            return absent(type, ann, path);
        }

        final Object coerced = coerce(type, raw, path);
        if (coerced != null) {
            return coerced;
        }

        // Custom type — must have a registered serializer
        @SuppressWarnings("unchecked") final ConfigSerializer<Object> serializer = (ConfigSerializer<Object>) config.registry.get(type);
        if (serializer == null) {
            throw new SerializerNotFoundException(path, type);
        }

        final Object value = serializer.deserialize(raw, path);
        return value != null ? value : absent(type, ann, path);
    }

    /**
     * Called when the raw value is absent or a serializer returned {@code null}.
     * Applies the required/def/zero-value fallback chain.
     */
    private static Object absent(Class<?> type, ConfigField ann, String path) {
        if (ann.required()) throw new ConfigMissingPathException(path);
        if (!ann.def().isEmpty()) return parseDefault(type, ann.def(), path);
        return zeroValue(type);
    }

    // ── List<X> / Set<X> ──────────────────────────────────────────────────────

    private static Object resolveListLike(Config config, RecordComponent component, ConfigField ann,
                                           String path, boolean asSet) {
        final Class<?> declaredType = component.getType();

        // A serializer registered for the exact declared type (e.g. fastutil's IntList,
        // LongList, ...) always wins — these are built whole, not decomposed element-by-element.
        final ConfigSerializer<?> wholeContainer = config.registry.get(declaredType);
        if (wholeContainer != null) {
            return resolveViaWholeContainerSerializer(wholeContainer, config.source.list(path), ann, path);
        }

        final List<?> raw = config.source.list(path);
        if (raw == null) {
            if (ann.required()) throw new ConfigMissingPathException(path);
            return null;
        }

        final Class<?> elementType = resolveElementType(component, declaredType, path);

        if (asSet) {
            final Set<Object> result = new LinkedHashSet<>(raw.size());
            for (final Object element : raw) {
                final Object value = deserializeElement(config, elementType, element, path);
                if (value != null) result.add(value);
            }
            return result;
        }

        final List<Object> result = new ArrayList<>(raw.size());
        for (final Object element : raw) {
            final Object value = deserializeElement(config, elementType, element, path);
            if (value != null) result.add(value);
        }
        return result;
    }

    private static Object resolveViaWholeContainerSerializer(ConfigSerializer<?> serializer, Object raw,
                                                               ConfigField ann, String path) {
        if (raw == null) {
            if (ann.required()) throw new ConfigMissingPathException(path);
            return null;
        }
        @SuppressWarnings("unchecked")
        final ConfigSerializer<Object> objSerializer = (ConfigSerializer<Object>) serializer;
        return objSerializer.deserialize(raw, path);
    }

    // ── Map<String, X> ────────────────────────────────────────────────────────

    private static Object resolveMap(Config config, RecordComponent component, ConfigField ann, String path) {
        final Class<?> declaredType = component.getType();

        // Same whole-container precedence as List/Set — see resolveListLike.
        final ConfigSerializer<?> wholeContainer = config.registry.get(declaredType);
        if (wholeContainer != null) {
            return resolveViaWholeContainerSerializer(wholeContainer, config.source.map(path), ann, path);
        }

        final Map<?, ?> raw = config.source.map(path);
        if (raw == null) {
            if (ann.required()) throw new ConfigMissingPathException(path);
            return null;
        }

        final Type generic = component.getGenericType();
        if (!(generic instanceof ParameterizedType parameterized) || parameterized.getActualTypeArguments().length != 2) {
            throw new IllegalArgumentException(
                    "Cannot resolve key/value types for Map field \"" + component.getName()
                            + "\" at path: \"" + path + "\". Declare a concrete Map<String, X>."
            );
        }
        final Type keyArg = parameterized.getActualTypeArguments()[0];
        if (keyArg != String.class) {
            throw new IllegalArgumentException(
                    "Map field \"" + component.getName() + "\" at path: \"" + path
                            + "\" must have a String key — config map keys are always strings."
            );
        }
        final Type valueArg = parameterized.getActualTypeArguments()[1];
        if (!(valueArg instanceof Class<?> valueType)) {
            throw new IllegalArgumentException(
                    "Cannot resolve value type for Map field \"" + component.getName()
                            + "\" at path: \"" + path + "\". Nested generics (e.g. Map<String, List<X>>) are not supported."
            );
        }

        final Map<String, Object> result = new LinkedHashMap<>(raw.size());
        for (final Map.Entry<?, ?> entry : raw.entrySet()) {
            final Object value = deserializeElement(config, valueType, entry.getValue(), path);
            if (value != null) result.put(String.valueOf(entry.getKey()), value);
        }
        return result;
    }

    // ── Element type resolution (shared by List/Set) ─────────────────────────

    /**
     * Recovers the element type of a {@code List<X>}/{@code Set<X>} component from its generic
     * signature. Only applies to plain, decomposable collection fields — a specialized type like
     * fastutil's {@code IntList} should instead have a {@link ConfigSerializer} registered for
     * itself (see the whole-container path in {@link #resolveListLike}), which is handed the raw
     * list directly and never reaches this method.
     */
    private static Class<?> resolveElementType(RecordComponent component, Class<?> declaredType, String path) {
        final Type generic = component.getGenericType();
        if (generic instanceof ParameterizedType parameterized) {
            final Type arg = parameterized.getActualTypeArguments()[0];
            if (arg instanceof Class<?> elementType) {
                return elementType;
            }
        }

        throw new IllegalArgumentException(
                "Cannot resolve element type for " + declaredType.getSimpleName() + " field \""
                        + component.getName() + "\" at path: \"" + path + "\". Declare a concrete "
                        + "element type (e.g. List<String>), or, if \"" + declaredType.getName()
                        + "\" is a specialized collection type (e.g. fastutil's IntList) that should "
                        + "be built as a whole rather than decomposed element-by-element, register a "
                        + "ConfigSerializer for it instead."
        );
    }

    private static Object deserializeElement(Config config, Class<?> elementType, Object raw, String path) {
        if (raw == null) return null;
        final Object coerced = coerce(elementType, raw, path);
        if (coerced != null) return coerced;
        @SuppressWarnings("unchecked")
        final ConfigSerializer<Object> serializer = (ConfigSerializer<Object>) config.registry.get(elementType);
        if (serializer == null) throw new SerializerNotFoundException(path, elementType);
        return serializer.deserialize(raw, path);
    }

    // ── @ConfigSection ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private static Object resolveSection(Config config, RecordComponent component, ConfigSection ann) {
        final String path = ann.value();
        final Class<?> type = component.getType();

        final ConfigSource sectionSource = config.source.section(path);

        if (sectionSource == null) {
            if (ann.required()) throw new ConfigMissingPathException(path);
            return null;
        }

        // config.wrap() preserves the concrete subtype (e.g. BukkitConfig → BukkitConfig)
        return map(config.wrap(sectionSource), (Class<Object>) type);
    }

    // ── Constructor ───────────────────────────────────────────────────────────

    private static <T> T construct(Class<T> type, Class<?>[] paramTypes, Object[] args) {
        try {
            final Constructor<T> ctor = type.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate record " + type.getName(), e);
        }
    }

    // ── Coercion ──────────────────────────────────────────────────────────────

    /**
     * Coerces {@code raw} to {@code type} if {@code type} is a primitive, boxed primitive, or
     * {@link String}. Returns {@code null} if {@code type} isn't one of those — this doubles as
     * the "is this primitive-like" check, since a successful coercion never itself produces
     * {@code null}. Throws on a genuine mismatch (e.g. {@code type == int.class} but {@code raw}
     * is neither a {@link Number} nor a parseable {@link String}).
     */
    private static Object coerce(Class<?> type, Object raw, String path) {
        if (type == String.class) return raw.toString();
        if (type == boolean.class || type == Boolean.class) return toBoolean(raw, path);
        if (type == int.class || type == Integer.class) return toInt(raw, path);
        if (type == long.class || type == Long.class) return toLong(raw, path);
        if (type == double.class || type == Double.class) return toDouble(raw, path);
        if (type == float.class || type == Float.class) return toFloat(raw, path);
        return null;
    }

    private static Object parseDefault(Class<?> type, String def, String path) {
        try {
            if (type == String.class) return def;
            if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(def);
            if (type == int.class || type == Integer.class) return Integer.parseInt(def);
            if (type == long.class || type == Long.class) return Long.parseLong(def);
            if (type == double.class || type == Double.class) return Double.parseDouble(def);
            if (type == float.class || type == Float.class) return Float.parseFloat(def);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid default value \"" + def + "\" for type " + type.getSimpleName()
                            + " at path: " + path, e
            );
        }
        // Complex type — string default is not meaningful, treat as no-default
        return null;
    }

    private static Object zeroValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0d;
        if (type == float.class) return 0.0f;
        return null;
    }

    // ── Coercion helpers ──────────────────────────────────────────────────────

    private static boolean toBoolean(Object raw, String path) {
        if (raw instanceof Boolean b) return b;
        if (raw instanceof String s) return Boolean.parseBoolean(s);
        throw typeMismatch("boolean", raw, path);
    }

    private static int toInt(Object raw, String path) {
        if (raw instanceof Number n) return n.intValue();
        if (raw instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                // fall through to the single failure point below
            }
        }
        throw typeMismatch("int", raw, path);
    }

    private static long toLong(Object raw, String path) {
        if (raw instanceof Number n) return n.longValue();
        if (raw instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                // fall through to the single failure point below
            }
        }
        throw typeMismatch("long", raw, path);
    }

    private static double toDouble(Object raw, String path) {
        if (raw instanceof Number n) return n.doubleValue();
        if (raw instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
                // fall through to the single failure point below
            }
        }
        throw typeMismatch("double", raw, path);
    }

    private static float toFloat(Object raw, String path) {
        if (raw instanceof Number n) return n.floatValue();
        if (raw instanceof String s) {
            try {
                return Float.parseFloat(s);
            } catch (NumberFormatException ignored) {
                // fall through to the single failure point below
            }
        }
        throw typeMismatch("float", raw, path);
    }

    private static IllegalArgumentException typeMismatch(String expected, Object raw, String path) {
        return new IllegalArgumentException(
                "Cannot coerce " + raw.getClass().getSimpleName()
                        + " to " + expected + " at config path: \"" + path + "\""
        );
    }
}
