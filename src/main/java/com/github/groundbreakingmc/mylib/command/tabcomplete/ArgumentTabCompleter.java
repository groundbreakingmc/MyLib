package com.github.groundbreakingmc.mylib.command.tabcomplete;

import com.github.groundbreakingmc.mylib.command.executors.ModernCommandExecutor;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ArgumentTabCompleter extends ModernTabCompleter {

    private final Map<Integer, Map<String, ModernCommandExecutor>> allArguments;

    public ArgumentTabCompleter(final Map<Integer, Map<String, ModernCommandExecutor>> arguments) {
        this.allArguments = arguments;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        final int current = args.length - 1;
        final Map<String, ModernCommandExecutor> arguments = this.allArguments.get(current);
        if (arguments == null || arguments.isEmpty()) {
            return ImmutableList.of();
        }

        final List<String> completions = new ArrayList<>();

        final String input = args[current];
        for (final Map.Entry<String, ModernCommandExecutor> entry : arguments.entrySet()) {
            final String key = entry.getKey();
            final ModernCommandExecutor value = entry.getValue();
// TODO            if (CommandUtils.startsWithIgnoreCase(input, key)
//                    && (value.getPermission() == null || sender.hasPermission(value.getPermission()))) {
//                completions.add(entry.getKey());
//            }
        }

        return completions;
    }
}
