package com.github.groundbreakingmc.mylib.eventbus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fast, priority-ordered synchronous event bus.
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>Register listeners via {@link #register(Object)} or lambda handlers via {@link #on}.</li>
 *   <li>Call {@link #bake()} once all listeners are registered to sort and compile handlers.</li>
 *   <li>Post events via {@link #post(Event)}.</li>
 * </ol>
 *
 * <p>Handlers may be registered at any time — even after {@link #bake()} has been called.
 * Late registrations are inserted into the already-compiled array in priority order.</p>
 *
 * <h2>Annotation-based registration</h2>
 * <pre>{@code
 * public class MyListener {
 *
 *     @Subscribe(priority = Priorities.HIGH)
 *     public void onMyEvent(final MyEvent event) {
 *         // handle event
 *     }
 * }
 *
 * bus.register(new MyListener());
 * bus.bake();
 * bus.post(new MyEvent());
 * }</pre>
 *
 * <h2>Lambda registration</h2>
 * <pre>{@code
 * ListenerRegistration reg = bus.on(MyEvent.class, event -> { ... });
 * // later:
 * reg.cancel();
 * }</pre>
 */
public final class EventBus {

    private final Map<Class<?>, List<Handler>> raw = new HashMap<>();
    private final Map<Class<?>, Handler[]> baked = new HashMap<>();

    private boolean bakedFlag = false;

    /**
     * Scans the given listener object for methods annotated with {@link Subscribe}
     * and registers each one as a handler.
     *
     * <p>Each annotated method must accept exactly one parameter that implements
     * {@link Event}, otherwise an {@link IllegalArgumentException} is thrown.</p>
     *
     * @param listener the object whose annotated methods should be registered
     * @throws IllegalArgumentException if a {@link Subscribe}-annotated method has
     *                                  an invalid signature
     * @throws RuntimeException         if an {@link EventExecutor} cannot be created
     *                                  for a method
     */
    public void register(final Object listener) {
        for (final Method method : listener.getClass().getDeclaredMethods()) {
            final Subscribe sub = method.getAnnotation(Subscribe.class);
            if (sub == null) continue;

            final Class<?>[] params = method.getParameterTypes();
            if (params.length != 1) {
                throw new IllegalArgumentException(
                        "Method " + method + " must have exactly 1 parameter"
                );
            }

            final Class<?> eventType = params[0];

            if (!Event.class.isAssignableFrom(eventType)) {
                throw new IllegalArgumentException(
                        "Parameter of method " + method +
                                " must extend " + Event.class.getName()
                );
            }

            try {
                final EventExecutor executor = LambdaFactory.create(method);
                final Handler handler = new Handler(
                        listener,
                        executor,
                        eventType,
                        sub.priority(),
                        sub.ignoreCancelled()
                );

                this.addHandler(eventType, handler);

            } catch (Throwable t) {
                throw new RuntimeException(
                        "Failed to create handler for method " + method, t
                );
            }
        }
    }

    /**
     * Registers a pre-built {@link EventExecutor} for the given event type.
     *
     * @param eventType       the event class to subscribe to
     * @param executor        the executor to invoke on dispatch
     * @param listener        the listener instance passed to the executor
     * @param priority        dispatch priority; higher values run first
     * @param ignoreCancelled whether to skip invocation when the event is cancelled
     * @param <T>             the event type
     */
    public <T extends Event> void register(
            final Class<T> eventType,
            final EventExecutor executor,
            final Object listener,
            final int priority,
            final boolean ignoreCancelled
    ) {
        this.addHandler(eventType, new Handler(listener, executor, eventType, priority, ignoreCancelled));
    }

    /**
     * Removes all handlers belonging to the given listener object.
     *
     * <p>Handlers registered via {@link #on} are not affected, since they carry
     * no listener reference. Use the {@link ListenerRegistration} returned by {@link #on}
     * to cancel those.</p>
     *
     * @param listener the listener whose handlers should be removed
     */
    public void unregister(final Object listener) {
        if (this.bakedFlag) {
            for (final Map.Entry<Class<?>, Handler[]> entry : this.baked.entrySet()) {
                final Handler[] old = entry.getValue();
                int count = 0;

                for (final Handler handler : old) {
                    if (handler.listener() != listener) count++;
                }

                if (count == old.length) continue;

                final Handler[] result = new Handler[count];
                int i = 0;

                for (final Handler handler : old) {
                    if (handler.listener() != listener) result[i++] = handler;
                }

                this.baked.put(entry.getKey(), result);
            }
        } else {
            for (final List<Handler> list : this.raw.values()) {
                list.removeIf(h -> h.listener() == listener);
            }
        }
    }

    /**
     * Registers a lambda handler for the given event type with
     * {@link Priorities#NORMAL} priority and {@code ignoreCancelled = true}.
     *
     * @param eventType the event class to subscribe to
     * @param executor  the lambda to invoke on dispatch
     * @param <T>       the event type
     * @return a {@link ListenerRegistration} that can be used to remove this handler
     */
    public <T extends Event> ListenerRegistration on(
            final Class<T> eventType,
            final EventExecutor executor
    ) {
        return this.on(eventType, executor, Priorities.NORMAL, true);
    }

    /**
     * Registers a lambda handler for the given event type with the specified priority
     * and {@code ignoreCancelled = true}.
     *
     * @param eventType the event class to subscribe to
     * @param executor  the lambda to invoke on dispatch
     * @param priority  dispatch priority; higher values run first
     * @param <T>       the event type
     * @return a {@link ListenerRegistration} that can be used to remove this handler
     */
    public <T extends Event> ListenerRegistration on(
            Class<T> eventType,
            EventExecutor executor,
            int priority
    ) {
        return this.on(eventType, executor, priority, true);
    }

    /**
     * Registers a lambda handler for the given event type with the specified priority
     * and cancellation behaviour.
     *
     * @param eventType       the event class to subscribe to
     * @param executor        the lambda to invoke on dispatch
     * @param priority        dispatch priority; higher values run first
     * @param ignoreCancelled whether to skip invocation when the event is cancelled
     * @param <T>             the event type
     * @return a {@link ListenerRegistration} that can be used to remove this handler
     */
    public <T extends Event> ListenerRegistration on(
            Class<T> eventType,
            EventExecutor executor,
            int priority,
            boolean ignoreCancelled
    ) {
        final Handler handler = new Handler(null, executor, eventType, priority, ignoreCancelled);
        this.addHandler(eventType, handler);
        return () -> this.removeHandler(eventType, handler);
    }

    /**
     * Sorts and compiles all pending handlers into arrays for fast dispatch.
     *
     * <p>Must be called before {@link #post(Event)} for optimal performance.
     * Calling this method more than once has no effect.</p>
     */
    public void bake() {
        if (this.bakedFlag) return;

        for (final Map.Entry<Class<?>, List<Handler>> entry : this.raw.entrySet()) {
            final List<Handler> list = entry.getValue();
            list.sort((a, b) -> Integer.compare(b.priority(), a.priority()));
            this.baked.put(entry.getKey(), list.toArray(new Handler[0]));
        }

        this.raw.clear();
        this.bakedFlag = true;
    }

    /**
     * Posts the given event to all matching handlers in priority order.
     *
     * <p>If the event implements {@link Cancellable} and has been cancelled,
     * handlers with {@link Subscribe#ignoreCancelled()} set to {@code true}
     * will be skipped.</p>
     *
     * @param event the event to dispatch
     */
    public void post(Event event) {
        final Handler[] handlers = this.baked.get(event.getClass());
        if (handlers == null) return;

        final boolean cancellable = event instanceof Cancellable;

        for (final Handler handler : handlers) {
            if (cancellable && ((Cancellable) event).isCancelled() && handler.ignoreCancelled()) {
                continue;
            }

            handler.invoke(event);
        }
    }

    private void addHandler(Class<?> type, Handler handler) {
        if (this.bakedFlag) {
            final Handler[] old = this.baked.get(type);

            if (old == null || old.length == 0) {
                this.baked.put(type, new Handler[]{handler});
                return;
            }

            final int len = old.length;
            final Handler[] result = new Handler[len + 1];
            int i = 0;

            while (i < len && old[i].priority() >= handler.priority()) {
                result[i] = old[i];
                i++;
            }

            result[i] = handler;

            if (i < len) {
                System.arraycopy(old, i, result, i + 1, len - i);
            }

            this.baked.put(type, result);
            return;
        }

        this.raw.computeIfAbsent(type, k -> new ArrayList<>()).add(handler);
    }

    private void removeHandler(Class<?> type, Handler handler) {
        if (this.bakedFlag) {
            final Handler[] old = this.baked.get(type);
            if (old == null) return;

            int count = 0;
            for (final Handler h : old) {
                if (h != handler) count++;
            }

            if (count == old.length) return;

            final Handler[] result = new Handler[count];
            int i = 0;

            for (final Handler h : old) {
                if (h != handler) result[i++] = h;
            }

            this.baked.put(type, result);
        } else {
            final List<Handler> list = this.raw.get(type);
            if (list != null) list.remove(handler);
        }
    }
}
