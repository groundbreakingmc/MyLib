package com.github.groundbreakingmc.mylib.eventbus;

/**
 * Predefined priority constants for use with {@link Subscribe#priority()}
 * and the {@code on(...)} methods in {@link EventBus}.
 *
 * <p>Handlers with a higher priority value are invoked first.
 * Use {@link #MONITOR} only for observing the final state of an event —
 * never modify or cancel it at this priority.</p>
 *
 * <pre>
 * Dispatch order (first → last):
 *   MONITOR > HIGHEST > HIGH > NORMAL > LOW > LOWEST
 * </pre>
 */
public final class Priorities {

    public static final int LOWEST = -100;
    public static final int LOW = -50;
    public static final int NORMAL = 0;
    public static final int HIGH = 50;
    public static final int HIGHEST = 100;

    /**
     * Reserved for read-only observation of the event's final state.
     * Do not cancel or modify events at this priority.
     */
    public static final int MONITOR = 1_000_000;

    private Priorities() {
    }
}
