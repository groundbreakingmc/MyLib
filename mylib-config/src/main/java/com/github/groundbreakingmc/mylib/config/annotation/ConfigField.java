package com.github.groundbreakingmc.mylib.config.annotation;

import com.github.groundbreakingmc.mylib.config.Config;
import com.github.groundbreakingmc.mylib.config.SerializerRegistry;
import com.github.groundbreakingmc.mylib.config.source.ConfigSource;
import com.github.groundbreakingmc.mylib.config.exception.ConfigMissingPathException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Marks a field as a scalar config value to be read from
 * {@link ConfigSource}.
 *
 * <p>Supported component types:
 * <ul>
 *   <li>Primitives and their boxed forms: {@code boolean}, {@code int}, {@code long},
 *       {@code double}, {@code float}</li>
 *   <li>{@link String}</li>
 *   <li>{@link List} — returned as the raw list from the source</li>
 *   <li>Any type registered in {@link SerializerRegistry}</li>
 * </ul>
 *
 * <p>For nested sections (sub-records), use {@link ConfigSection} instead.
 *
 * <p>Example:
 * <pre>{@code
 * record PunishmentConfig(
 *     @ConfigField(value = "provider", def = "auto") String provider,
 *     @ConfigField(value = "enable-commands", def = "true") boolean enableCommands,
 *     @ConfigField(value = "max-duration-days") int maxDurationDays // required
 * ) {}
 *
 * PunishmentConfig cfg = config.as(PunishmentConfig.class);
 * }</pre>
 *
 * @see ConfigSection
 * @see Config#as(Class)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface ConfigField {

    /**
     * The config path to read from. Required.
     * Dot-separated for nested keys, e.g. {@code "commands.ban.enabled"}.
     */
    String value();

    /**
     * String representation of the default value used when the path is absent
     * and {@link #required()} is {@code false}.
     *
     * <p>Empty string ({@code ""}, the default) means "no explicit default":
     * primitives fall back to their Java zero-values ({@code false}, {@code 0}, etc.),
     * and object types fall back to {@code null}.
     */
    String def() default "";

    /**
     * If {@code true}, throws {@link ConfigMissingPathException}
     * when the path is absent, ignoring {@link #def()}.
     *
     * <p>Defaults to {@code false} — missing paths are handled by {@link #def()} or zero-value fallback.
     */
    boolean required() default false;
}
