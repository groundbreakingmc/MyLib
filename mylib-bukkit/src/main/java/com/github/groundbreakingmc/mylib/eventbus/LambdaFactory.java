package com.github.groundbreakingmc.mylib.eventbus;

import java.lang.invoke.*;
import java.lang.reflect.Method;

/**
 * Creates {@link EventExecutor} instances backed by {@link LambdaMetafactory}
 * to avoid per-dispatch reflection overhead.
 *
 * <p>The generated executor is functionally equivalent to calling the target
 * method directly, but without the cost of {@link Method#invoke(Object, Object...)}.</p>
 */
public final class LambdaFactory {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final MethodType INVOKE_TYPE =
            MethodType.methodType(void.class, Object.class, Event.class);

    private LambdaFactory() {
    }

    /**
     * Creates an {@link EventExecutor} for the given handler method.
     *
     * @param method the handler method; must accept exactly one {@link Event} subtype parameter
     * @return a fast executor backed by an invokedynamic call site
     * @throws RuntimeException if the executor cannot be created
     */
    public static EventExecutor create(final Method method) {
        try {
            method.setAccessible(true);

            final MethodHandle target = LOOKUP.unreflect(method);

            final CallSite site = LambdaMetafactory.metafactory(
                    LOOKUP,
                    "execute",
                    MethodType.methodType(EventExecutor.class),
                    INVOKE_TYPE,
                    target,
                    target.type()
            );

            return (EventExecutor) site.getTarget().invokeExact();

        } catch (Throwable t) {
            throw new RuntimeException("Failed to create EventExecutor for method: " + method, t);
        }
    }
}
