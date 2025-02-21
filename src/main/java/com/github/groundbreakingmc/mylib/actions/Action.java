package com.github.groundbreakingmc.mylib.actions;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.logger.Logger;
import com.github.groundbreakingmc.mylib.utils.vault.VaultUtils;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public enum Action {
    CONSOLE("[CONSOLE]", createFactory(ConsoleCommandAction::new, "[CONSOLE]")),
    PLAYER("[PLAYER]", createFactory(PlayerCommandAction::new, "[PLAYER]")),
    LUCKPERMS("[LUCKPERMS]", createFactory(LuckPermsAction::new, "[LUCKPERMS]")),
    MONEY("[MONEY]", createFactory(MoneyAction::new, "[MONEY]")),
    BROADCAST("[BROADCAST]", createFactory(BroadcastAction::new, "[BROADCAST]")),
    MESSAGE("[MESSAGE]", createFactory(MessageAction::new, "[MESSAGE]")),
    PLAY_SOUND("[SOUNDALL]", createFactory(SoundAllAction::new, "[SOUNDALL]")),
    SOUND_ALL("[SOUND]", createFactory(SoundAction::new, "[SOUND]")),
    TITLE_ALL("[TITLEALL]", createFactory(TitleAllAction::new, "[TITLEALL]")),
    TITLE("[TITLE]", createFactory(TitleAction::new, "[TITLE]")),
    EFFECT_ALL("[EFFECTALL]", createFactory(EffectAllAction::new, "[EFFECTALL]")),
    EFFECT("[EFFECT]", createFactory(EffectAction::new, "[EFFECT]")),

    // Заглушка для пользовательских действий
    CUSTOM(null, null);

    @Getter
    private final String prefix;
    private final ActionFactory actionFactory;

    private static final Map<String, Action> BUILTIN_ACTIONS = new HashMap<>();
    private static final Map<String, ActionFactory> CUSTOM_ACTIONS = new HashMap<>();

    static {
        for (Action action : values()) {
            if (action != CUSTOM && action.prefix != null) {
                BUILTIN_ACTIONS.put(action.prefix.toLowerCase(), action);
            }
        }
    }

    Action(String prefix, ActionFactory actionFactory) {
        this.prefix = prefix;
        this.actionFactory = actionFactory;
    }

    public static void registerCustomAction(String prefix, ActionFactory factory) {
        CUSTOM_ACTIONS.putIfAbsent(prefix.toLowerCase(), factory);
    }

    @Nullable
    public static Action fromString(final String string) {
        for (final Map.Entry<String, Action> entry : BUILTIN_ACTIONS.entrySet()) {
            if (StringUtil.startsWithIgnoreCase(string, entry.getKey())) {
                return entry.getValue();
            }
        }

        for (final String prefix : CUSTOM_ACTIONS.keySet()) {
            if (StringUtil.startsWithIgnoreCase(string, prefix)) {
                return CUSTOM;
            }
        }

        return null;
    }

    @Nullable
    public ActionExecutor createAction(Plugin plugin, Logger logger, Colorizer colorizer, String string) {

        ActionFactory resultFactory = this.actionFactory;
        String resultPrefix = this.prefix;

        if (this == CUSTOM) {
            for (final Map.Entry<String, ActionFactory> entry : CUSTOM_ACTIONS.entrySet()) {
                if (StringUtil.startsWithIgnoreCase(string, entry.getKey())) {
                    resultFactory = entry.getValue();
                    resultPrefix = entry.getKey();
                    break;
                }
            }
        }

        return resultFactory.create(
                plugin,
                logger,
                colorizer,
                string.substring(resultPrefix.length()).trim(),
                resultPrefix
        );
    }

    private static ActionFactory createFactory(ActionCreator creator, String prefix) {
        return (plugin, logger, colorizer, string, key) ->
                createAction(plugin, logger, colorizer, string, creator, prefix);
    }

    @Nullable
    private static ActionExecutor createAction(
            Plugin plugin,
            Logger logger,
            Colorizer colorizer,
            String string,
            ActionCreator creator,
            String prefix
    ) {
        if (string.isEmpty()) {
            logger.warn("Missing arguments for " + prefix + " action. Check your config.");
            return null;
        }

        return creator.create(plugin, colorizer, string);
    }

    @FunctionalInterface
    private interface ActionFactory {
        ActionExecutor create(
                Plugin plugin,
                Logger logger,
                Colorizer colorizer,
                String string,
                String prefix
        );
    }

    @FunctionalInterface
    interface ActionCreator {
        ActionExecutor create(Plugin plugin, Colorizer colorizer, String string);
    }

    public abstract static class ActionExecutor {

        protected final Plugin plugin;
        protected final Chat chat;
        protected final Economy economy;
        protected final Colorizer colorizer;
        protected final String action;

        protected ActionExecutor(Plugin plugin, Colorizer colorizer, String action) {
            this.plugin = plugin;
            this.chat = VaultUtils.getChatProvider(plugin.getServer().getServicesManager());
            this.economy = VaultUtils.getEconomyProvider(plugin.getServer().getServicesManager());
            this.colorizer = colorizer;
            this.action = colorizer.colorize(action);
        }

        public abstract void execute(@NotNull Player player);
    }
}