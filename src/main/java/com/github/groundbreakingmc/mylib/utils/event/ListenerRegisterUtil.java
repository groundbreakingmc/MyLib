package com.github.groundbreakingmc.mylib.utils.event;

import lombok.experimental.UtilityClass;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused") @UtilityClass @Deprecated
public final class ListenerRegisterUtil {

    @Deprecated
    public static boolean register(final Plugin plugin,
                                   final Listener listener,
                                   final Class<? extends Event> eventClass,
                                   final EventPriority eventPriority,
                                   final boolean ignoreCancelled,
                                   final EventExecutor eventExecutor) {
        final ListenerUtils.ListenerData listenerData = new ListenerUtils.ListenerData(eventClass, null, eventPriority, ignoreCancelled);
        final Set<ListenerUtils.ListenerData> listenerDataSet = ListenerUtils.REGISTERED.computeIfAbsent(listener, k -> new HashSet<>());

        if (!listenerDataSet.isEmpty() && listenerDataSet.contains(listenerData)) {
            return false;
        }

        plugin.getServer().getPluginManager().registerEvent(
                eventClass,
                listener,
                eventPriority,
                eventExecutor,
                plugin,
                ignoreCancelled
        );
        listenerDataSet.add(listenerData);
        return true;
    }

    @Deprecated
    public static boolean unregister(final Listener listener) {
        if (ListenerUtils.REGISTERED.remove(listener) == null) {
            return false;
        }

        HandlerList.unregisterAll(listener);
        return true;
    }

    @Deprecated @Nullable
    public static EventPriority getEventPriority(final String priority) {
        return ListenerUtils.getEventPriority(priority, null);
    }

    @Deprecated
    public static EventPriority getEventPriority(final String priority, final EventPriority defaultPriority) {
        return ListenerUtils.getEventPriority(priority, defaultPriority);
    }
}
