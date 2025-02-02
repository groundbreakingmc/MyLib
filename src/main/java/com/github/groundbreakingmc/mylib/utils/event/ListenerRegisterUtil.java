package com.github.groundbreakingmc.mylib.utils.event;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public final class ListenerRegisterUtil {

    private static final Map<Listener, ListenerData> REGISTERED = new HashMap<>();

    public static void register(final Plugin plugin,
                                final Listener listener,
                                final Class<? extends Event> eventClass,
                                final EventPriority eventPriority,
                                final boolean ignoreCancelled,
                                final EventExecutor eventExecutor) {
        final ListenerData listenerData = REGISTERED.get(listener);
        if (listenerData != null && listenerData.equals(eventClass, eventPriority, eventExecutor)) {
            throw new UnsupportedOperationException("Cannot register registered listener!");
        }

        plugin.getServer().getPluginManager().registerEvent(
                eventClass,
                listener,
                eventPriority,
                eventExecutor,
                plugin,
                ignoreCancelled
        );
    }

    public static void unregister(final Listener listener) {
        if (!REGISTERED.containsKey(listener)) {
            throw new UnsupportedOperationException("Cannot unregister not registered listener!");
        }

        HandlerList.unregisterAll(listener);
    }

    public EventPriority getEventPriority(final String priority) {
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
                throw new UnsupportedOperationException("Invalid event priority\"" + priority + "\"!");
        }
    }

    @RequiredArgsConstructor
    private final class ListenerData {
        public final Class<? extends Event> left;
        public final EventPriority middle;
        public final EventExecutor right;

        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof ListenerData)) {
                return false;
            }

            final ListenerData other = (ListenerData) object;
            return Objects.equals(this.left, other.left)
                    && Objects.equals(this.middle, other.middle)
                    && Objects.equals(this.right, other.right);
        }

        public boolean equals(final Object objectLeft,
                              final Object middleRight,
                              final Object objectRight) {
            return Objects.equals(this.left, objectLeft)
                    && Objects.equals(this.middle, middleRight)
                    && Objects.equals(this.right, objectRight);
        }
    }
}
