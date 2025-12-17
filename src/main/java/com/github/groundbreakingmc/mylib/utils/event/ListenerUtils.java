package com.github.groundbreakingmc.mylib.utils.event;

import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

@UtilityClass
@SuppressWarnings("unused")
public class ListenerUtils {

    static final Map<Listener, Set<ListenerData>> REGISTERED = new HashMap<>();

    public static void clearAllRegistered() {
        REGISTERED.clear();
    }

    @Nullable
    @ApiStatus.Experimental
    public RegisteredListener register(@NotNull Plugin plugin,
                                       @NotNull Listener listener,
                                       @NotNull Method executeMethod,
                                       @NotNull EventPriority eventPriority,
                                       boolean ignoreCancelled) {
        final Class<?> checkClass;
        if (executeMethod.getParameterCount() != 1
                || !Event.class.isAssignableFrom(checkClass = executeMethod.getParameterTypes()[0])) {
            throw new RuntimeException("Attempted to register an invalid EventHandler method signature \"" + executeMethod.toGenericString() + "\" in " + listener.getClass());
        }

        final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
        executeMethod.setAccessible(true);

        final EventExecutor executor = EventExecutor.create(executeMethod, eventClass);
        final ListenerData listenerData = new ListenerData(eventClass, executeMethod.getName(), eventPriority, ignoreCancelled);
        final Set<ListenerData> listenerDataSet = REGISTERED.get(listener);
        if (listenerDataSet == null) {
            REGISTERED.put(listener, Sets.newHashSet(listenerData));
        } else if (listenerDataSet.isEmpty() || !listenerDataSet.add(listenerData)) {
            return null;
        }

        final RegisteredListener registeredListener = new RegisteredListener(plugin, listener, eventClass, executeMethod, executor, eventPriority, ignoreCancelled);
        getEventListeners(eventClass).register(registeredListener);
        return registeredListener;
    }

    @ApiStatus.Experimental
    public static boolean unregister(@NotNull Listener listener,
                                     @NotNull Class<? extends Event> eventClass,
                                     @NotNull org.bukkit.plugin.RegisteredListener registeredListener,
                                     @NotNull String methodName) {
        final Set<ListenerData> listenerDataSet = ListenerUtils.REGISTERED.get(listener);
        if (listenerDataSet == null || listenerDataSet.isEmpty()) {
            return false;
        }

        getEventListeners(eventClass).unregister(registeredListener);
        final Iterator<ListenerData> iterator = listenerDataSet.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(eventClass, methodName)) {
                iterator.remove();
                break;
            }
        }
        return true;
    }

    @Nullable
    public static EventPriority getEventPriority(final String priority) {
        return getEventPriority(priority, null);
    }

    public static EventPriority getEventPriority(final String priority, final EventPriority defaultPriority) {
        return switch (priority) {
            case "LOWEST" -> EventPriority.LOWEST;
            case "LOW" -> EventPriority.LOW;
            case "NORMAL" -> EventPriority.NORMAL;
            case "HIGH" -> EventPriority.HIGH;
            case "HIGHEST" -> EventPriority.HIGHEST;
            default -> defaultPriority;
        };
    }

    @NotNull
    public HandlerList getEventListeners(@NotNull Class<? extends Event> eventClass) {
        try {
            final Method method = getRegistrationClass(eventClass).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (final Exception ex) {
            throw new IllegalPluginAccessException(ex.toString());
        }
    }

    @NotNull
    public Class<? extends Event> getRegistrationClass(@NotNull Class<? extends Event> eventClass) {
        try {
            eventClass.getDeclaredMethod("getHandlerList");
            return eventClass;
        } catch (final NoSuchMethodException ex) {
            if (eventClass.getSuperclass() != null
                    && !eventClass.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(eventClass.getSuperclass())) {
                return getRegistrationClass(eventClass.getSuperclass().asSubclass(Event.class));
            }

            throw new IllegalPluginAccessException("Unable to find handler list for event " + eventClass.getName() + ". Static getHandlerList method required!");
        }
    }

    record ListenerData(
            Class<? extends Event> eventClass,
            String methodName,
            EventPriority priority,
            boolean ignoreCancelled
    ) {

        @Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            if (!(object instanceof final ListenerData other)) {
                return false;
            }

            return this.eventClass.isAssignableFrom(other.eventClass)
                    && this.methodName.equals(other.methodName)
                    && this.priority == other.priority
                    && this.ignoreCancelled == other.ignoreCancelled;
        }

        public boolean equals(final Class<? extends Event> eventClass,
                              final String methodName) {
            return this.eventClass.isAssignableFrom(eventClass)
                    && this.methodName.equals(methodName);
        }

        public int hashCode() {
            int result = 17;
            result = (result << 5) + Objects.hashCode(this.eventClass);
            result = (result << 5) + Objects.hashCode(this.methodName);
            result = (result << 5) * Objects.hashCode(this.priority);
            result = (result << 5) + Boolean.hashCode(this.ignoreCancelled);
            return result;
        }
    }
}
