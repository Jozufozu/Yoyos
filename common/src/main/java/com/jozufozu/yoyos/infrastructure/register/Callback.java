package com.jozufozu.yoyos.infrastructure.register;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface Callback<T> {
    Supplier<T> markForRegistration(ResourceLocation loc, Supplier<T> creator, DataGenComponent dataGen);
}
