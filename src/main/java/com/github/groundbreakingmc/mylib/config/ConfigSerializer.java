package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.config.exception.SerializerTypeMismatchException;
import org.jetbrains.annotations.Nullable;

/**
 * Deserializes a raw config value into type {@code T}.
 *
 * <p>The {@code value} parameter is the raw object returned by the config backend
 * (e.g. a {@link String}, {@link Integer}, {@link java.util.Map Map&lt;?,?&gt;}, etc.).
 * The {@code path} parameter is provided purely for readable error messages —
 * implementations should include it in any exception they throw.
 *
 * <p>Return {@code null} to signal that the value is absent or invalid in a
 * non-exceptional way; the caller ({@link Config#find} / {@link Config#getOr})
 * handles the throw/default.
 *
 * <p>Throw {@link SerializerTypeMismatchException} when the runtime type of
 * {@code value} is incompatible with what this serializer expects.
 *
 * @param <T> the type this serializer produces
 */
@FunctionalInterface
public interface ConfigSerializer<T> {

    @Nullable T deserialize(Object value, String path);
}
