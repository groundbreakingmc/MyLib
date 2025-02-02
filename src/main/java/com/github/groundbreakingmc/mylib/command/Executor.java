package com.github.groundbreakingmc.mylib.command;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public final class Executor {
    public final Object executor;
    public final Method executeMethod;
}
