package com.github.groundbreakingmc.mylib.eventbus;

/**
 * Invokes a specific event handler method on a listener object.
 *
 * <p>Implementations are typically generated at runtime via
 * {@link LambdaFactory} to avoid reflective overhead on each dispatch.</p>
 */
@FunctionalInterface
public interface EventExecutor {

    /**
     * Executes the handler method on the given listener with the given event.
     *
     * @param listener the object that owns the handler method
     * @param event    the event to pass to the handler
     */
    void execute(Object listener, Event event);
}
