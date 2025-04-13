package com.github.groundbreakingmc.mylib.logger.console;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface Logger {

    void info(String msg);

    void info(Supplier<String> msg);

    @Deprecated
    void warn(String msg);

    void warning(String msg);

    void warning(Supplier<String> msg);

}
