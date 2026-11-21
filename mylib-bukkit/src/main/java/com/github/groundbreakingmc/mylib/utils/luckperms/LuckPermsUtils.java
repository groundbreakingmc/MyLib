package com.github.groundbreakingmc.mylib.utils.luckperms;

import com.github.groundbreakingmc.mylib.utils.bukkit.BukkitProviderUtils;
import lombok.experimental.UtilityClass;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeBuilder;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A utility class for simplified interaction with LuckPerms API.
 * Supports permission and group manipulation with optional durations and override behavior.
 */
@UtilityClass
@SuppressWarnings("unused")
public final class LuckPermsUtils {

    private static LuckPerms LUCK_PERMS;

    /**
     * Grants a permission node to a player.
     *
     * @param playerUUID UUID of the player
     * @param permission the permission string (e.g. "example.use")
     * @param duration   optional duration of the permission (null for permanent)
     * @param value      whether the permission should be granted or denied
     * @param override   whether to replace an existing node with the same permission
     * @return a {@link CompletableFuture} indicating completion
     */
    public @NotNull CompletableFuture<Void> addPermission(@NotNull UUID playerUUID,
                                                          @NotNull String permission,
                                                          @Nullable Duration duration,
                                                          boolean value,
                                                          boolean override) {
        return editUser(playerUUID, user -> {
            for (final PermissionNode node : user.getNodes(NodeType.PERMISSION)) {
                if (node.getPermission().equalsIgnoreCase(permission)) {
                    if (!override) return;
                    user.data().remove(node);
                }
            }

            applyNode(user, PermissionNode.builder(permission).value(value), duration);
        });
    }

    /**
     * Removes a permission node from a player.
     *
     * @param playerUUID UUID of the player
     * @param permission the permission string to remove
     * @return a {@link CompletableFuture} indicating completion
     */
    public @NotNull CompletableFuture<Void> removePermission(@NotNull UUID playerUUID, @NotNull String permission) {
        return editUser(playerUUID, user -> {
            for (final PermissionNode node : user.getNodes(NodeType.PERMISSION)) {
                if (node.getPermission().equalsIgnoreCase(permission)) {
                    user.data().remove(node);
                }
            }
        });
    }

    /**
     * Adds a group to a player for a specified duration or permanently.
     *
     * @param playerUUID UUID of the player
     * @param groupName  the group name
     * @param duration   optional duration (null for permanent)
     * @return a {@link CompletableFuture} indicating completion
     */
    public @NotNull CompletableFuture<Void> addGroup(@NotNull UUID playerUUID, @NotNull String groupName, @Nullable Duration duration) {
        return editUser(playerUUID, user -> {
            applyNode(user, InheritanceNode.builder(groupName), duration);
        });
    }

    /**
     * Adds a group or extends its expiration time if already present.
     * If the group is already permanent, it will not be overridden.
     *
     * @param playerUUID UUID of the player
     * @param groupName  group to assign
     * @param duration   duration to add (or extend by)
     * @return a {@link CompletableFuture} indicating completion
     */
    public @NotNull CompletableFuture<Void> addOrExtendGroup(@NotNull UUID playerUUID,
                                                             @NotNull String groupName,
                                                             @Nullable Duration duration) {
        return editUser(playerUUID, user -> {
            Duration newDuration = duration;
            if (duration != null) {
                InheritanceNode currentNode = null;
                for (final InheritanceNode node : user.getNodes(NodeType.INHERITANCE)) {
                    if (node.getGroupName().equalsIgnoreCase(groupName)) {
                        final Instant expiry = node.getExpiry();
                        if (expiry == null) return; // already permanent
                        if (currentNode == null || expiry.isAfter(currentNode.getExpiry())) {
                            currentNode = node;
                        }
                    }
                }

                if (currentNode != null) {
                    newDuration = Duration.ofSeconds(currentNode.getExpiry().plus(duration).getEpochSecond());
                }
            }

            applyNode(user, InheritanceNode.builder(groupName), newDuration);
        });
    }

    /**
     * Assigns a group to a player. Can optionally override an existing assignment.
     *
     * @param playerUUID UUID of the player
     * @param groupName  name of the group
     * @param duration   optional duration for the group (null for permanent)
     * @param override   whether to remove existing group assignment before setting
     * @return a {@link CompletableFuture} indicating completion
     */
    public @NotNull CompletableFuture<Void> setGroup(@NotNull UUID playerUUID,
                                                     @NotNull String groupName,
                                                     @Nullable Duration duration,
                                                     boolean override) {
        return editUser(playerUUID, user -> {
            for (final InheritanceNode node : user.getNodes(NodeType.INHERITANCE)) {
                if (node.getGroupName().equalsIgnoreCase(groupName)) {
                    if (!override) return;
                    user.data().remove(node);
                }
            }

            applyNode(user, InheritanceNode.builder(groupName), duration);
        });
    }

    /**
     * Modifies a user with a given action, saving and cleaning up after the modification.
     *
     * @param playerUUID   UUID of the player
     * @param userConsumer the consumer to perform edits on the user
     * @return a {@link CompletableFuture} indicating completion
     */
    public @NotNull CompletableFuture<Void> editUser(@NotNull UUID playerUUID,
                                                     @NotNull Consumer<User> userConsumer) {
        return getLuckPerms().getUserManager().loadUser(playerUUID).thenAccept(user -> {
            userConsumer.accept(user);
            LUCK_PERMS.getUserManager().saveUser(user);
            LUCK_PERMS.getUserManager().cleanupUser(user);
        });
    }

    /**
     * Returns the current {@code LuckPerms} instance, initializing it lazily if needed.
     *
     * @return the LuckPerms instance
     * @throws UnsupportedOperationException if the plugin is not enabled
     */
    public LuckPerms getLuckPerms() {
        if (LUCK_PERMS == null) {
            setLuckPerms();
        }

        return LUCK_PERMS;
    }

    /**
     * Initializes the LuckPerms instance from the Bukkit service manager.
     *
     * @throws UnsupportedOperationException if the plugin is not enabled
     */
    private void setLuckPerms() {
        if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            throw new UnsupportedOperationException("LuckPerms is not enabled!");
        }

        LUCK_PERMS = BukkitProviderUtils.getProvider(Bukkit.getServicesManager(), LuckPerms.class);
    }

    // Internal helper methods

    private void applyNode(User user, NodeBuilder<?, ?> nodeBuilder, @Nullable Duration duration) {
        if (duration != null) {
            nodeBuilder.expiry(duration);
        }

        user.data().add(nodeBuilder.build());
    }

    private void applyExpiry(NodeBuilder<?, ?> builder, @Nullable Duration duration) {
        if (duration != null) {
            builder.expiry(duration);
        }
    }
}
