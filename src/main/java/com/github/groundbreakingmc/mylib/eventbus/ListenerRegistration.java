package com.github.groundbreakingmc.mylib.eventbus;

/**
 * A handle to a lambda handler registered via one of the {@code on(...)} methods
 * in {@link EventBus}.
 *
 * <p>Holds a reference to the underlying {@link Handler} so it can be
 * removed from the bus on demand.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * ListenerRegistration reg = bus.on(MyEvent.class, event -> { ... });
 * // later:
 * reg.cancel();
 * }</pre>
 */
public interface ListenerRegistration {

    /**
     * Removes this handler from the {@link EventBus} it was registered with.
     *
     * <p>Calling this method more than once has no effect.</p>
     */
    void cancel();
}
