package com.github.groundbreakingmc.mylib.reflect;

import java.lang.invoke.*;
import java.lang.reflect.Proxy;
import java.util.function.*;

/**
 * Low-level wrapper around {@link LambdaMetafactory}.
 *
 * <p>Package-private — external code should go through {@link HandleLookup}
 * or {@link JavaInvoker}. All typed factory methods delegate to the single
 * generic {@link #create} entry point.
 *
 * <p>LambdaMetafactory-generated lambdas are JIT-compiled as normal call sites —
 * faster than raw {@code MethodHandle.invoke()} with zero checked exceptions at call time.
 */
final class LambdaFactory {

    private LambdaFactory() {
    }

    // ── Generic core ──────────────────────────────────────────────────────────

    /**
     * Wraps {@code handle} into an instance of {@code iface} using {@link LambdaMetafactory}.
     *
     * @param lookup      lookup with access to {@code handle}'s owner
     * @param handle      the target method handle
     * @param iface       functional interface to implement
     * @param samMethod   name of the single abstract method in {@code iface}
     * @param erased      erased SAM signature (references → Object, primitives stay)
     * @param specialized specialised SAM signature matching {@code handle}'s actual types
     * @return lambda implementing {@code iface}
     */
    @SuppressWarnings("unchecked")
    static <F> F create(
            MethodHandles.Lookup lookup,
            MethodHandle handle,
            Class<F> iface,
            String samMethod,
            MethodType erased,
            MethodType specialized
    ) {
        try {
            MethodHandles.Lookup metaLookup;
            try {
                metaLookup = MethodHandles.privateLookupIn(iface, MethodHandles.lookup());
            } catch (IllegalAccessException e) {
                metaLookup = lookup;
            }

            final CallSite site = LambdaMetafactory.metafactory(
                    metaLookup, samMethod,
                    MethodType.methodType(iface),
                    erased, handle, specialized
            );
            // invokeExact() cannot be used here: F is erased to Object in bytecode,
            // so the JVM sees ()Object at the call site but the handle has ()F,
            // causing WrongMethodTypeException regardless of any asType() wrapping.
            // invoke() performs the implicit type adaptation transparently.
            // This path executes exactly once per lambda at initialisation.
            return (F) site.getTarget().invoke();
        } catch (LambdaConversionException notDirect) {
            return createViaProxy(handle, iface, samMethod, notDirect);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create " + iface.getSimpleName() + " from: " + handle, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <F> F createViaProxy(
            MethodHandle handle, Class<F> iface, String samMethod, Throwable cause
    ) {
        try {
            return (F) Proxy.newProxyInstance(
                    iface.getClassLoader(),
                    new Class<?>[]{ iface },
                    (proxy, method, args) -> {
                        if (method.getName().equals(samMethod)) {
                            return handle.invokeWithArguments(args);
                        }
                        if (method.getName().equals("equals"))   return proxy == args[0];
                        if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
                        if (method.getName().equals("toString")) return iface.getSimpleName()
                                + "@" + Integer.toHexString(System.identityHashCode(proxy));
                        throw new UnsupportedOperationException(method.toString());
                    }
            );
        } catch (Throwable e) {
            e.addSuppressed(cause);
            throw new RuntimeException("Failed to create " + iface.getSimpleName() + " from: " + handle, e);
        }
    }

    // ── Typed shortcuts ───────────────────────────────────────────────────────

    /**
     * () → R
     */
    static <R> Supplier<R> supplier(MethodHandles.Lookup lookup, MethodHandle handle) {
        return create(lookup, handle,
                Supplier.class, "get",
                MethodType.methodType(Object.class),
                handle.type().wrap().changeReturnType(Object.class));
    }

    /**
     * () → int  (no boxing)
     */
    static IntSupplier intSupplier(MethodHandles.Lookup lookup, MethodHandle handle) {
        return create(lookup, handle,
                IntSupplier.class, "getAsInt",
                MethodType.methodType(int.class),
                MethodType.methodType(int.class));
    }

    /**
     * () → long  (no boxing)
     */
    static LongSupplier longSupplier(MethodHandles.Lookup lookup, MethodHandle handle) {
        return create(lookup, handle,
                LongSupplier.class, "getAsLong",
                MethodType.methodType(long.class),
                MethodType.methodType(long.class));
    }

    /**
     * () → void
     */
    static Runnable runnable(MethodHandles.Lookup lookup, MethodHandle handle) {
        return create(lookup, handle,
                Runnable.class, "run",
                MethodType.methodType(void.class),
                MethodType.methodType(void.class));
    }

    /**
     * (T) → R
     */
    static <T, R> Function<T, R> function(MethodHandles.Lookup lookup, MethodHandle handle) {
        final MethodType impl = handle.type();
        return create(lookup, handle,
                Function.class, "apply",
                MethodType.methodType(Object.class, Object.class),
                MethodType.methodType(boxIfPrimitive(impl.returnType()), impl.parameterType(0)));
    }

    /**
     * (T) → int  (no boxing)
     */
    static <T> ToIntFunction<T> toIntFunction(MethodHandles.Lookup lookup, MethodHandle handle) {
        return create(lookup, handle,
                ToIntFunction.class, "applyAsInt",
                MethodType.methodType(int.class, Object.class),
                MethodType.methodType(int.class, handle.type().parameterType(0)));
    }

    /**
     * (T) → long  (no boxing)
     */
    static <T> ToLongFunction<T> toLongFunction(MethodHandles.Lookup lookup, MethodHandle handle) {
        return create(lookup, handle,
                ToLongFunction.class, "applyAsLong",
                MethodType.methodType(long.class, Object.class),
                MethodType.methodType(long.class, handle.type().parameterType(0)));
    }

    /**
     * (T) → void
     */
    static <T> Consumer<T> consumer(MethodHandles.Lookup lookup, MethodHandle handle) {
        return create(lookup, handle,
                Consumer.class, "accept",
                MethodType.methodType(void.class, Object.class),
                MethodType.methodType(void.class, handle.type().parameterType(0)));
    }

    /**
     * (T, U) → void
     */
    static <T, U> BiConsumer<T, U> biConsumer(MethodHandles.Lookup lookup, MethodHandle handle) {
        final MethodType impl = handle.type();
        return create(lookup, handle,
                BiConsumer.class, "accept",
                MethodType.methodType(void.class, Object.class, Object.class),
                MethodType.methodType(void.class, impl.parameterType(0), impl.parameterType(1)));
    }

    /**
     * (T, U) → R
     */
    static <T, U, R> BiFunction<T, U, R> biFunction(MethodHandles.Lookup lookup, MethodHandle handle) {
        final MethodType impl = handle.type();
        return create(lookup, handle,
                BiFunction.class, "apply",
                MethodType.methodType(Object.class, Object.class, Object.class),
                MethodType.methodType(boxIfPrimitive(impl.returnType()), impl.parameterType(0), impl.parameterType(1)));
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private static Class<?> boxIfPrimitive(Class<?> type) {
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        return type;
    }
}
