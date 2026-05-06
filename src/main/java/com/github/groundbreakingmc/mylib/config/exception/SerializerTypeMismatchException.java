package com.github.groundbreakingmc.mylib.config.exception;

/**
 * Thrown when a {@link com.github.groundbreakingmc.mylib.config.ConfigSerializer}
 * receives a value whose runtime type does not match the expected type.
 *
 * <p>Example: a {@code SoundSerializer} that expects {@link String}
 * but finds an {@link Integer} in the config at the given path.
 */
public final class SerializerTypeMismatchException extends RuntimeException {

    private final String path;
    private final Class<?> expectedType;
    private final Class<?> actualType;

    public SerializerTypeMismatchException(String path, Class<?> expectedType, Class<?> actualType) {
        super("Type mismatch at path '" + path + "': "
                + "expected " + expectedType.getSimpleName()
                + ", got " + actualType.getSimpleName());
        this.path = path;
        this.expectedType = expectedType;
        this.actualType = actualType;
    }

    public String getPath() {
        return path;
    }

    public Class<?> getExpectedType() {
        return expectedType;
    }

    public Class<?> getActualType() {
        return actualType;
    }
}
