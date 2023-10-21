package com.jozufozu.yoyos.infrastructure.register;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullConsumer;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;
import com.jozufozu.yoyos.infrastructure.register.packet.PacketBehavior;

import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface PacketRegistrationCallback<T> {
    @NotNull Register.Promise<PacketBehavior<T>> markForRegistration(ResourceLocation name, NotNullSupplier<PacketBehavior<T>> creator, NotNullConsumer<PacketBehavior<T>> onRegister);
}
