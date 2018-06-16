package org.swaggertools.core.config;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationProperty {
    String name() default "";

    String description() default "";

    boolean required() default false;

    String defaultValue() default "";
}
