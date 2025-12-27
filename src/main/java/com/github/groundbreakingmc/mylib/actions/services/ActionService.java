package com.github.groundbreakingmc.mylib.actions.services;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.actions.context.ActionContext;
import com.github.groundbreakingmc.mylib.actions.factories.ActionCreator;
import com.github.groundbreakingmc.mylib.actions.impl.*;
import com.github.groundbreakingmc.mylib.colorizer.component.ComponentColorizer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Central service for managing and creating actions from string representations.
 * <p>
 * This service maintains a registry of {@link ActionCreator} instances that can parse
 * action strings and create corresponding {@link Action} objects. Actions are identified
 * by their prefix (e.g., "message:", "sound:", "command:").
 * <p>
 * Example usage:
 * <pre>
 * ActionService&lt;PlayerContext&gt; service = new ActionService&lt;&gt;();
 * service.registerDefaultActions(colorizer);
 *
 * Action action = service.fromString("message: Hello World");
 * if (action != null) {
 *     action.execute(context);
 * }
 * </pre>
 * <p>
 * The service supports:
 * <ul>
 *   <li>Registering custom action creators</li>
 *   <li>Parsing action strings by prefix matching</li>
 *   <li>Overriding existing actions</li>
 *   <li>Case-insensitive prefix matching</li>
 * </ul>
 *
 * @param <C> the type of context required by actions in this service
 * @author groundbreakingmc
 * @see Action
 * @see ActionCreator
 * @see ActionContext
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ActionService<C extends ActionContext> {

    private final Set<ActionCreator<C>> actions = new ObjectOpenHashSet<>();

    /**
     * Registers a new action creator in the service.
     * <p>
     * The action creator will be used to parse action strings that start with
     * its prefix (case-insensitive). If an action with the same prefix already
     * exists, the behavior depends on the {@code override} parameter.
     * <p>
     * Example:
     * <pre>
     * service.register(new ActionCreator&lt;&gt;() {
     *     public String getPrefix() { return "custom:"; }
     *     public Action create(String action) {
     *         return new CustomAction(action.substring(7));
     *     }
     * }, false);
     * </pre>
     *
     * @param actionCreator the action creator to register, must not be null
     * @param override      if true, replaces any existing action with the same prefix;
     *                      if false, registration fails if prefix already exists
     * @return true if the action was successfully registered, false if a conflict
     * occurred and override was false
     */
    public boolean register(@NotNull ActionCreator<C> actionCreator, boolean override) {
        if (!override) {
            for (final ActionCreator<C> target : this.actions) {
                if (target.getPrefix().equalsIgnoreCase(actionCreator.getPrefix())) {
                    return false;
                }
            }
        }

        this.actions.add(actionCreator);
        return true;
    }

    /**
     * Parses an action string and creates the corresponding Action instance.
     * <p>
     * This method searches through all registered action creators and finds the first
     * one whose prefix matches the beginning of the action string (case-insensitive).
     * The matching creator is then used to create the Action object.
     * <p>
     * Example:
     * <pre>
     * Action action1 = service.fromString("message: Hello World");
     * Action action2 = service.fromString("sound: minecraft:entity.experience_orb.pickup;1.0;1.0");
     * Action action3 = service.fromString("console: give {player} diamond 1");
     * </pre>
     *
     * @param action the action string to parse, must not be null
     * @return the created Action instance, or null if no matching prefix was found
     */
    @Nullable
    public Action<C> fromString(@NotNull String action) {
        for (final ActionCreator<C> target : this.actions) {
            if (StringUtil.startsWithIgnoreCase(action, target.getPrefix())) {
                return target.create(action);
            }
        }

        return null;
    }

    /**
     * Retrieves a registered {@link ActionCreator} by its prefix.
     * <p>
     * The lookup is performed using case-insensitive comparison.
     * If multiple creators theoretically shared the same prefix
     * (which should not happen unless overriding is misused),
     * the first matching creator is returned.
     * <p>
     * Example:
     * <pre>
     * ActionCreator&lt;PlayerContext&gt; creator =
     *     service.getByPrefix("[message]");
     * </pre>
     *
     * @param prefix the prefix to search for, must not be null
     * @return the matching {@link ActionCreator}, or {@code null}
     * if no creator with the given prefix is registered
     * @see ActionCreator#getPrefix()
     */
    @Nullable
    public ActionCreator<C> byPrefix(@NotNull String prefix) {
        for (final ActionCreator<C> target : this.actions) {
            if (target.getPrefix().equalsIgnoreCase(prefix)) {
                return target;
            }
        }

        return null;
    }

    /**
     * Registers default action creators for common action types.
     * <p>
     * This method registers the following built-in actions:
     * <ul>
     *   <li><b>[message]</b> - Sends a message to the player</li>
     *   <li><b>[broadcast]</b> - Broadcasts a message to all players</li>
     *   <li><b>[player]</b> - Executes a command as the player</li>
     *   <li><b>[console]</b> - Executes a command as console</li>
     *   <li><b>[title]</b> - Shows a title to the player</li>
     *   <li><b>[sound]</b> - Plays a sound to the player</li>
     *   <li><b>[effect]</b> - Applies a potion effect to the player</li>
     * </ul>
     * <p>
     * All existing actions will be overridden by default actions.
     *
     * @param colorizer the colorizer to use for message and title actions
     */
    public void registerDefaultActions(@NotNull ComponentColorizer colorizer) {
        this.register(new ActionCreator<>("[message]", action -> {
            return new MessageAction<>(action, colorizer, false);
        }), true);

        this.register(new ActionCreator<>("[broadcast]", action -> {
            return new MessageAction<>(action, colorizer, true);
        }), true);

        this.register(new ActionCreator<>("[player]", action -> {
            return new CommandAction<>(action, false);
        }), true);

        this.register(new ActionCreator<>("[console]", action -> {
            return new CommandAction<>(action, true);
        }), true);

        this.register(new ActionCreator<>("[title]", action -> {
            return new TitleAction<>(action, colorizer);
        }), true);

        this.register(new ActionCreator<>("[sound]", SoundAction::new), true);
        this.register(new ActionCreator<>("[effect]", EffectAction::new), true);
    }
}
