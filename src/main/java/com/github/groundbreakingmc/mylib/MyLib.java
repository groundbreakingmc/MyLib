package com.github.groundbreakingmc.mylib;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@SuppressWarnings("unused")
public final class MyLib extends JavaPlugin {

    @Override
    public void onEnable() {
        // WorldGuard loads region manager to late :(
        try {
            Class.forName("com.github.groundbreakingmc.mylib.utils.worldguard.WorldGuardUtils");
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        throw new UnsupportedOperationException("JavaPlugin#getConfig is not supported! If you want to load configuration file use com.github.groundbreakingmc.mylib.config.ConfigurateLoader#loader instead!");
    }

    @Override
    public void reloadConfig() {
        throw new UnsupportedOperationException("JavaPlugin#reloadConfig is not supported! If you want to load configuration file use com.github.groundbreakingmc.mylib.config.ConfigurateLoader#loader instead!");
    }

    @Override
    public void saveConfig() {
        throw new UnsupportedOperationException("JavaPlugin#saveConfig is not supported! If you want to load configuration file use com.github.groundbreakingmc.mylib.config.ConfigurateLoader#loader instead!");
    }

    @Override
    public void saveDefaultConfig() {
        throw new UnsupportedOperationException("JavaPlugin#saveDefaultConfig is not supported! If you want to load configuration file use com.github.groundbreakingmc.mylib.config.ConfigurateLoader#loader instead!");
    }

    @Override
    public void saveResource(@NotNull String resourcePath, boolean replace) {
        throw new UnsupportedOperationException("JavaPlugin#saveResource is not supported! If you want to load configuration file use com.github.groundbreakingmc.mylib.config.ConfigurateLoader#loader instead!");
    }

    @Override
    public InputStream getResource(@NotNull String filename) {
        throw new UnsupportedOperationException("JavaPlugin#getResource is not supported! If you want to load configuration file use com.github.groundbreakingmc.mylib.config.ConfigurateLoader#loader instead!");
    }

    @Override
    public PluginCommand getCommand(@NotNull String name) {
        throw new UnsupportedOperationException("JavaPlugin#getCommand is not supported! If you want to get the command use com.github.groundbreakingmc.mylib.utils.command.CommandRuntimeUtils#register instead!");
    }

    static {
        final String jarPath = MyLib.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try (final JarFile jarFile = new JarFile(jarPath)) {
            for (final Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                final JarEntry entry = entries.nextElement();
                final String entryName = entry.getName();

                if (entryName.endsWith(".class")
                        && entryName.startsWith("com/github/groundbreakingmc/mylib")
                        && !entryName.contains("worldguard/WorldGuardUtils.class")) {
                    final String className = entryName
                            .replace("/", ".")
                            .replace(".class", "");
                    MyLib.class.getClassLoader().loadClass(className);
                }
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }
}
