package com.github.groundbreakingmc.mylib.utils.luckperms;

import com.github.groundbreakingmc.mylib.utils.bukkit.BukkitProviderUtils;
import lombok.experimental.UtilityClass;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
@UtilityClass
public final class LuckPermsUtils {

    private static LuckPerms LUCK_PERMS;

    public static void givePermission(final UUID playerUUID, final String permission, final boolean value) {
        givePermission(playerUUID, permission, value, null);
    }

    public static void givePermission(final UUID playerUUID, final String permission, final boolean value, final Duration duration) {
        if (LUCK_PERMS == null) {
            setLuckPerms();
        }

        LUCK_PERMS.getUserManager().loadUser(playerUUID).thenAccept(user -> {
            if (user != null) {
                final PermissionNode.Builder node = PermissionNode.builder(permission).value(value);

                if (duration != null) {
                    node.expiry(duration);
                }

                user.data().add(node.build());

                LUCK_PERMS.getUserManager().saveUser(user);
            }
        });
    }

    public static void setPlayerGroup(final UUID playerUUID, final String groupName) {
        setPlayerGroup(playerUUID, groupName, null);
    }

    public static void setPlayerGroup(final UUID playerUUID, final String groupName, final Duration duration) {
        if (LUCK_PERMS == null) {
            setLuckPerms();
        }

        final CompletableFuture<User> userFuture = LUCK_PERMS.getUserManager().loadUser(playerUUID);
        userFuture.thenAccept(user -> {
            if (user == null) {
                return;
            }

            final Optional<InheritanceNode> existingGroupNode = user.getNodes(NodeType.INHERITANCE).stream()
                    .filter(node -> node.getGroupName().equals(groupName))
                    .findFirst();

            if (existingGroupNode.isPresent() && duration != null) {
                final InheritanceNode groupNode = existingGroupNode.get();

                final Instant expiry = groupNode.getExpiry();
                if (expiry != null) {
                    if (expiry.isAfter(Instant.now().plus(duration))) {
                        return;
                    }

                    user.data().remove(groupNode);
                }
            }

            final InheritanceNode.Builder newGroupNode = InheritanceNode.builder(groupName);

            if (duration != null) {
                newGroupNode.expiry(duration);
            }

            user.data().add(newGroupNode.build());

            LUCK_PERMS.getUserManager().saveUser(user);
        });
    }

    private static void setLuckPerms() {
        if (!Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            throw new UnsupportedOperationException("LuckPerms is not enabled!");
        }

        LUCK_PERMS = BukkitProviderUtils.getProvider(Bukkit.getServicesManager(), LuckPerms.class);
    }

    public static LuckPerms getLuckPerms() {
        if (LUCK_PERMS == null) {
            setLuckPerms();
        }

        return LuckPermsUtils.LUCK_PERMS;
    }
}
