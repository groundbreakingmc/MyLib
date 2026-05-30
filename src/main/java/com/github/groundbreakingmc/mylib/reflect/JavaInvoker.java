package com.github.groundbreakingmc.mylib.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.function.*;

/**
 * Builder facade for creating lambda invocations without try-catch.
 *
 * <p>Prefer {@link HandleLookup}'s one-liner shortcuts for simple cases.
 * Use {@code JavaInvoker} when you need version-conditional fallback signatures
 * ({@link MethodBuilder#orParams}) or want to wrap into a custom
 * {@link FunctionalInterface} ({@link MethodBuilder#as}).
 *
 * <pre>{@code
 * // Simple virtual method:
 * ToIntFunction<Object> getId = JavaInvoker
 *     .virtual(NmsClasses.entity(), "getId", int.class)
 *     .asToIntFunction();
 *
 * // Version-differing signatures (new has extra param):
 * StartRidingFn startRiding = JavaInvoker
 *     .virtual(NmsClasses.entity(), "startRiding", boolean.class,
 *              NmsClasses.entity(), boolean.class, boolean.class)   // primary (new)
 *     .orParams(NmsClasses.entity(), boolean.class)                 // fallback (old)
 *     .as(StartRidingFn.class, "invoke");
 *
 * // Static no-arg int method:
 * IntSupplier nextId = JavaInvoker
 *     .staticMethod(NmsClasses.entity(), "nextEntityId", int.class)
 *     .asIntSupplier();
 * }</pre>
 */
public final class JavaInvoker {

    private JavaInvoker() {
    }

    public static MethodBuilder virtual(Class<?> owner, String name, Class<?> returnType, Class<?>... params) {
        return new MethodBuilder(owner, name, returnType, params, MethodKind.VIRTUAL);
    }

    public static MethodBuilder staticMethod(Class<?> owner, String name, Class<?> returnType, Class<?>... params) {
        return new MethodBuilder(owner, name, returnType, params, MethodKind.STATIC);
    }

    public static MethodBuilder constructor(Class<?> owner, Class<?>... params) {
        return new MethodBuilder(owner, "<init>", owner, params, MethodKind.CONSTRUCTOR);
    }

    public static FieldBuilder field(Class<?> owner, String name, Class<?> type) {
        return new FieldBuilder(owner, name, type, false);
    }

    public static FieldBuilder staticField(Class<?> owner, String name, Class<?> type) {
        return new FieldBuilder(owner, name, type, true);
    }

    // ── Method builder ────────────────────────────────────────────────────────

    public static final class MethodBuilder {

        private final Class<?> owner;
        private final String name;
        private final Class<?> returnType;
        private final Class<?>[] params;
        private final MethodKind kind;
        private Class<?>[] fallbackParams;

        private MethodBuilder(Class<?> owner, String name, Class<?> returnType,
                              Class<?>[] params, MethodKind kind) {
            this.owner = owner;
            this.name = name;
            this.returnType = returnType;
            this.params = params;
            this.kind = kind;
        }

        /**
         * Fallback parameter types used if the primary signature is not found.
         * Extra trailing parameters from the primary signature are silently dropped
         * via {@link MethodHandles#dropArguments}.
         *
         * <pre>{@code
         * // primary (new):  startRiding(Entity, boolean, boolean)
         * // fallback (old): startRiding(Entity, boolean)
         * JavaInvoker.virtual(..., NmsClasses.entity(), boolean.class, boolean.class)
         *            .orParams(NmsClasses.entity(), boolean.class)
         * }</pre>
         */
        public MethodBuilder orParams(Class<?>... fallback) {
            this.fallbackParams = fallback;
            return this;
        }

        // ── Terminal ──────────────────────────────────────────────────────────

        public MethodHandle toHandle() {
            return this.resolve();
        }

        public <R> Supplier<R> asSupplier() {
            return LambdaFactory.supplier(lookupOf(this.owner), this.resolve());
        }

        public IntSupplier asIntSupplier() {
            return LambdaFactory.intSupplier(lookupOf(this.owner), this.resolve());
        }

        public LongSupplier asLongSupplier() {
            return LambdaFactory.longSupplier(lookupOf(this.owner), this.resolve());
        }

        public Runnable asRunnable() {
            return LambdaFactory.runnable(lookupOf(this.owner), this.resolve());
        }

        public <T, R> Function<T, R> asFunction() {
            return LambdaFactory.function(lookupOf(this.owner), this.resolve());
        }

        public <T, U, R> BiFunction<T, U, R> asBiFunction() {
            return LambdaFactory.biFunction(lookupOf(this.owner), this.resolve());
        }

