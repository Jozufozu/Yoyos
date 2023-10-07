package com.jozufozu.yoyos.infrastructure.register;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;

public class Entry<T> implements Supplier<T> {
    private final ResourceLocation loc;
    private final Supplier<T> supplier;

    protected Entry(ResourceLocation loc, Supplier<T> supplier) {
        this.loc = loc;
        this.supplier = supplier;
    }

    @Override
    public T get() {
        return supplier.get();
    }
}
