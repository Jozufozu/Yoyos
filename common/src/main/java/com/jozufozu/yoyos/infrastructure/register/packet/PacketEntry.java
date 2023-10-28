package com.jozufozu.yoyos.infrastructure.register.packet;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;
import com.jozufozu.yoyos.infrastructure.register.Register;

public class PacketEntry<T> implements NotNullSupplier<PacketBehavior<T>> {
    private final Register.RegistrylessPromise<PacketBehavior<T>> promise;

    public PacketEntry(Register.RegistrylessPromise<PacketBehavior<T>> promise) {
        this.promise = promise;
    }

    @Override
    public PacketBehavior<T> get() {
        return promise.get();
    }
}
