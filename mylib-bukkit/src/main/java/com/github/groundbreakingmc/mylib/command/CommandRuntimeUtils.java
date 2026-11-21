package com.github.groundbreakingmc.mylib.command;

import com.github.groundbreakingmc.mylib.scheduler.PluginScheduler;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for runtime command registration and management.
 * Provides methods to register, unregister, and modify commands without plugin.yml.
 *
 * <p>Uses reflection to access Bukkit's internal command map. Call the methods on the server
 * thread, the global region thread on Folia. Players get the changed commands on the next tick,
 * once for all changes made during that tick.</p>
 */
@UtilityClass
@SuppressWarnings({"unchecked", "unused"})
public class CommandRuntimeUtils {

    private final Constructor<PluginCommand> PLUGIN_COMMAND_CONSTRUCTOR;
    private final Field ALIASES_FIELD;
    /**
     * The server's command map.
     */
    public final SimpleCommandMap COMMAND_MAP;
    // On Paper a view of Brigadier that forwards get, containsKey, put, remove and its views, but
    // not the other Map methods: putIfAbsent, for one, would fill the HashMap beneath, which nothing reads.
    private final Map<String, Command> KNOWN_COMMANDS;
    // Plugins whose players get the commands on the next tick.
    private final Set<Plugin> PENDING_SYNCS = ConcurrentHashMap.newKeySet();

    /**
     * Registers a command with a tab executor (handles both execution and tab completion).
     *
     * @param plugin      plugin instance
     * @param command     command name
     * @param tabExecutor tab executor
     * @return the registered command, to pass to {@link #unregisterCommand(Command)}
     */
    public PluginCommand register(@NotNull Plugin plugin,
                                  @NotNull String command,
                                  @NotNull TabExecutor tabExecutor) {
        return register(plugin, command, null, tabExecutor, tabExecutor);
    }

    /**
     * Registers a command with a tab executor (handles both execution and tab completion).
     * The first element of {@code aliases} is used as the command name,
     * the remaining elements are registered as aliases.
     *
     * @param plugin      plugin instance
     * @param aliases     list where the first element is the command name and the rest are aliases
     * @param tabExecutor tab executor
     * @return the registered command, to pass to {@link #unregisterCommand(Command)}
     */
    public PluginCommand register(@NotNull Plugin plugin,
                                  @NotNull List<String> aliases,
                                  @NotNull TabExecutor tabExecutor) {
        return register(plugin, aliases, tabExecutor, tabExecutor);
    }

    /**
     * Registers a command with executor and optional tab completer.
     *
     * @param plugin          plugin instance
     * @param command         command name
     * @param commandExecutor command executor
     * @param tabCompleter    tab completer (nullable)
     * @return the registered command, to pass to {@link #unregisterCommand(Command)}
     */
    public PluginCommand register(@NotNull Plugin plugin,
                                  @NotNull String command,
                                  @NotNull CommandExecutor commandExecutor,
                                  @Nullable TabCompleter tabCompleter) {
        return register(plugin, command, null, commandExecutor, tabCompleter);
    }

    /**
     * Registers a command with executor and optional tab completer.
     * The first element of {@code aliases} is used as the command name,
     * the remaining elements are registered as aliases.
     *
     * @param plugin          plugin instance
     * @param aliases         list where the first element is the command name and the rest are aliases
     * @param commandExecutor command executor
     * @param tabCompleter    tab completer (nullable)
     * @return the registered command, to pass to {@link #unregisterCommand(Command)}
     */
    public PluginCommand register(@NotNull Plugin plugin,
                                  @NotNull List<String> aliases,
                                  @NotNull CommandExecutor commandExecutor,
                                  @Nullable TabCompleter tabCompleter) {
        final String command = aliases.get(0);
        final List<String> rest = aliases.size() > 1 ? aliases.subList(1, aliases.size()) : null;
        return register(plugin, command, rest, commandExecutor, tabCompleter);
    }

    /**
     * Core registration logic shared by all public {@code register} overloads.
     *
     * @param plugin          plugin instance
     * @param command         command name
     * @param aliases         command aliases (nullable)
     * @param commandExecutor command executor
     * @param tabCompleter    tab completer (nullable)
     * @return the registered command
     */
    private PluginCommand register(@NotNull Plugin plugin,
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
            // A copy: the command keeps the list, and a view of the caller's list, such as the
            // subList above, fails once that list changes.
            pluginCommand.setAliases(new ArrayList<>(aliases));
        }

