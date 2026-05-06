package com.github.groundbreakingmc.mylib.config.exception;

/**
 * Thrown when no serializer is found for a config value,
 * making it impossible to read the value by key.
 */
public final class SerializerNotFoundException extends RuntimeException {

    public SerializerNotFoundException(String path, Class<?> type) {
        super("No serializer found for path \"" + path + "\" and type \""
                + type.getName() + "\"");
    }

    public SerializerNotFoundException(String path) {
        super("No serializer found for path \"" + path + "\"");
    }
}
