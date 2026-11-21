package com.github.groundbreakingmc.mylib.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Fluent wrapper around {@link MethodHandles.Lookup} that eliminates boilerplate
 * try-catch blocks when acquiring {@link MethodHandle}s, {@link VarHandle}s,
 * and common functional interfaces.
 *
 * <p>For version-conditional logic or fallback signatures use {@link JavaInvoker},
 * which composes on top of this class.
 *
 * <pre>{@code
 * // Raw handle:
 * MethodHandle handle = HandleLookup.of(NmsClasses.entity())
 *     .staticGetter("ENTITY_COUNTER", AtomicInteger.class);
 *
 * // Direct lambda — virtual method:
 * ToIntFunction<Object> getId = HandleLookup.of(NmsClasses.entity())
 *     .asToIntFunction("getId");
 *
 * // Direct lambda — static no-arg int method:
 * IntSupplier nextId = HandleLookup.of(NmsClasses.entity())
 *     .staticMethodAsIntSupplier("nextEntityId");
 * }</pre>
 */
public final class HandleLookup {

    private final Class<?> owner;
    private final MethodHandles.Lookup lookup;

    private HandleLookup(Class<?> owner) {
        this.owner = owner;
        try {
            this.lookup = MethodHandles.privateLookupIn(owner, MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot obtain private lookup for: " + owner.getName(), e);
        }
    }

    public static HandleLookup of(Class<?> clazz) {
        return new HandleLookup(Objects.requireNonNull(clazz, "clazz cannot be null"));
    }

    // ── Raw handles ───────────────────────────────────────────────────────────

    public MethodHandle getter(String field, Class<?> type) {
        try {
            return this.lookup.findGetter(this.owner, field, type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw fieldError("getter", field, type, e);
        }
    }

    public MethodHandle setter(String field, Class<?> type) {
        try {
            return this.lookup.findSetter(this.owner, field, type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw fieldError("setter", field, type, e);
        }
    }

    public MethodHandle staticGetter(String field, Class<?> type) {
        try {
            return this.lookup.findStaticGetter(this.owner, field, type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw fieldError("static getter", field, type, e);
        }
    }

    public MethodHandle staticSetter(String field, Class<?> type) {
        try {
            return this.lookup.findStaticSetter(this.owner, field, type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw fieldError("static setter", field, type, e);
        }
    }

    public MethodHandle virtualMethod(String name, Class<?> returnType, Class<?>... params) {
        try {
            return this.lookup.findVirtual(this.owner, name, MethodType.methodType(returnType, params));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw methodError("virtual", name, returnType, params, e);
        }
    }

    public MethodHandle staticMethod(String name, Class<?> returnType, Class<?>... params) {
        try {
            return this.lookup.findStatic(this.owner, name, MethodType.methodType(returnType, params));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw methodError("static", name, returnType, params, e);
        }
    }

    public MethodHandle constructor(Class<?>... params) {
        try {
            return this.lookup.findConstructor(this.owner, MethodType.methodType(void.class, params));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(
                    "Cannot find constructor(" + classNames(params) + ") in " + this.owner.getName(), e);
        }
    }

    // ── VarHandles ────────────────────────────────────────────────────────────

    public VarHandle varHandle(String field, Class<?> type) {
        try {
            return this.lookup.findVarHandle(this.owner, field, type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw fieldError("var handle", field, type, e);
        }
    }

    public VarHandle staticVarHandle(String field, Class<?> type) {
        try {
            return this.lookup.findStaticVarHandle(this.owner, field, type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw fieldError("static var handle", field, type, e);
        }
    }

    // ── Field → lambda shortcuts ──────────────────────────────────────────────

    /**
     * Static field → {@code Supplier<F>}
     */
    public <F> Supplier<F> staticFieldAsSupplier(String field, Class<F> type) {
        return LambdaFactory.supplier(this.lookup, this.staticGetter(field, type));
    }

    /**
     * Instance field bound to {@code instance} → {@code Supplier<F>}
     */
    public <F> Supplier<F> fieldAsSupplier(Object instance, String field, Class<F> type) {
        return LambdaFactory.supplier(this.lookup, this.getter(field, type).bindTo(instance));
    }

    // ── Virtual method → lambda shortcuts ────────────────────────────────────

    /**
     * (T) → R
     */
    public <T, R> Function<T, R> asFunction(String method, Class<R> returnType, Class<?>... params) {
        return LambdaFactory.function(this.lookup, this.virtualMethod(method, returnType, params));
    }

    /**
     * (T) → int  (no boxing)
     */
    public <T> ToIntFunction<T> asToIntFunction(String method, Class<?>... params) {
        return LambdaFactory.toIntFunction(this.lookup, this.virtualMethod(method, int.class, params));
    }

    /**
     * (T) → long  (no boxing)
     */
    public <T> ToLongFunction<T> asToLongFunction(String method, Class<?>... params) {
        return LambdaFactory.toLongFunction(this.lookup, this.virtualMethod(method, long.class, params));
    }

    /**
     * (T) → void
     */
    public <T> Consumer<T> asConsumer(String method, Class<?>... params) {
        return LambdaFactory.consumer(this.lookup, this.virtualMethod(method, void.class, params));
    }

    /**
     * (T, U) → void
     */
    public <T, U> BiConsumer<T, U> asBiConsumer(String method, Class<?>... params) {
        return LambdaFactory.biConsumer(this.lookup, this.virtualMethod(method, void.class, params));
    }

    /**
     * (T, U) → R
     */
    public <T, U, R> BiFunction<T, U, R> asBiFunction(String method, Class<R> returnType, Class<?>... params) {
        return LambdaFactory.biFunction(this.lookup, this.virtualMethod(method, returnType, params));
    }

    // ── Static method → lambda shortcuts ─────────────────────────────────────

    /**
     * Static () → int  (no boxing) — e.g. {@code Entity.nextEntityId()}
     */
    public IntSupplier staticMethodAsIntSupplier(String method, Class<?>... params) {
        return LambdaFactory.intSupplier(this.lookup, this.staticMethod(method, int.class, params));
    }

    /**
     * Static () → long  (no boxing)
     */
    public LongSupplier staticMethodAsLongSupplier(String method, Class<?>... params) {
        return LambdaFactory.longSupplier(this.lookup, this.staticMethod(method, long.class, params));
    }

    /**
     * Static () → R
     */
    public <R> Supplier<R> staticMethodAsSupplier(String method, Class<R> returnType, Class<?>... params) {
        return LambdaFactory.supplier(this.lookup, this.staticMethod(method, returnType, params));
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Class<?> owner() {
        return this.owner;
    }

    public MethodHandles.Lookup lookup() {
        return this.lookup;
    }

    // ── Error helpers ─────────────────────────────────────────────────────────

    private RuntimeException fieldError(String kind, String name, Class<?> type, Throwable cause) {
        return new RuntimeException(
                "Cannot find " + kind + " '" + name + "' (" + type.getSimpleName() + ") in "
                        + this.owner.getName(), cause);
    }

    private RuntimeException methodError(String kind, String name, Class<?> ret, Class<?>[] params, Throwable cause) {
        return new RuntimeException(
                "Cannot find " + kind + " method '" + name + "(" + classNames(params) + "):"
                        + ret.getSimpleName() + "' in " + this.owner.getName(), cause);
    }

    private static String classNames(Class<?>[] types) {
        return Arrays.stream(types).map(Class::getSimpleName).collect(Collectors.joining(", "));
    }
}
