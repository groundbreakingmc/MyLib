package com.github.groundbreakingmc.mylib.utils.luckperms;

import lombok.experimental.UtilityClass;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class LuckPermsUtils {

    private static LuckPerms luckPerms;

    public static void givePermission(final UUID playerUUID, final String permission) {
        if (luckPerms == null) {
            return;
        }

        luckPerms.getUserManager().loadUser(playerUUID).thenAccept(user -> {
            if (user != null) {
                final Node node = PermissionNode.builder(permission)
                        .value(true)
                        .build();

                user.data().add(node);

                luckPerms.getUserManager().saveUser(user);
            }
        });
    }

    public static void setPlayerGroup(final UUID playerUUID, final String groupName, final Duration duration) {
        final CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(playerUUID);
        userFuture.thenAccept(user -> {
            if (user == null) {
                return;
            }

            final Optional<InheritanceNode> existingGroupNode = user.getNodes().stream()
                    .filter(node -> node instanceof InheritanceNode)
                    .map(node -> (InheritanceNode) node)
                    .filter(node -> node.getGroupName().equalsIgnoreCase(groupName))
                    .findFirst();

            if (existingGroupNode.isPresent() && duration != null) {
                final InheritanceNode groupNode = existingGroupNode.get();

                if (!groupNode.hasExpiry()) {
                    return;
                }

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
                newGroupNode.expiry(Duration.ofMillis(System.currentTimeMillis() + duration.toMillis()));
            }

            user.data().add(newGroupNode.build());

            luckPerms.getUserManager().saveUser(user);
        });
    }

    static {
        final RegisteredServiceProvider<LuckPerms> registration = Bukkit.getServicesManager()
                .getRegistration(LuckPerms.class);
        if (registration != null) {
            luckPerms = registration.getProvider();
        }
    }
}
