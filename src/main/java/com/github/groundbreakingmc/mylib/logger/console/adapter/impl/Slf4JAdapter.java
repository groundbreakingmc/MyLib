package com.github.groundbreakingmc.mylib.logger.console.adapter.impl;

import com.github.groundbreakingmc.mylib.logger.console.adapter.LogLevel;
import com.github.groundbreakingmc.mylib.logger.console.adapter.LoggerAdapter;

public class Slf4JAdapter implements LoggerAdapter {

    private final org.slf4j.Logger logger;

    public Slf4JAdapter(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    @Override
    public void trace(String message) {
        this.logger.trace(message);
    }

    @Override
    public void trace(String message, Throwable throwable) {
        this.logger.trace(message, throwable);
    }

    @Override
    public void debug(String message) {
        this.logger.debug(message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        this.logger.debug(message, throwable);
    }

    @Override
    public void info(String message) {
        this.logger.info(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        this.logger.info(message, throwable);
    }

    @Override
    public void warn(String message) {
        this.logger.warn(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        this.logger.warn(message, throwable);
    }

    @Override
    public void error(String message) {
        this.logger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        this.logger.error(message, throwable);
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        return switch (level) {
            case TRACE -> this.logger.isTraceEnabled();
            case DEBUG -> this.logger.isDebugEnabled();
            case INFO -> this.logger.isInfoEnabled();
            case WARN -> this.logger.isWarnEnabled();
            case ERROR -> this.logger.isErrorEnabled();
        };
    }
}