        public <T> Consumer<T> asConsumer() {
            return LambdaFactory.consumer(lookupOf(this.owner), resolve());
        }

        public <T, U> BiConsumer<T, U> asBiConsumer() {
            return LambdaFactory.biConsumer(lookupOf(this.owner), this.resolve());
        }

        public <T> ToIntFunction<T> asToIntFunction() {
            return LambdaFactory.toIntFunction(lookupOf(this.owner), this.resolve());
        }

        public <T> ToLongFunction<T> asToLongFunction() {
            return LambdaFactory.toLongFunction(lookupOf(this.owner), this.resolve());
        }

        public <T> ToDoubleFunction<T> asToDoubleFunction() {
            final MethodHandle handle = this.resolve();
            return LambdaFactory.create(lookupOf(this.owner), handle,
                    ToDoubleFunction.class, "applyAsDouble",
                    MethodType.methodType(double.class, Object.class),
                    MethodType.methodType(double.class, handle.type().parameterType(0)));
        }

        /**
         * Wraps into any custom {@link FunctionalInterface}.
         * Reference types in the SAM signature are erased to {@link Object} automatically.
         *
         * <pre>{@code
         * StartRidingFn fn = JavaInvoker.virtual(...)
         *     .orParams(...)
         *     .as(StartRidingFn.class, "invoke");
         * }</pre>
         */
        public <F> F as(Class<F> iface, String samMethod) {
            final MethodHandle handle = this.resolve();
            final MethodType erased = eraseRefs(handle.type());
            return LambdaFactory.create(lookupOf(this.owner), handle,
                    iface, samMethod,
                    erased,
                    erased);
        }

        // ── Internal ──────────────────────────────────────────────────────────

        private MethodHandle resolve() {
            final HandleLookup lookup = HandleLookup.of(this.owner);
            try {
                return this.findWith(lookup, this.params);
            } catch (RuntimeException primary) {
                if (this.fallbackParams == null) throw primary;
                try {
                    return this.adaptFallback(lookup);
                } catch (RuntimeException fallbackEx) {
                    fallbackEx.addSuppressed(primary);
                    throw fallbackEx;
                }
            }
        }

        private MethodHandle findWith(HandleLookup lookup, Class<?>[] p) {
            return switch (this.kind) {
                case VIRTUAL -> lookup.virtualMethod(this.name, this.returnType, p);
                case STATIC -> lookup.staticMethod(this.name, this.returnType, p);
                case CONSTRUCTOR -> lookup.constructor(p);
            };
        }

        private MethodHandle adaptFallback(HandleLookup lu) {
            MethodHandle adapted = this.findWith(lu, this.fallbackParams);
            for (int i = this.fallbackParams.length; i < this.params.length; i++) {
                adapted = MethodHandles.dropArguments(
                        adapted,
                        adapted.type().parameterCount(),
                        this.params[i]
                );
            }
            return adapted;
        }
    }

    // ── Field builder ─────────────────────────────────────────────────────────

    public static final class FieldBuilder {

        private final Class<?> owner;
        private final String name;
        private final Class<?> type;
        private final boolean isStatic;

        private FieldBuilder(Class<?> owner, String name, Class<?> type, boolean isStatic) {
            this.owner = owner;
            this.name = name;
            this.type = type;
            this.isStatic = isStatic;
        }

        public MethodHandle toHandle() {
            final HandleLookup lookup = HandleLookup.of(this.owner);
            return this.isStatic ? lookup.staticGetter(this.name, this.type) : lookup.getter(this.name, this.type);
        }

        public VarHandle toVarHandle() {
            final HandleLookup lookup = HandleLookup.of(this.owner);
            return this.isStatic ? lookup.staticVarHandle(this.name, this.type) : lookup.varHandle(this.name, this.type);
        }

        public <R> Supplier<R> asSupplier() {
            return LambdaFactory.supplier(lookupOf(this.owner), this.toHandle());
        }

        public <T, R> Function<T, R> asFunction() {
            return LambdaFactory.function(lookupOf(this.owner), this.toHandle());
        }
    }

    // ── Shared utilities ──────────────────────────────────────────────────────

    private static MethodType eraseRefs(MethodType type) {
        MethodType result = type.returnType().isPrimitive()
                ? type
                : type.changeReturnType(Object.class);
        for (int i = 0; i < result.parameterCount(); i++) {
            if (!result.parameterType(i).isPrimitive()) {
                result = result.changeParameterType(i, Object.class);
            }
        }
        return result;
    }

    private static MethodHandles.Lookup lookupOf(Class<?> clazz) {
        try {
            return MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot obtain lookup for: " + clazz.getName(), e);
        }
    }

    private enum MethodKind {VIRTUAL, STATIC, CONSTRUCTOR}
}
