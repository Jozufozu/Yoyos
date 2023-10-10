package com.jozufozu.yoyos.infrastructure.notnull;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface NotNullSupplier<T> extends Supplier<T> {
    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    @NotNull
    T get();
}
