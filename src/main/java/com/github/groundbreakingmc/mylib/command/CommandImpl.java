package com.github.groundbreakingmc.mylib.command;

import com.github.groundbreakingmc.mylib.utils.command.CommandUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@AllArgsConstructor @Getter @Setter @Builder
public final class CommandImpl implements TabExecutor {

    @Builder.Default
    private final Map<String, CommandImpl> arguments = new HashMap<>();
    private final Executor command;
    private final Executor tabComplete;
    @Builder.Default
    private final boolean enableLibTabComplete = true;
    private final int nextArgumentOrdinalNumb;

    private String argumentNotFound;
    private String permission;
    private String permissionMessage;
    private int minArgs;
    private String minArgsMessage;
    private int maxArgs;
    private String maxArgsMessage;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (this.permission != null
                && !this.permission.isEmpty()
                && !sender.hasPermission(this.permission)) {
            sender.sendMessage(this.permissionMessage != null
                    ? this.permissionMessage
                    : Bukkit.getPermissionMessage()
            );
            return true;
        }

        if (!this.isArgsLengthValid(sender, args)) {
            return true;
        }

        if (!this.arguments.isEmpty()) {
            final CommandImpl argument = this.arguments.get(args[this.nextArgumentOrdinalNumb]);
            if (argument != null) {
                return argument.onCommand(sender, command, label, args);
            }

            return true;
        }

        try {
            return (boolean) this.command.executeMethod.invoke(this.command.executor, sender, command, label, args);
        } catch (final Exception ex) {
            ex.printStackTrace();
            return true;
        }
    }

    private boolean isArgsLengthValid(final CommandSender sender, final String[] args) {
        if (args.length < (this.arguments.isEmpty() ? args.length : 1)) {
            if (this.minArgsMessage != null) {
                sender.sendMessage(this.minArgsMessage);
            }
            return false;
        }

        if (args.length > maxArgs) {
            if (maxArgsMessage != null) {
                sender.sendMessage(maxArgsMessage);
            }
            return false;
        }

        return true;
    }

    public void addArgument(final String argument, final CommandImpl implementation) {
        this.arguments.put(argument, implementation);
    }

    public void removeArgument(final String argument) {
        this.arguments.remove(argument);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> completions = new ArrayList<>();
        if (this.enableLibTabComplete) {
            final String input = args[args.length - 1];
            if (args.length == 1 && !this.arguments.isEmpty()) {
                for (final Map.Entry<String, CommandImpl> entry : this.arguments.entrySet()) {
                    final String key = entry.getKey();
                    final CommandImpl value = entry.getValue();
                    if (CommandUtils.startsWithIgnoreCase(input, key)
                            && (value.permission == null || sender.hasPermission(value.permission))) {
                        completions.add(entry.getKey());
                    }
                }

                return completions;
            }

            final CommandImpl argument = this.arguments.get(input);
            if (argument != null) {
                if (argument.permission != null && sender.hasPermission(argument.permission)) {
                    final String[] cloned = Arrays.copyOfRange(args, 1, args.length);
                    return argument.onTabComplete(sender, command, alias, cloned);
                } else {
                    return completions;
                }
            }
        }

        try {
            return this.tabComplete != null
                    ? (List<String>) this.tabComplete.executeMethod.invoke(this.tabComplete.executor, sender, command, alias, args)
                    : completions;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return completions;
        }
    }
}
