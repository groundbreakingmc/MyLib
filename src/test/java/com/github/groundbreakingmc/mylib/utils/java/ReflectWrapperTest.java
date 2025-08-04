package com.github.groundbreakingmc.mylib.utils.java;

import org.junit.jupiter.api.Test;

import java.lang.invoke.VarHandle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReflectWrapperTest {

    @Test
    void constructorBuilder() {
        final ReflectWrapper<TestClass> constructorWrapper = ReflectWrapper
                .<TestClass>constructorBuilder()
                .target(TestClass.class)
                .parameterTypes(String.class)
                .build();

        final String message = "Welcome!";
        final TestClass instance = constructorWrapper.invoke(message);
        assertNotNull(instance);
        assertEquals(message, instance.message);
    }

    @Test
    void methodBuilder() {
        final ReflectWrapper<String> methodWrapper = ReflectWrapper
                .<String>methodBuilder()
                .target(TestClass.class)
                .name("sayHello")
                .parameterTypes(String.class)
                .build();

        final String name = "Steve";
        final TestClass instance = new TestClass("Nice to meet you.");
        final String result = methodWrapper.invoke(instance, name);
        assertEquals(instance.sayHello(name), result);
    }

    @Test
    void fieldBuilder() {
        final ReflectWrapper<Integer> getter = ReflectWrapper
                .<Integer>fieldBuilder(VarHandle.AccessMode.GET)
                .target(TestClass.class)
                .name("counter")
                .build();

        final ReflectWrapper<Void> setter = ReflectWrapper
                .<Void>fieldBuilder(VarHandle.AccessMode.SET)
                .target(TestClass.class)
                .name("counter")
                .build();

        final int value = 42;
        final TestClass instance = new TestClass("test");
        setter.invoke(instance, value);
        final int result = getter.invoke(instance);

        assertEquals(value, result);
        assertEquals(value, instance.getCounter());
    }

    static class TestClass {

        private final String message;
        private int counter;

        private TestClass(String message) {
            this.message = message;
        }

        private String sayHello(String name) {
            return "Hello, " + name + "! " + this.message;
        }

        private int getCounter() {
            return counter;
        }

        private void setCounter(int value) {
            this.counter = value;
        }
    }
}