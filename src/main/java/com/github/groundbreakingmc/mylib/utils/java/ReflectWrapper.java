package com.github.groundbreakingmc.mylib.utils.java;

import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@ToString
public final class ReflectWrapper<T> {

    private final MethodHandle handle;

    private ReflectWrapper(MethodHandle handle) {
        this.handle = handle;
    }

    @SuppressWarnings("unchecked")
    public T invoke(Object... args) {
        try {
            return (T) this.handle.invokeWithArguments(args);
        } catch (final Throwable th) {
            throw new RuntimeException("Failed to invoke handle", th);
        }
    }

    public static <T> ReflectWrapper<T> forMethod(@NotNull Method method) {
        try {
            method.setAccessible(true);
            final MethodHandle handle = MethodHandles.lookup().unreflect(method);
            return new ReflectWrapper<>(handle);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access method: " + method, e);
        }
    }

    public static <T> ReflectWrapper<T> forFieldGetter(@NotNull Field field, @NotNull VarHandle.AccessMode accessMode) {
        try {
            field.setAccessible(true);
            final MethodHandle handle = MethodHandles.lookup().unreflectVarHandle(field).toMethodHandle(accessMode);
            return new ReflectWrapper<>(handle);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field getter: " + field, e);
        }
    }

    public static <T> ReflectWrapper<T> forConstructor(@NotNull Constructor<T> constructor) {
        try {
            constructor.setAccessible(true);
            final MethodHandle handle = MethodHandles.lookup().unreflectConstructor(constructor);
            return new ReflectWrapper<>(handle);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access constructor: " + constructor, e);
        }
    }

    public static <T> Builder<T> constructorBuilder(@NotNull Class<T> returnType) {
        return new Builder<>(Builder.Types.CONSTRUCTOR);
    }

    public static <T> Builder<T> methodBuilder(@NotNull Class<T> returnType) {
        return new Builder<>(Builder.Types.METHOD);
    }

    public static <T> Builder<T> fieldBuilder(@NotNull Class<T> returnType, @NotNull VarHandle.AccessMode accessMode) {
        return new Builder<>(accessMode);
    }

    public static final class Builder<T> {

        private final Types wrapperType;
        private final VarHandle.AccessMode accessMode;

        private Class<?> targetClass;
        private String name;
        private Class<?>[] parameterTypes = new Class<?>[0];

        private Builder(Types wrapperType) {
            this.wrapperType = wrapperType;
            this.accessMode = null;
        }

        private Builder(VarHandle.AccessMode accessMode) {
            this.wrapperType = Types.FIELD;
            this.accessMode = accessMode;
        }

        public Builder<T> target(@NotNull Class<?> targetClass) {
            this.targetClass = targetClass;
            return this;
        }

        public Builder<T> name(@NotNull String name) {
            this.name = name;
            return this;
        }

        public Builder<T> parameterTypes(@NotNull Class<?>... parameterTypes) {
            if (this.wrapperType == Types.FIELD) {
                throw new IllegalStateException("Can not specify parameter types for field builder!");
            }
            this.parameterTypes = parameterTypes;
            return this;
        }

        public ReflectWrapper<T> build() {
            if (this.targetClass == null) {
                throw new IllegalStateException("Target class must be specified");
            }

            if ((this.wrapperType == Types.METHOD || this.wrapperType == Types.FIELD) && this.name == null) {
                throw new IllegalStateException("Name must be specified for method or field wrapper");
            }

            try {
                final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(this.targetClass, MethodHandles.lookup());
                return switch (this.wrapperType) {
                    case CONSTRUCTOR -> {
                        final Constructor<?> constructor = this.targetClass.getDeclaredConstructor(this.parameterTypes);
                        constructor.setAccessible(true);
                        yield new ReflectWrapper<>(lookup.unreflectConstructor(constructor));
                    }
                    case METHOD -> {
                        final Method method = this.targetClass.getDeclaredMethod(this.name, this.parameterTypes);
                        method.setAccessible(true);
                        yield new ReflectWrapper<>(lookup.unreflect(method));
                    }
                    case FIELD -> {
                        final Field field = this.targetClass.getDeclaredField(this.name);
                        field.setAccessible(true);
                        yield new ReflectWrapper<>(lookup.unreflectVarHandle(field).toMethodHandle(this.accessMode));
                    }
                };
            } catch (Throwable e) {
                throw new RuntimeException("Failed to build ReflectWrapper", e);
            }
        }

        public enum Types {
            CONSTRUCTOR,
            METHOD,
            FIELD;
        }
    }
}
