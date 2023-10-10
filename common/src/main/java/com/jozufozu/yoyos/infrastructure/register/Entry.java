package com.jozufozu.yoyos.infrastructure.register;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;

public class Entry<T> implements NotNullSupplier<T> {
    private final Register.Promise<T> promise;

    protected Entry(Register.Promise<T> promise) {
        this.promise = promise;
    }

    @Override
    @NotNull
    public T get() {
        return promise.get();
    }
}
