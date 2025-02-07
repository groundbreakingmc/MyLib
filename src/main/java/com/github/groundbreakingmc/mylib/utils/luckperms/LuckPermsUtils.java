package com.github.groundbreakingmc.mylib.utils.luckperms;

import com.github.groundbreakingmc.mylib.utils.bukkit.BukkitProviderUtils;
import lombok.experimental.UtilityClass;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
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

    private static final LuckPerms LUCK_PERMS = BukkitProviderUtils.getProvider(Bukkit.getServicesManager(), LuckPerms.class);

    public static void givePermission(final UUID playerUUID, final String permission) {
        if (LUCK_PERMS == null) {
            return;
        }

        LUCK_PERMS.getUserManager().loadUser(playerUUID).thenAccept(user -> {
            if (user != null) {
                final Node node = PermissionNode.builder(permission)
                        .value(true)
                        .build();

                user.data().add(node);

                LUCK_PERMS.getUserManager().saveUser(user);
            }
        });
    }

    public static void setPlayerGroup(final UUID playerUUID, final String groupName, final Duration duration) {
        final CompletableFuture<User> userFuture = LUCK_PERMS.getUserManager().loadUser(playerUUID);
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

            LUCK_PERMS.getUserManager().saveUser(user);
        });
    }

    public static LuckPerms getLuckPerms() {
        return LuckPermsUtils.LUCK_PERMS;
    }
}
