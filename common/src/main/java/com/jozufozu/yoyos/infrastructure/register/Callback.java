package com.jozufozu.yoyos.infrastructure.register;

import java.util.function.Supplier;

import com.jozufozu.yoyos.infrastructure.register.data.DataGen;

import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface Callback<R, T extends R> {
    Register.Future<T> markForRegistration(ResourceLocation loc, Supplier<T> creator, DataGen<R, T> dataGen);
}
