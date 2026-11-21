package com.github.groundbreakingmc.mylib.utils.event;

import com.google.common.collect.Sets;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Set;

@SuppressWarnings("unused") @ApiStatus.Experimental
public class RegisteredListener extends org.bukkit.plugin.RegisteredListener {

    private final Class<? extends Event> eventClass;
    private final Method executeMethod;

    public RegisteredListener(@NotNull Plugin plugin,
                              @NotNull Listener listener,
                              @NotNull Class<? extends Event> eventClass,
                              @NotNull Method executeMethod,
                              @NotNull EventExecutor executor,
                              @NotNull EventPriority priority,
                              boolean ignoreCancelled) {
        super(listener, executor, priority, plugin, ignoreCancelled);

        this.eventClass = eventClass;
        this.executeMethod = executeMethod;
    }

    @ApiStatus.Experimental
    public boolean register() {
        final Class<?> checkClass;
        if (this.executeMethod.getParameterCount() != 1
                || !Event.class.isAssignableFrom(checkClass = this.executeMethod.getParameterTypes()[0])) {
            throw new RuntimeException("Attempted to register an invalid EventHandler method signature \""
                    + this.executeMethod.toGenericString()
                    + "\" in "
                    + super.getListener().getClass()
            );
        }

        final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);

        final ListenerUtils.ListenerData listenerData = new ListenerUtils.ListenerData(eventClass,
                this.executeMethod.getName(),
                super.getPriority(),
                super.isIgnoringCancelled()
        );

        final Set<ListenerUtils.ListenerData> listenerDataSet = ListenerUtils.REGISTERED.get(super.getListener());
        if (listenerDataSet == null) {
            ListenerUtils.REGISTERED.put(super.getListener(), Sets.newHashSet(listenerData));
        } else if (listenerDataSet.isEmpty() || !listenerDataSet.add(listenerData)) {
            return false;
        }

        ListenerUtils.getEventListeners(eventClass).register(this);
        return true;
    }

    @ApiStatus.Experimental
    public boolean unregister() {
        final Set<ListenerUtils.ListenerData> listenerDataSet = ListenerUtils.REGISTERED.get(super.getListener());
        if (listenerDataSet != null
                && !listenerDataSet.isEmpty()
                && listenerDataSet.removeIf(next -> next.equals(this.eventClass, this.executeMethod.getName()))) {
            ListenerUtils.getEventListeners(this.eventClass).unregister(this);
            return true;
        }

        return false;
    }

}
