package com.jozufozu.yoyos.infrastructure.register.packet;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullBiConsumer;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullConsumer;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullFunction;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PacketBehavior<T>(
    Class<T> clazz,
    ResourceLocation name,
    NotNullFunction<FriendlyByteBuf, T> reconstruct,
    NotNullBiConsumer<T, FriendlyByteBuf> write,
    NotNullSupplier<NotNullConsumer<T>> clientHandler,
    NotNullSupplier<NotNullConsumer<T>> serverHandler
) {

    public T reconstruct(FriendlyByteBuf buf) {
        return reconstruct.apply(buf);
    }

    public void handleClient(T msg) {
        clientHandler.get()
            .accept(msg);
    }

    public void handleServer(T msg) {
        serverHandler.get()
            .accept(msg);
    }

    public void write(T msg, FriendlyByteBuf buf) {
        write.accept(msg, buf);
    }
}
