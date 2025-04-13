package com.github.groundbreakingmc.mylib.utils.command;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@UtilityClass
public final class CommandRuntimeUtils {

    public static final SimpleCommandMap COMMAND_MAP = getCommandMap();

    public static void register(final Plugin plugin, final String command, final CommandExecutor commandExecutor) {
        register(plugin, command, new ArrayList<>(), commandExecutor, null);
    }

    public static void register(final Plugin plugin, final String command, final TabExecutor tabExecutor) {
        register(plugin, command, new ArrayList<>(), tabExecutor, tabExecutor);
    }

    public static void register(final Plugin plugin, final String command, final List<String> aliases, final CommandExecutor commandExecutor) {
        register(plugin, command, aliases, commandExecutor, null);
    }

    public static void register(final Plugin plugin, final String command, final List<String> aliases, final TabExecutor tabExecutor) {
        register(plugin, command, aliases, tabExecutor, tabExecutor);
    }

    public static void register(final Plugin plugin,
                                final String command,
                                final CommandExecutor commandExecutor,
                                final TabCompleter tabCompleter) {
        register(plugin, command, new ArrayList<>(), commandExecutor, tabCompleter);
    }

    public static void register(final Plugin plugin,
                                final String command,
                                final List<String> aliases,
                                final CommandExecutor commandExecutor,
                                final TabCompleter tabCompleter) {
        final PluginCommand pluginCommand = getCustomCommand(plugin, command);
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

    public PluginCommand getCustomCommand(final Plugin plugin, final String command) {
        try {
            final Constructor<PluginCommand> constructor =
                    PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);

            return constructor.newInstance(command, plugin);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void unregisterCustomCommand(final Plugin plugin, final String command) {
        unregisterCustomCommand(plugin, COMMAND_MAP.getCommand(command));
    }

    public void unregisterCustomCommand(final Plugin plugin, final Command command) {
        try {
            final Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            final Object map = knownCommandsField.get(COMMAND_MAP);

            final HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
            knownCommands.remove(command.getName());

            for (final String alias : command.getAliases()) {
                if (knownCommands.containsKey(alias)
                        && knownCommands.get(alias).toString().contains(plugin.getName())) {
                    knownCommands.remove(alias);
                }
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void syncCommands() {
        try {
            final Method syncCommandsMethod = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
            syncCommandsMethod.setAccessible(true);
            syncCommandsMethod.invoke(Bukkit.getServer());
        } catch (final ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

    public static SimpleCommandMap getCommandMap() {
        try {
            final Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            return (SimpleCommandMap) commandMapField.get(Bukkit.getPluginManager());
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
