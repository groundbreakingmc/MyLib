package com.github.groundbreakingmc.mylib.actions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.actions.context.ActionContext;
import com.github.groundbreakingmc.mylib.colorizer.component.ComponentColorizer;
import com.github.groundbreakingmc.mylib.utils.vault.VaultUtils;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class MessageAction<C extends ActionContext> implements Action<C> {

    private final Component message;
    private final ComponentColorizer colorizer;
    private final boolean broadcast;
    private final Chat vaultChat;

    public MessageAction(String message, ComponentColorizer colorizer, boolean broadcast) {
        this.message = colorizer.colorize(message);
        this.colorizer = colorizer;
        this.broadcast = broadcast;
        this.vaultChat = VaultUtils.getChatProvider();
    }

    public MessageAction(Component message, ComponentColorizer colorizer, boolean broadcast) {
        this.message = message;
        this.colorizer = colorizer;
        this.broadcast = broadcast;
        this.vaultChat = VaultUtils.getChatProvider();
    }

    @Override
    public void execute(@NotNull C context) {
        final Player player = context.getPlayer();
        if (player == null) {
            return;
        }

        final Player placeholderPlayer = context.getPlaceholderPlayer() != null
                ? context.getPlaceholderPlayer()
                : player;

        final Component message = this.getReplaced(placeholderPlayer);
        if (this.broadcast) {
            for (final Player target : Bukkit.getOnlinePlayers()) {
                target.sendMessage(message);
            }
        } else {
            player.sendMessage(message);
        }
    }

    private Component getReplaced(@NotNull Player player) {
        Component result = this.message;

        if (this.vaultChat != null) {
            final Component prefix = this.colorizer.colorize(this.vaultChat.getPlayerPrefix(player));
            final Component suffix = this.colorizer.colorize(this.vaultChat.getPlayerPrefix(player));
            result = result.replaceText((builder) -> builder
                    .match("\\{prefix}")
                    .replacement(prefix)
            ).replaceText((builder) -> builder
                    .match("\\{suffix}")
                    .replacement(suffix)
            );
        }

        return result.replaceText((builder) -> builder
                .match("\\{player}")
                .replacement(player.displayName())
        );
    }
}
