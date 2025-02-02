package com.github.groundbreakingmc.mylib.command.executors;

import com.github.groundbreakingmc.mylib.command.allowedexecutor.ExecutorCheck;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModernCommandExecutor extends ModernExecutor {

    public final int minArgs;
    public final String minArgsMessage;
    public final int maxArgs;
    public final String maxArgsMessage;

    private ModernCommandExecutor(
            final String permission, final String permissionMessage,
            final ExecutorCheck executorCheck, final String executorCheckMessage,
            final Map<String, ModernArgumentExecutor> arguments, final String argumentMessage,
            final int minArgs, final String minArgsMessage,
            final int maxArgs, final String maxArgsMessage) {
        super(permission, permissionMessage, executorCheck, executorCheckMessage, arguments, argumentMessage);
        this.minArgs = minArgs;
        this.minArgsMessage = minArgsMessage;
        this.maxArgs = maxArgs;
        this.maxArgsMessage = maxArgsMessage;
    }

    @Override
    protected boolean isAllowedToExecute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!super.isAllowedToExecute(sender, command, label, args)) {
            return false;
        }

        if (args.length < this.minArgs) {
            sender.sendMessage(this.minArgsMessage);
            return false;
        }

        if (args.length < this.maxArgs) {
            sender.sendMessage(this.maxArgsMessage);
            return false;
        }

        return true;
    }

    public static ModernCommandBuilder builder(final String command) {
        return new ModernCommandBuilder(command);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ModernCommandBuilder {
        private final String command;
        private List<String> aliases;
        private String permission;
        private String permissionMessage;
        private ExecutorCheck executorCheck;
        private String executorCheckMessage;
        private String argumentMessage;
        private int minArgs;
        private String minArgsMessage;
        private int maxArgs;
        private String maxArgsMessage;

        private final Map<String, ModernArgumentExecutor> arguments = new HashMap<>();

        public ModernCommandBuilder setPermission(final String permission) {
            this.permission = permission;
            return this;
        }

        public ModernCommandBuilder setPermissionMessage(final String permissionMessage) {
            this.permissionMessage = permissionMessage;
            return this;
        }

        public ModernCommandBuilder setExecutorCheck(final ExecutorCheck executorCheck) {
            this.executorCheck = executorCheck;
            return this;
        }

        public ModernCommandBuilder setExecutorCheckMessage(final String executorCheckMessage) {
            this.executorCheckMessage = executorCheckMessage;
            return this;
        }

        public ModernCommandBuilder setArgumentMessage(final String argumentMessage) {
            this.argumentMessage = argumentMessage;
            return this;
        }

        public ModernCommandBuilder setMinArgs(final int minArgs) {
            this.minArgs = minArgs;
            return this;
        }

        public ModernCommandBuilder setMinArgsMessage(final String minArgsMessage) {
            this.minArgsMessage = minArgsMessage;
            return this;
        }

        public ModernCommandBuilder setMaxArgs(final int maxArgs) {
            this.maxArgs = maxArgs;
            return this;
        }

        public ModernCommandBuilder setMaxArgsMessage(final String maxArgsMessage) {
            this.maxArgsMessage = maxArgsMessage;
            return this;
        }

        public ModernCommandBuilder addArgument(final String argument, final ModernArgumentExecutor argumentExecutor) {
            this.arguments.put(argument, argumentExecutor);
            return this;
        }

        public ModernCommandExecutor build() {
            return new ModernCommandExecutor(
                    this.permission, this.permissionMessage,
                    this.executorCheck, this.executorCheckMessage,
                    this.arguments, this.argumentMessage,
                    this.minArgs, this.minArgsMessage,
                    this.maxArgs, this.maxArgsMessage
            );
        }
    }
}
