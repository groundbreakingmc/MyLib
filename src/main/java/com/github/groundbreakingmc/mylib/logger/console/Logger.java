package com.github.groundbreakingmc.mylib.logger.console;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import com.github.groundbreakingmc.mylib.logger.console.adapter.LogLevel;
import com.github.groundbreakingmc.mylib.logger.console.adapter.LoggerAdapter;
import com.github.groundbreakingmc.mylib.logger.console.adapter.impl.JulAdapter;
import com.github.groundbreakingmc.mylib.logger.console.adapter.impl.Slf4JAdapter;
import com.github.groundbreakingmc.mylib.logger.console.colorizer.AnsiColorTranslator;

import java.util.function.Supplier;

public class Logger extends java.util.logging.Logger implements org.slf4j.Logger {

    private final LoggerAdapter adapter;

    private Logger(String name, LoggerAdapter adapter) {
        super(name, null);
        this.adapter = adapter;
    }

    // ============================================
    // PUBLIC API (SIMPLIFIED)
    // ============================================

    public void trace(String msg) {
        this.adapter.trace(AnsiColorTranslator.translate(msg));
    }

    public void trace(String format, Object... args) {
        this.adapter.trace(AnsiColorTranslator.translate(
                String.format(format, args)
        ));
    }

    @Override
    public void trace(String msg, Throwable t) {
        this.adapter.trace(AnsiColorTranslator.translate(msg), t);
    }

    @Override
    public void finest(String msg) {
        this.trace(AnsiColorTranslator.translate(msg));
    }

    @Override
    public void finest(Supplier<String> msg) {
        if (this.adapter.isEnabled(LogLevel.TRACE)) {
            this.trace(AnsiColorTranslator.translate(
                    msg.get()
            ));
        }
    }

    @Override
    public void debug(String msg) {
        this.adapter.debug(AnsiColorTranslator.translate(msg));
    }

    @Override
    public void debug(String format, Object... args) {
        this.adapter.debug(AnsiColorTranslator.translate(
                String.format(format, args)
        ));
    }

    @Override
    public void debug(String msg, Throwable t) {
        this.adapter.debug(AnsiColorTranslator.translate(msg), t);
    }

    @Override
    public void fine(String msg) {
        this.debug(AnsiColorTranslator.translate(msg));
    }

    @Override
    public void fine(Supplier<String> msg) {
        if (this.adapter.isEnabled(LogLevel.DEBUG)) {
            this.debug(AnsiColorTranslator.translate(
                    msg.get()
            ));
        }
    }

    @Override
    public void info(String msg) {
        this.adapter.info(AnsiColorTranslator.translate(msg));
    }

    @Override
    public void info(String format, Object... args) {
        this.adapter.info(AnsiColorTranslator.translate(
                String.format(format, args)
        ));
    }

    @Override
    public void info(String msg, Throwable t) {
        this.adapter.info(AnsiColorTranslator.translate(msg), t);
    }

    @Override
    public void info(Supplier<String> msg) {
        if (this.adapter.isEnabled(LogLevel.INFO)) {
            this.info(AnsiColorTranslator.translate(
                    msg.get()
            ));
        }
    }

    @Override
    public void warn(String msg) {
        this.adapter.warn(AnsiColorTranslator.translate(msg));
    }

    @Override
    public void warn(String format, Object... args) {
        this.adapter.warn(AnsiColorTranslator.translate(
                String.format(format, args)
        ));
    }

    @Override
    public void warn(String msg, Throwable t) {
        this.adapter.warn(AnsiColorTranslator.translate(msg), t);
    }

    @Override
    public void warning(String msg) {
        this.warn(AnsiColorTranslator.translate(msg));
    }

    @Override
    public void warning(Supplier<String> msg) {
        if (this.adapter.isEnabled(LogLevel.WARN)) {
            this.warn(AnsiColorTranslator.translate(
                    msg.get()
            ));
        }
    }

    @Override
    public void error(String msg) {
        this.adapter.error(AnsiColorTranslator.translate(msg));
    }

    @Override
    public void error(String format, Object... args) {
        this.adapter.error(AnsiColorTranslator.translate(
                String.format(format, args)
        ));
    }

    @Override
    public void error(String msg, Throwable t) {
        this.adapter.error(AnsiColorTranslator.translate(msg), t);
    }

    @Override
    public void severe(String msg) {
        this.error(AnsiColorTranslator.translate(msg));
    }

    @Override
    public void severe(Supplier<String> msg) {
        if (this.adapter.isEnabled(LogLevel.ERROR)) {
            this.error(AnsiColorTranslator.translate(
                    msg.get()
            ));
        }
    }

    // ============================================
    // FACTORY METHODS
    // ============================================

    public static Logger create(java.util.logging.Logger julLogger) {
        return new Logger(
                julLogger.getName(),
                new JulAdapter(julLogger)
        );
    }

    public static Logger create(org.slf4j.Logger slf4jLogger) {
        return new Logger(
                slf4jLogger.getName(),
                new Slf4JAdapter(slf4jLogger)
        );
    }

    public static Logger fromBukkit(org.bukkit.plugin.Plugin plugin) {
        final java.util.logging.Logger logger = PaperPluginLogger.getLogger(plugin.getDescription());
        return new Logger(logger.getName(), new JulAdapter(logger));
    }

