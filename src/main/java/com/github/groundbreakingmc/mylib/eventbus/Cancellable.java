package com.github.groundbreakingmc.mylib.eventbus;

/**
 * Marks an {@link Event} as cancellable.
 *
 * <p>When an event implements this interface, handlers with
 * {@link Subscribe#ignoreCancelled()} set to {@code true} will be skipped
 * once the event has been cancelled.</p>
 */
public interface Cancellable {

    /**
     * Returns whether this event has been cancelled.
     *
     * @return {@code true} if the event is cancelled
     */
    boolean isCancelled();

    /**
     * Sets the cancelled state of this event.
     *
     * @param cancelled {@code true} to cancel the event
     */
    void setCancelled(boolean cancelled);
}
