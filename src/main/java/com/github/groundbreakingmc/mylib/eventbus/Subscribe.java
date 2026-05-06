package com.github.groundbreakingmc.mylib.eventbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event handler to be picked up by {@link EventBus#register(Object)}.
 *
 * <p>The annotated method must have exactly one parameter that implements {@link Event}.
 * It will be invoked whenever an event of that type is posted to the bus.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * @Subscribe(priority = Priorities.HIGH)
 * public void onMyEvent(final MyEvent event) {
 *     // handle event
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {

    /**
     * The dispatch priority of this handler. Higher values run first.
     *
     * @return the priority; defaults to {@link Priorities#NORMAL}
     * @see Priorities
     */
    int priority() default Priorities.NORMAL;

    /**
     * Whether this handler should be skipped when the event has been cancelled.
     *
     * @return {@code true} to skip cancelled events; defaults to {@code true}
     * @see Cancellable
     */
    boolean ignoreCancelled() default true;
}
