package com.github.groundbreakingmc.mylib;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public final class MyLib extends JavaPlugin {

//    @Getter
//    private static MyLib instance;
//    @Getter
//    private final Logger customLogger = new LoggerFactory().getLogger(this);
//
//    public void onEnable() {
//        instance = this;
//    }

    @Override
    public FileConfiguration getConfig() {
        throw new UnsupportedOperationException("JavaPlugin#getConfig is not supported! If you want to load configuration file use com.github.groundbreakingmc.mylib.config.Config#get instead!");
    }

    @Override
    public void reloadConfig() {
        throw new UnsupportedOperationException("JavaPlugin#reloadConfig is not supported! If you want to load configuration file use com.github.groundbreakingmc.mylib.config.Config#get instead!");
    }

    @Override
    public void saveConfig() {
        throw new UnsupportedOperationException("JavaPlugin#saveConfig is not supported! If you want to load configuration file use com.github.groundbreakingmc.mylib.config.Config#get instead!");
    }

    @Override
    public void saveDefaultConfig() {
        throw new UnsupportedOperationException("JavaPlugin#saveDefaultConfig is not supported! If you want to load configuration file use com.github.groundbreakingmc.mylib.config.Config#get instead!");
    }

    @Override
    public void saveResource(@NotNull String resourcePath, boolean replace) {
        throw new UnsupportedOperationException("JavaPlugin#saveResource is not supported! If you want to load configuration file use com.github.groundbreakingmc.mylib.config.Config#get instead!");
    }

    @Override
    public InputStream getResource(@NotNull String filename) {
        throw new UnsupportedOperationException("JavaPlugin#getResource is not supported! If you want to load configuration file use com.github.groundbreakingmc.mylib.config.Config#get instead!");
    }

    @Override
    public PluginCommand getCommand(@NotNull String name) {
        throw new UnsupportedOperationException("JavaPlugin#getCommand is not supported! If you want to get the command use com.github.groundbreakingmc.mylib.utils.command.CommandRuntimeUtils#register instead!");
    }
}
