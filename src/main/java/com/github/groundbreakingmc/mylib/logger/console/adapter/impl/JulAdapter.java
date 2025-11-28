package com.github.groundbreakingmc.mylib.logger.console.adapter.impl;

import com.github.groundbreakingmc.mylib.logger.console.adapter.LogLevel;
import com.github.groundbreakingmc.mylib.logger.console.adapter.LoggerAdapter;

import java.util.logging.Level;

public class JulAdapter implements LoggerAdapter {

    private final java.util.logging.Logger logger;

    public JulAdapter(java.util.logging.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void trace(String message) {
        this.logger.log(Level.FINEST, message);
    }

    @Override
    public void trace(String message, Throwable throwable) {
        this.logger.log(Level.FINEST, message, throwable);
    }

    @Override
    public void debug(String message) {
        this.logger.log(Level.FINE, message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        this.logger.log(Level.FINE, message, throwable);
    }

    @Override
    public void info(String message) {
        this.logger.log(Level.INFO, message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        this.logger.log(Level.INFO, message, throwable);
    }

    @Override
    public void warn(String message) {
        this.logger.log(Level.WARNING, message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        this.logger.log(Level.WARNING, message, throwable);
    }

    @Override
    public void error(String message) {
        this.logger.log(Level.SEVERE, message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        this.logger.log(Level.SEVERE, message, throwable);
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return this.logger.isLoggable(this.toJulLevel(level));
    }

    private Level toJulLevel(LogLevel level) {
        return switch (level) {
            case TRACE -> Level.FINEST;
            case DEBUG -> Level.FINE;
            case INFO -> Level.INFO;
            case WARN -> Level.WARNING;
            case ERROR -> Level.SEVERE;
        };
    }
}
