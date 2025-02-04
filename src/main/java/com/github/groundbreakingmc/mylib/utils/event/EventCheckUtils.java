package com.github.groundbreakingmc.mylib.utils.event;

import lombok.experimental.UtilityClass;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

@UtilityClass
public final class EventCheckUtils {

    public static boolean clickedOnBlock(final PlayerInteractEvent event) {
        return event.getAction() == Action.LEFT_CLICK_BLOCK
                || event.getAction() == Action.RIGHT_CLICK_BLOCK;
    }

    public static boolean clickedOnAir(final PlayerInteractEvent event) {
        return event.getAction() == Action.LEFT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_AIR;
    }
}
