package com.github.groundbreakingmc.mylib.vanish;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Tells whether a player sees another, as vanish plugins decide it.
 *
 * <p>Vanish plugins hide a player from each viewer who may not see them with
 * {@link Player#hidePlayer}, or hide them from everyone with
 * {@link org.bukkit.entity.Entity#setVisibleByDefault} and show them to those who may;
 * {@link Player#canSee(Player)} reflects both, see-permissions and levels included. Some hide a
 * player only from the tab list of some viewers, which Paper's {@code isListed} reflects. Checked
 * in the sources of EssentialsX, SuperVanish, PremiumVanish, CMI, SayanVanish, AdvancedVanish,
 * VanishNoPacket, SunLight, StaffPlusPlus and others. What this misses is hiding with packets
 * alone, such as the tab-list mode of StaffPlusPlus.
 *
 * <p>A player hidden for other reasons, such as by a lobby's player hider, counts as unseen too.
 */
@UtilityClass
public final class VisibleChecker {

    // Paper's; Spigot keeps no tab list per viewer.
    private static final boolean TAB_LIST = hasTabList();

    /**
     * Tells whether a player sees another, in the world and in the tab list.
     *
     * @param viewer the player looking
     * @param target the player looked at
     * @return {@code true} if the target isn't hidden from the viewer; always for the viewer
     *         themselves
     */
    public static boolean canSee(@NotNull Player viewer, @NotNull Player target) {
        return viewer.canSee(target) && (!TAB_LIST || viewer.isListed(target));
    }

    private static boolean hasTabList() {
        try {
            Player.class.getMethod("isListed", Player.class);
            return true;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }
}
