package com.jozufozu.yoyos.infrastructure.register;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullConsumer;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;
import com.jozufozu.yoyos.infrastructure.register.data.DataGen;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface RegistrationCallback<R, T extends R> {
    @NotNull Register.Promise<T> markForRegistration(ResourceKey<? extends Registry<R>> resourceKey, ResourceLocation name, NotNullSupplier<T> creator, DataGen<R, T> dataGen, NotNullConsumer<T> onRegister);
}
