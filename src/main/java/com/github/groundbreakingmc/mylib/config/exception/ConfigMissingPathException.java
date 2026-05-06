package com.github.groundbreakingmc.mylib.config.exception;

/**
 * Thrown when a required config path is missing or maps to {@code null}.
 */
public final class ConfigMissingPathException extends RuntimeException {

    public ConfigMissingPathException(String path) {
        super("Missing required config path: \"" + path + "\"");
    }
}
