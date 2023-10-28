package com.jozufozu.yoyos.infrastructure.notnull;

import java.lang.annotation.*;

import javax.annotation.meta.TypeQualifierDefault;

import org.jetbrains.annotations.NotNull;

/**
 * This annotation can be applied to a package or class to indicate that the fields in that element are nonnull by default unless there is:
 * <ul>
 * <li>An explicit nullness annotation
 * <li>there is a default parameter annotation applied to a more tightly nested element.
 * </ul>
 */
@Documented
@NotNull
@TypeQualifierDefault(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface FieldsAreNotNullByDefault {
}
