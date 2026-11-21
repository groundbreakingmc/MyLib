package com.github.groundbreakingmc.mylib.eventbus;

/**
 * Internal representation of a registered event handler.
 *
 * @param listener        the object that owns the handler method, or {@code null} for lambda handlers
 * @param executor        the executor that invokes the handler
 * @param eventType       the event class this handler is subscribed to
 * @param priority        the dispatch priority; higher values run first
 * @param ignoreCancelled whether to skip invocation when the event is cancelled
 */
record Handler(
        Object listener,
        EventExecutor executor,
        Class<?> eventType,
        int priority,
        boolean ignoreCancelled
) {

    /**
     * Invokes this handler with the given event.
     *
     * @param event the event to dispatch
     */
    void invoke(final Event event) {
        this.executor.execute(this.listener, event);
    }
}
