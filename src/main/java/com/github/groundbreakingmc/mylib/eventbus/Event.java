package com.github.groundbreakingmc.mylib.eventbus;

/**
 * Marker interface for all events posted through the {@link EventBus}.
 *
 * <p>Every class that represents an event must implement this interface.
 * If the event should support cancellation, it should also implement
 * {@link Cancellable}.</p>
 */
public interface Event {
}
