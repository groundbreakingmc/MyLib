package com.github.groundbreakingmc.mylib.command;

import com.github.groundbreakingmc.mylib.command.annotations.ArgumentInfo;
import com.github.groundbreakingmc.mylib.command.annotations.CommandInfo;
import com.github.groundbreakingmc.mylib.command.executors.ModernArgumentExecutor;
import com.github.groundbreakingmc.mylib.command.executors.ModernCommandExecutor;
import com.github.groundbreakingmc.mylib.utils.java.ReflectionUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public final class CommandManager extends JavaPlugin {

    public static void registerCommands(final Plugin plugin, final Class<? extends ModernArgumentExecutor>... classes) {
        for (final Class<? extends ModernArgumentExecutor> clazz : classes) {
            registerCommand(plugin, clazz);
        }
    }

    public static void registerCommands(final Plugin plugin, final Object... objects) {
        for (final Object object : objects) {
            final Method[] methods = object.getClass().getMethods();
            for (final Method method : methods) {
                //registerCommand(plugin, method);
            }
        }
    }

    public static void registerCommands(final Plugin plugin, final Method... methods) {
        for (final Method method : methods) {
            //registerCommand(plugin, method);
        }
    }

    public static void registerCommand(final Plugin plugin, final Class<? extends ModernArgumentExecutor> clazz) {
        final CommandInfo info = ReflectionUtils.getAnnotation(clazz, CommandInfo.class, "Can not register class with out @CommandInfo annotation!");

        final ModernCommandExecutor.ModernCommandBuilder commandBuilder = ModernCommandExecutor.builder(info.command());
        commandBuilder.setPermission(info.permission());
        commandBuilder.setPermissionMessage(info.permissionMessage());
        try {
            commandBuilder.setExecutorCheck(info.allowedOnly().newInstance());
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        commandBuilder.setExecutorCheckMessage(info.executorCheckMessage());
        commandBuilder.setArgumentMessage(info.argumentMessage());
        commandBuilder.setMinArgs(info.minArgs());
        commandBuilder.setMinArgsMessage(info.minArgsMessage());
        commandBuilder.setMaxArgs(info.maxArgs());
        commandBuilder.setMaxArgsMessage(info.maxArgsMessage());

        for (final Class<? extends ModernArgumentExecutor> argument : info.arguments()) {
            //getArgument(pl)
        }


//        CommandRuntimeUtils.register(
//                plugin,
//                info.command(),
//                Arrays.asList(info.aliases()),
//                modernExecutor
//        );
    }

    public static ModernArgumentExecutor getArgument(final Plugin plugin, final Class<? extends ModernArgumentExecutor> argumentClass) {
        final ArgumentInfo info = ReflectionUtils.getAnnotation(argumentClass, ArgumentInfo.class, "Can not register argument class with out @CommandInfo annotation!");

        final ModernArgumentExecutor.ModernArgumentBuilder argumentBuilder = ModernArgumentExecutor.builder();
        argumentBuilder.setPermission(info.permission());
        argumentBuilder.setPermissionMessage(info.permissionMessage());
        try {
            argumentBuilder.setExecutorCheck(info.allowedOnly().newInstance());
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        argumentBuilder.setExecutorCheckMessage(info.executorCheckMessage());
        argumentBuilder.setArgumentMessage(info.argumentMessage());

        return argumentBuilder.build();
    }
}
