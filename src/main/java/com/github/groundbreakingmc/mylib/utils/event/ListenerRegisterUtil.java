package com.github.groundbreakingmc.mylib.utils.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@UtilityClass @SuppressWarnings("unused")
public final class ListenerRegisterUtil {

    private static final Map<Listener, Set<ListenerData>> REGISTERED = new HashMap<>();

    public static boolean register(final Plugin plugin,
                                   final Listener listener,
                                   final Class<? extends Event> eventClass,
                                   final EventPriority eventPriority,
                                   final boolean ignoreCancelled,
                                   final EventExecutor eventExecutor) {
        final ListenerData listenerData = new ListenerData(eventClass, eventPriority, ignoreCancelled, eventExecutor);
        final Set<ListenerData> listenerDataSet = REGISTERED.computeIfAbsent(listener, k -> new HashSet<>());

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

    public static boolean unregister(final Listener listener) {
        if (REGISTERED.remove(listener) == null) {
            return false;
        }

        HandlerList.unregisterAll(listener);
        return true;
    }

    @Nullable
    public static EventPriority getEventPriority(final String priority) {
        return getEventPriority(priority, null);
    }

    public static EventPriority getEventPriority(final String priority, final EventPriority defaultPriority) {
        switch (priority) {
            case "LOWEST":
                return EventPriority.LOWEST;
            case "LOW":
                return EventPriority.LOW;
            case "NORMAL":
                return EventPriority.NORMAL;
            case "HIGH":
                return EventPriority.HIGH;
            case "HIGHEST":
                return EventPriority.HIGHEST;
            default:
                return defaultPriority;
        }
    }

    @RequiredArgsConstructor @Getter
    private static final class ListenerData {

        private final Class<? extends Event> eventClass;
        private final EventPriority priority;
        private final boolean ignoreCancelled;
        private final EventExecutor executor;

        @Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof ListenerData)) {
                return false;
            }

            final ListenerData other = (ListenerData) object;
            return this.eventClass.isAssignableFrom(other.eventClass)
                    && this.priority == other.priority
                    && this.ignoreCancelled == other.ignoreCancelled
                    && this.executor == other.executor;
        }

        public boolean equals(final Class<? extends Event> eventClass,
                              final EventPriority priority,
                              boolean ignoreCancelled,
                              final EventExecutor executor) {
            return this.eventClass.isAssignableFrom(eventClass)
                    && this.priority == priority
                    && this.ignoreCancelled == ignoreCancelled
                    && this.executor == executor;
        }

        public int hashCode() {
            int result = 17;
            result = (result << 5) + Objects.hashCode(this.eventClass);
            result = (result << 5) * (this.priority != null ? priority.ordinal() : 0);
            result = (result << 5) + (this.ignoreCancelled ? 1 : 0);
            result = (result << 5) + Objects.hashCode(this.executor);
            return result;
        }
    }
}
