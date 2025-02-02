package com.github.groundbreakingmc.mylib.utils.java;

import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

@UtilityClass
public final class ReflectionUtils {

    public static <T extends Annotation> T getAnnotation(final AnnotatedElement element, final Class<T> annotation, final String message) {
        if (!element.isAnnotationPresent(annotation)) {
            throw new UnsupportedOperationException(message);
        }

        return element.getAnnotation(annotation);
    }
}
