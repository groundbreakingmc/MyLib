package com.github.groundbreakingmc.mylib.utils.command;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for runtime command registration and management.
 * Provides methods to register, unregister, and modify commands without plugin.yml.
 *
 * <p>Uses reflection to access Bukkit's internal command map and supports
 * Paper/Spigot 1.20.6+ with async command synchronization.</p>
 */
@UtilityClass
@SuppressWarnings({"unchecked", "unused"})
public class CommandRuntimeUtils {

    private final Constructor<PluginCommand> PLUGIN_COMMAND_CONSTRUCTOR;
    private final Field COMMAND_MAP_FIELD;
    private final Field ALIASES_FIELD;
    private final Method SYNC_COMMANDS_METHOD;
    public final SimpleCommandMap COMMAND_MAP;
    private final Map<String, Command> KNOWN_COMMANDS;

    private static final Object SYNC_LOCK = new Object();
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> scheduledSync = null;

    /**
     * Registers a command with executor only.
     *
     * @param plugin          plugin instance
     * @param command         command name
     * @param commandExecutor command executor
     */
    public void register(@NotNull Plugin plugin, @NotNull String command, @NotNull CommandExecutor commandExecutor) {
        register(plugin, command, null, commandExecutor, null);
    }

    /**
     * Registers a command with tab executor.
     *
     * @param plugin      plugin instance
     * @param command     command name
     * @param tabExecutor tab executor (handles both execution and tab completion)
     */
    public void register(@NotNull Plugin plugin, @NotNull String command, @NotNull TabExecutor tabExecutor) {
        register(plugin, command, null, tabExecutor, tabExecutor);
    }

    /**
     * Registers a command with aliases and executor.
     *
     * @param plugin          plugin instance
     * @param command         command name
     * @param aliases         command aliases
     * @param commandExecutor command executor
     */
    public void register(@NotNull Plugin plugin, @NotNull String command, @NotNull List<String> aliases, @NotNull CommandExecutor commandExecutor) {
        register(plugin, command, aliases, commandExecutor, null);
    }

    /**
     * Registers a command with aliases and tab executor.
     *
     * @param plugin      plugin instance
     * @param command     command name
     * @param aliases     command aliases
     * @param tabExecutor tab executor
     */
    public void register(@NotNull Plugin plugin, @NotNull String command, @NotNull List<String> aliases, @NotNull TabExecutor tabExecutor) {
        register(plugin, command, aliases, tabExecutor, tabExecutor);
    }

    /**
     * Registers a command with separate executor and tab completer.
     *
     * @param plugin          plugin instance
     * @param command         command name
     * @param commandExecutor command executor
     * @param tabCompleter    tab completer
     */
    public void register(@NotNull Plugin plugin,
                         @NotNull String command,
                         @NotNull CommandExecutor commandExecutor,
                         @NotNull TabCompleter tabCompleter) {
        register(plugin, command, null, commandExecutor, tabCompleter);
    }

    /**
     * Registers a command with full configuration.
     *
     * @param plugin          plugin instance
     * @param command         command name
     * @param aliases         command aliases (nullable)
     * @param commandExecutor command executor
     * @param tabCompleter    tab completer (nullable)
     */
    public void register(@NotNull Plugin plugin,
                         @NotNull String command,
                         @Nullable List<String> aliases,
                         @NotNull CommandExecutor commandExecutor,
                         @Nullable TabCompleter tabCompleter) {
        final PluginCommand pluginCommand = createCommand(plugin, command);
        pluginCommand.setExecutor(commandExecutor);
        if (tabCompleter != null) {
            pluginCommand.setTabCompleter(tabCompleter);
        }
        if (aliases != null && !aliases.isEmpty()) {
            pluginCommand.setAliases(aliases);
        }

        COMMAND_MAP.register(plugin.getDescription().getName(), pluginCommand);

        syncCommands();
    }

    /**
     * Creates a PluginCommand instance using reflection.
     *
     * @param plugin  plugin instance
     * @param command command name
     * @return created PluginCommand
     */
    public PluginCommand createCommand(@NotNull Plugin plugin, @NotNull String command) {
        try {
            return PLUGIN_COMMAND_CONSTRUCTOR.newInstance(command, plugin);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Unregisters a command by name.
     *
     * @param command command name
     * @return true if command was found and unregistered
     */
    public boolean unregisterCommand(@NotNull String command) {
        final Command mapCommand = COMMAND_MAP.getCommand(command);
        if (mapCommand != null) {
            unregisterCommand(mapCommand);
            return true;
        }
        return false;
    }

    /**
     * Unregisters a command instance.
     *
     * @param command command instance
     */
    public void unregisterCommand(@NotNull Command command) {
        try {
            KNOWN_COMMANDS.remove(command.getName());
            for (final String alias : command.getAliases()) {
                KNOWN_COMMANDS.remove(alias);
            }
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Adds aliases to a command by name.
     *
     * @param command command name
     * @param aliases aliases to add
     * @return true if command was found
     */
    public boolean addAliases(@NotNull String command, List<String> aliases) {
        final Command mapCommand = COMMAND_MAP.getCommand(command);
        if (mapCommand != null) {
            addAliases(command, aliases);
            return true;
        }
        return false;
    }

    /**
     * Adds aliases to a command instance.
     *
     * @param command command instance
     * @param aliases aliases to add
     */
    public void addAliases(@NotNull Command command, List<String> aliases) {
        try {
            for (int i = 0; i < aliases.size(); i++) {
                final String alias = aliases.get(i);
                command.getAliases().add(alias);
                ((List<String>) ALIASES_FIELD.get(command)).add(alias);
                KNOWN_COMMANDS.put(alias, command);
            }
            syncCommands();
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Synchronizes commands with all online players.
     * Uses debouncing (100ms) to prevent concurrent modification exceptions on Paper 1.21+.
     * Multiple rapid calls will be coalesced into a single sync operation.
     */
    public void syncCommands() {
        synchronized (SYNC_LOCK) {
            if (scheduledSync != null && !scheduledSync.isDone()) {
                scheduledSync.cancel(false);
            }

            scheduledSync = EXECUTOR.schedule(() -> {
                try {
                    SYNC_COMMANDS_METHOD.invoke(Bukkit.getServer());
                } catch (final ReflectiveOperationException ex) {
                    throw new RuntimeException(ex);
                }
            }, 100, TimeUnit.MILLISECONDS);
        }
    }

    private SimpleCommandMap getCommandMap() {
        try {
            return (SimpleCommandMap) COMMAND_MAP_FIELD.get(Bukkit.getPluginManager());
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static {
        try {
            PLUGIN_COMMAND_CONSTRUCTOR = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            PLUGIN_COMMAND_CONSTRUCTOR.setAccessible(true);

            COMMAND_MAP_FIELD = SimplePluginManager.class.getDeclaredField("commandMap");
            COMMAND_MAP_FIELD.setAccessible(true);

            ALIASES_FIELD = Command.class.getDeclaredField("aliases");
            ALIASES_FIELD.setAccessible(true);

            SYNC_COMMANDS_METHOD = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
            SYNC_COMMANDS_METHOD.setAccessible(true);

            COMMAND_MAP = getCommandMap();

            final Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            KNOWN_COMMANDS = (HashMap<String, Command>) knownCommandsField.get(COMMAND_MAP);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
