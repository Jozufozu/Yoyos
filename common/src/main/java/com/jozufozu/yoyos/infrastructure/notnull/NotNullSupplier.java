package com.jozufozu.yoyos.infrastructure.notnull;

import java.util.function.Supplier;

@FunctionalInterface
public interface NotNullSupplier<@NotNullType T> extends Supplier<T> {
    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    T get();
}
