package com.github.groundbreakingmc.mylib.config.annotations;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("com.github.groundbreakingmc.mylib.config.annotations.Value")
public final class Validator extends AbstractProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (final Element element : roundEnv.getElementsAnnotatedWith(Value.class)) {
            if (element.getKind() == ElementKind.FIELD) {
                final Set<Modifier> modifiers = element.getModifiers();
                if (modifiers.contains(Modifier.FINAL)) {
                    super.processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            "Field annotated with @Value can not be final!",
                            element
                    );
                }
            }
        }

        return true;
    }
}
