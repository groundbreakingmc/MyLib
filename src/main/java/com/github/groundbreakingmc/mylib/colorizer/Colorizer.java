package com.github.groundbreakingmc.mylib.colorizer;

import org.jetbrains.annotations.Nullable;

public interface Colorizer<R> {

    R colorize(@Nullable String message);
}