    public static Logger fromBungee(net.md_5.bungee.api.plugin.Plugin plugin) {
        return create(plugin.getLogger());
    }

    public static Logger fromVelocity(org.slf4j.Logger logger) {
        return create(logger);
    }

    // ============================================
    // SLF4J COMPATIBILITY (MARKERS)
    // ============================================

    @Override
    public void trace(org.slf4j.Marker marker, String msg) {
        this.trace(msg);
    }

    @Override
    public void trace(org.slf4j.Marker marker, String format, Object arg) {
        this.trace(format, arg);
    }

    @Override
    public void trace(org.slf4j.Marker marker, String format, Object arg1, Object arg2) {
        this.trace(format, arg1, arg2);
    }

    @Override
    public void trace(org.slf4j.Marker marker, String format, Object... args) {
        this.trace(format, args);
    }

    @Override
    public void trace(org.slf4j.Marker marker, String msg, Throwable t) {
        this.trace(msg, t);
    }

    @Override
    public void debug(org.slf4j.Marker marker, String msg) {
        this.debug(msg);
    }

    @Override
    public void debug(org.slf4j.Marker marker, String format, Object arg) {
        this.debug(format, arg);
    }

    @Override
    public void debug(org.slf4j.Marker marker, String format, Object arg1, Object arg2) {
        this.debug(format, arg1, arg2);
    }

    @Override
    public void debug(org.slf4j.Marker marker, String format, Object... args) {
        this.debug(format, args);
    }

    @Override
    public void debug(org.slf4j.Marker marker, String msg, Throwable t) {
        this.debug(msg, t);
    }

    @Override
    public void info(org.slf4j.Marker marker, String msg) {
        this.info(msg);
    }

    @Override
    public void info(org.slf4j.Marker marker, String format, Object arg) {
        this.info(format, arg);
    }

    @Override
    public void info(org.slf4j.Marker marker, String format, Object arg1, Object arg2) {
        this.info(format, arg1, arg2);
    }

    @Override
    public void info(org.slf4j.Marker marker, String format, Object... args) {
        this.info(format, args);
    }

    @Override
    public void info(org.slf4j.Marker marker, String msg, Throwable t) {
        this.info(msg, t);
    }

    @Override
    public void warn(org.slf4j.Marker marker, String msg) {
        this.warn(msg);
    }

    @Override
    public void warn(org.slf4j.Marker marker, String format, Object arg) {
        this.warn(format, arg);
    }

    @Override
    public void warn(org.slf4j.Marker marker, String format, Object arg1, Object arg2) {
        this.warn(format, arg1, arg2);
    }

    @Override
    public void warn(org.slf4j.Marker marker, String format, Object... args) {
        this.warn(format, args);
    }

    @Override
    public void warn(org.slf4j.Marker marker, String msg, Throwable t) {
        this.warn(msg, t);
    }

    @Override
    public void error(org.slf4j.Marker marker, String msg) {
        this.error(msg);
    }

    @Override
    public void error(org.slf4j.Marker marker, String format, Object arg) {
        this.error(format, arg);
    }

    @Override
    public void error(org.slf4j.Marker marker, String format, Object arg1, Object arg2) {
        this.error(format, arg1, arg2);
    }

    @Override
    public void error(org.slf4j.Marker marker, String format, Object... args) {
        this.error(format, args);
    }

    @Override
    public void error(org.slf4j.Marker marker, String msg, Throwable t) {
        this.error(msg, t);
    }

    // ============================================
    // SLF4J COMPATIBILITY (ENABLED CHECKS)
    // ============================================

    @Override
    public boolean isTraceEnabled() {
        return this.adapter.isEnabled(LogLevel.TRACE);
    }

    @Override
    public boolean isTraceEnabled(org.slf4j.Marker marker) {
        return this.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return this.adapter.isEnabled(LogLevel.DEBUG);
    }

    @Override
    public boolean isDebugEnabled(org.slf4j.Marker marker) {
        return this.isDebugEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return this.adapter.isEnabled(LogLevel.INFO);
    }

    @Override
    public boolean isInfoEnabled(org.slf4j.Marker marker) {
        return this.isInfoEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return this.adapter.isEnabled(LogLevel.WARN);
    }

    @Override
    public boolean isWarnEnabled(org.slf4j.Marker marker) {
        return this.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return this.adapter.isEnabled(LogLevel.ERROR);
    }

    @Override
    public boolean isErrorEnabled(org.slf4j.Marker marker) {
        return this.isErrorEnabled();
    }

    // ============================================
    // SLF4J COMPATIBILITY (VARARGS)
    // ============================================

    @Override
    public void trace(String format, Object arg) {
        this.trace(format, new Object[]{arg});
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        this.trace(format, new Object[]{arg1, arg2});
    }

    @Override
    public void debug(String format, Object arg) {
        this.debug(format, new Object[]{arg});
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        this.debug(format, new Object[]{arg1, arg2});
    }

    @Override
    public void info(String format, Object arg) {
        this.info(format, new Object[]{arg});
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        this.info(format, new Object[]{arg1, arg2});
    }

    @Override
    public void warn(String format, Object arg) {
        this.warn(format, new Object[]{arg});
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        this.warn(format, new Object[]{arg1, arg2});
    }

    @Override
    public void error(String format, Object arg) {
        this.error(format, new Object[]{arg});
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        this.error(format, new Object[]{arg1, arg2});
    }
}
