package com.github.groundbreakingmc.mylib.command.annotations;

import com.github.groundbreakingmc.mylib.command.allowedexecutor.ExecutorCheck;
import com.github.groundbreakingmc.mylib.command.executors.ModernArgumentExecutor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CommandInfo {
    String command();
    String[] aliases() default {};

    boolean useLibTabComplete() default true;

    Class<? extends ExecutorCheck> allowedOnly() default ExecutorCheck.class;
    String executorCheckMessage() default "";

    Class<? extends ModernArgumentExecutor>[] arguments() default {};
    String argumentMessage() default "";

    String permission() default "";
    String permissionMessage() default "";

    int minArgs() default 0;
    String minArgsMessage() default "";

    int maxArgs() default Integer.MAX_VALUE;
    String maxArgsMessage() default "";
}
