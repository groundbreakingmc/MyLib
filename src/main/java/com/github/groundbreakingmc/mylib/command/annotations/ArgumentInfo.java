package com.github.groundbreakingmc.mylib.command.annotations;

import com.github.groundbreakingmc.mylib.command.allowedexecutor.ExecutorCheck;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ArgumentInfo {
    String argument();

    // boolean useLibTabComplete() default true;

    // Class<? extends ModernArgumentExecutor>[] arguments() default {};

    Class<? extends ExecutorCheck> allowedOnly() default ExecutorCheck.class;
    String executorCheckMessage() default "";

    int nextArgumentOrdinalNumb();
    String argumentMessage() default "";

    String permission() default "";
    String permissionMessage() default "";
}
