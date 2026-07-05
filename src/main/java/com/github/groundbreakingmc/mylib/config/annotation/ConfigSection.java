package com.github.groundbreakingmc.mylib.config.annotation;

import com.github.groundbreakingmc.mylib.config.Config;
import com.github.groundbreakingmc.mylib.config.exception.ConfigMissingPathException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a nested config section.
 *
 * <p>The component type must itself be a class or record whose components are annotated
 * with {@link ConfigField} or {@link ConfigSection}. Mapping is applied recursively.
 *
 * <p>Example:
 * <pre>{@code
 * record CommandsConfig(
 *     @ConfigField(value = "ban",  def = "true") boolean ban,
 *     @ConfigField(value = "mute", def = "true") boolean mute
 * ) {}
 *
 * record PunishmentConfig(
 *     @ConfigField(value = "provider",  def = "auto") String provider,
 *     @ConfigSection("commands") CommandsConfig commands,
 *
 *     // optional section — null if "extra" key is absent in YAML
 *     @ConfigSection(value = "extra", required = false) ExtraConfig extra
 * ) {}
 *
 * PunishmentConfig cfg = config.as(PunishmentConfig.class);
 * }</pre>
 *
 * @see ConfigField
 * @see Config#as(Class)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface ConfigSection {

    /**
     * The config path of the section. Required.
     * Dot-separated for nested keys, e.g. {@code "punishment.commands"}.
     */
    String value();

    /**
     * If {@code true} (default), throws {@link ConfigMissingPathException}
     * when the section is absent.
     *
     * <p>If {@code false}, the component receives {@code null} when the section is absent.
     * The component type should be treated as nullable at the call site.
     */
    boolean required() default true;
}
