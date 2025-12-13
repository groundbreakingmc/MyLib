package com.github.groundbreakingmc.mylib.actions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.actions.context.ActionContext;
import com.github.groundbreakingmc.mylib.colorizer.component.ComponentColorizer;
import com.github.groundbreakingmc.mylib.utils.vault.VaultUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

@SuppressWarnings("unused")
public final class TitleAction<C extends ActionContext> implements Action<C> {

    private final ComponentColorizer colorizer;
    private final Chat vaultChat;

    private final Component title;
    private final Component subtitle;
    private final Title.Times times;

    public TitleAction(@NotNull String action, @NotNull ComponentColorizer colorizer) {
        this.colorizer = colorizer;
        this.vaultChat = VaultUtils.getChatProvider();

        final String[] params = action.split(";");
        this.title = colorizer.colorize(params[0]);
        this.subtitle = params.length > 1 ? colorizer.colorize(params[1]) : Component.empty();

        final Duration in = Ticks.duration(params.length > 2 ? Integer.parseInt(params[2]) : 10);
        final Duration stay = Ticks.duration(params.length > 3 ? Integer.parseInt(params[3]) : 40);
        final Duration out = Ticks.duration(params.length > 4 ? Integer.parseInt(params[4]) : 10);
        this.times = Title.Times.times(in, stay, out);
    }

    public TitleAction(@NotNull Component title,
                       @NotNull Component subtitle,
                       @NotNull Title.Times times,
                       @NotNull ComponentColorizer colorizer) {
        this.title = title;
        this.subtitle = subtitle;
        this.times = times;
        this.colorizer = colorizer;
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

        final Component replacedTitle = this.getReplaced(placeholderPlayer, this.title);
        final Component replacedSubtitle = this.getReplaced(placeholderPlayer, this.subtitle);

        player.showTitle(Title.title(replacedTitle, replacedSubtitle, this.times));
    }

    private Component getReplaced(@NotNull Player player, Component toReplace) {
        Component result = toReplace;

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
