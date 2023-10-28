package com.jozufozu.yoyos.infrastructure.notnull;

import java.lang.annotation.*;

import org.jetbrains.annotations.NotNull;

/**
 * An alternative to {@link NotNull} which works on type parameters (J8 feature).
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotNull
public @interface NotNullType {
}