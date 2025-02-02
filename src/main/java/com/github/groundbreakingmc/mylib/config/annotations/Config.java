package com.github.groundbreakingmc.mylib.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Config {
    String fileName();
    double version() default 0d;
    String versionPath() default "config-version";
    String colorizerPath() default "";
}
