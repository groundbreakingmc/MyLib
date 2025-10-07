package com.github.groundbreakingmc.mylib.utils.fakeplugin;

import com.destroystokyo.paper.utils.PaperPluginLogger;
import com.github.groundbreakingmc.mylib.utils.server.version.ServerVersion;
import com.github.groundbreakingmc.mylib.utils.server.version.ServerVersionUtils;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.logging.Logger;

/**
 * Utility class for creating and configuring fake plugin instances using reflection and Unsafe.
 */
@UtilityClass
public class FakePluginUtils {

    /**
     * Creates a fake plugin instance without calling its constructor.
     *
     * @param description the plugin description file
     * @param logger      the logger to use, or null to create a default logger
     * @return the configured fake plugin instance
     * @throws RuntimeException if plugin creation or setup fails
     */
    public static Plugin create(@NotNull PluginDescriptionFile description, @Nullable Logger logger) {
        try {
            final Unsafe unsafe = getUnsafe();
            final FakePlugin plugin = (FakePlugin) unsafe.allocateInstance(FakePlugin.class);
            setupJavaPlugin(plugin, description, logger);
            return plugin;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    /**
     * Configures a JavaPlugin instance by reflectively setting its internal fields.
     *
     * @param plugin      the plugin instance to configure
     * @param description the plugin description file
     * @param logger      the logger to use, or null to create a default logger
     * @throws RuntimeException if field access or modification fails
     */
    public static void setupJavaPlugin(@NotNull JavaPlugin plugin, @NotNull PluginDescriptionFile description, @Nullable Logger logger) {
        try {
            final Field isEnabledField = JavaPlugin.class.getDeclaredField("isEnabled");
            isEnabledField.setAccessible(true);
            isEnabledField.set(plugin, true);

            final Field loaderField = JavaPlugin.class.getDeclaredField("loader");
            loaderField.setAccessible(true);
            loaderField.set(plugin, new JavaPluginLoader(Bukkit.getServer()));

            final Field serverField = JavaPlugin.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(plugin, Bukkit.getServer());

            final Field descriptionField = JavaPlugin.class.getDeclaredField("description");
            descriptionField.setAccessible(true);
            descriptionField.set(plugin, description);

            final Field classLoaderField = JavaPlugin.class.getDeclaredField("classLoader");
            classLoaderField.setAccessible(true);
            classLoaderField.set(plugin, new ClassLoader() {
            });

            final Field loggerField = JavaPlugin.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
            loggerField.set(plugin, logger != null ? logger : getLogger(plugin, description));
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    /**
     * Retrieves the Unsafe instance via reflection.
     *
     * @return the Unsafe instance
     * @throws ReflectiveOperationException if Unsafe field access fails
     */
    private static Unsafe getUnsafe() throws ReflectiveOperationException {
        final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        return (Unsafe) theUnsafe.get(null);
    }

    /**
     * Creates an appropriate logger based on server version.
     *
     * @param plugin      the plugin instance
     * @param description the plugin description file
     * @return a PaperPluginLogger for 1.12+ or PluginLogger for older versions
     */
    private static Logger getLogger(Plugin plugin, PluginDescriptionFile description) {
        return ServerVersionUtils.isHigherOrEqual(ServerVersion.V1_12_R1)
                ? PaperPluginLogger.getLogger(description)
                : new PluginLogger(plugin);
    }
}
