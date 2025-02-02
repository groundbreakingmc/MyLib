package com.github.groundbreakingmc.mylib.command.executors;

import com.github.groundbreakingmc.mylib.command.allowedexecutor.ExecutorCheck;
import com.github.groundbreakingmc.mylib.command.annotations.ArgumentInfo;
import com.github.groundbreakingmc.mylib.utils.java.ReflectionUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class ModernExecutor implements CommandExecutor {

    public final String permission;
    public final String permissionMessage;
    public final ExecutorCheck executorCheck;
    public final String executorCheckMessage;
    public final Map<String, ModernArgumentExecutor> arguments;
    public final String argumentMessage;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!this.isAllowedToExecute(sender, command, label, args)) {
            return true;
        }

        return this.execute(sender, command, label, args);
    }

    protected boolean isAllowedToExecute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!this.executorCheck.check(sender)) {
            sender.sendMessage(this.executorCheckMessage);
            return false;
        }

        if (!this.permission.isEmpty() && !sender.hasPermission(this.permission)) {
            sender.sendMessage(this.permissionMessage);
            return false;
        }

        return true;
    }

    protected boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final String input = args[0].toLowerCase();
        final ModernArgumentExecutor executor = this.arguments.get(input);
        if (executor == null) {
            sender.sendMessage(this.argumentMessage);
            return true;
        }

        return executor.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
    }

    public void registerArgument(final ModernArgumentExecutor argumentExecutor) {

// TODO
//        argumentExecutor.setPermission(info.permission());
//        argumentExecutor.setPermissionMessage(info.permissionMessage());
//        try {
//            argumentExecutor.setExecutorCheck(info.allowedOnly().newInstance());
//        } catch (final Exception ex) {
//            ex.printStackTrace();
//        }
//        argumentExecutor.setExecutorCheckMessage(info.executorCheckMessage());
//
//        this.arguments.put(info.argument(), argumentExecutor);
    }

    public void registerArgument(final Plugin plugin, final Class<? extends ModernArgumentExecutor> argumentClass) {
        final ArgumentInfo info = ReflectionUtils.getAnnotation(argumentClass, ArgumentInfo.class, "Can not register argument class with out @CommandInfo annotation!");

        try {
            final Constructor<? extends ModernArgumentExecutor> constructor = argumentClass.getConstructor(Plugin.class, String.class, String.class, ExecutorCheck.class, String.class);
            constructor.setAccessible(true);

            final ModernArgumentExecutor argumentExecutor = constructor.newInstance(
                    plugin,
                    info.permission(), info.permissionMessage(),
                    info.allowedOnly().newInstance(), info.executorCheckMessage()
            );

            this.arguments.put(info.argument(), argumentExecutor);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }
}
