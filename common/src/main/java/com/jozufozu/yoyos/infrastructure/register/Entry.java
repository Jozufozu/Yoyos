package com.jozufozu.yoyos.infrastructure.register;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;

public class Entry<R, T extends R> implements NotNullSupplier<T> {
    private final Register.Promise<R, T> promise;

    protected Entry(Register.Promise<R, T> promise) {
        this.promise = promise;
    }

    @Override
    @NotNull
    public T get() {
        return promise.get();
    }
}