        // A label another plugin already holds stays theirs; the command still gets "plugin:label".
        COMMAND_MAP.register(plugin.getName(), pluginCommand);

        syncLater(plugin);
        return pluginCommand;
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
     * Unregisters the command a label leads to, under all of its labels.
     *
     * <p>A label may lead to another plugin's command, if that plugin took it first. To remove
     * your own command, pass the command {@code register} returned to {@link #unregisterCommand(Command)}.
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
     * Unregisters a command under every label that leads to it, the {@code plugin:label} forms
     * included. A label that leads to another command, such as an alias another plugin took first,
     * stays theirs.
     *
     * @param command command instance
     */
    public void unregisterCommand(@NotNull Command command) {
        KNOWN_COMMANDS.values().removeIf(known -> known == command);

        // So the command can be registered again.
        command.unregister(COMMAND_MAP);
        syncLater(command);
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
            addAliases(mapCommand, aliases);
            return true;
        }
        return false;
    }

    /**
     * Adds aliases to a command instance. As in the command map, an alias another command already
     * holds stays theirs, and a plugin command also gets {@code plugin:alias}.
     *
     * @param command command instance
     * @param aliases aliases to add
     */
    public void addAliases(@NotNull Command command, List<String> aliases) {
        final String prefix = command instanceof PluginCommand pluginCommand
                ? pluginCommand.getPlugin().getName().toLowerCase(Locale.ENGLISH)
                : null;
        try {
            // The configured aliases, which the command uses again when it is registered anew. A copy:
            // the command may hold an immutable list.
            final List<String> configured = new ArrayList<>((List<String>) ALIASES_FIELD.get(command));
            for (final String alias : aliases) {
                final String label = alias.toLowerCase(Locale.ENGLISH).trim();
                configured.add(label);
                if (prefix != null) {
                    KNOWN_COMMANDS.put(prefix + ":" + label, command);
                }
                if (!KNOWN_COMMANDS.containsKey(label)) {
                    KNOWN_COMMANDS.put(label, command);
                    command.getAliases().add(label);
                }
            }
            command.setAliases(configured);
        } catch (final IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
        syncLater(command);
    }

    /**
     * Sends the commands to all online players right away, on the calling thread.
     * Registering and unregistering already do this on the next tick.
     */
    public void syncCommands() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            player.updateCommands();
        }
    }

    // Each sync sends every player the whole command tree, so the changes of a tick share one.
    private void syncLater(@NotNull Plugin plugin) {
        // A disabled plugin can't schedule, such as one removing its commands in onDisable.
        if (!plugin.isEnabled()) {
            PENDING_SYNCS.remove(plugin);
            syncCommands();
            return;
        }
        if (!PENDING_SYNCS.add(plugin)) {
            return;
        }
        final PluginScheduler scheduler = PluginScheduler.of(plugin);
        scheduler.global(() -> {
            PENDING_SYNCS.remove(plugin);
            for (final Player player : Bukkit.getOnlinePlayers()) {
                // On Folia the player's own region thread sends its commands.
                scheduler.execute(player, player::updateCommands, null);
            }
        });
    }

    private void syncLater(@NotNull Command command) {
        if (command instanceof PluginCommand pluginCommand) {
            syncLater(pluginCommand.getPlugin());
        } else {
            syncCommands();
        }
    }

    // Server#getCommandMap is Paper's; Spigot has the method only on CraftServer, so it is looked
    // up on the server's class. SimplePluginManager, which holds the map too, is marked for removal.
    private SimpleCommandMap getCommandMap() {
        try {
            final Object server = Bukkit.getServer();
            return (SimpleCommandMap) server.getClass().getMethod("getCommandMap").invoke(server);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static {
        try {
            PLUGIN_COMMAND_CONSTRUCTOR = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            PLUGIN_COMMAND_CONSTRUCTOR.setAccessible(true);

            ALIASES_FIELD = Command.class.getDeclaredField("aliases");
            ALIASES_FIELD.setAccessible(true);

            COMMAND_MAP = getCommandMap();

            final Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            KNOWN_COMMANDS = (Map<String, Command>) knownCommandsField.get(COMMAND_MAP);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
