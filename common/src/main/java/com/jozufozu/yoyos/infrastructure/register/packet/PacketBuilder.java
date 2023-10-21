package com.jozufozu.yoyos.infrastructure.register.packet;

import java.util.Objects;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullBiConsumer;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullConsumer;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullFunction;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;
import com.jozufozu.yoyos.infrastructure.register.PacketRegistrationCallback;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class PacketBuilder<T> {

    private final ResourceLocation name;
    private final PacketRegistrationCallback<T> callback;
    private final NotNullFunction<FriendlyByteBuf, T> decoder;
    private final Class<T> clazz;

    private NotNullBiConsumer<T, FriendlyByteBuf> encoder = NotNullBiConsumer.noop();
    private NotNullConsumer<PacketBehavior<T>> onRegister = NotNullConsumer.noop();
    private NotNullSupplier<NotNullConsumer<T>> clientHandler = NotNullConsumer::noop;
    private NotNullSupplier<NotNullConsumer<T>> serverHandler = NotNullConsumer::noop;

    public PacketBuilder(PacketRegistrationCallback<T> callback, ResourceLocation name, Class<T> clazz, NotNullFunction<FriendlyByteBuf, T> decoder) {
        this.callback = callback;
        this.name = name;
        this.clazz = clazz;
        this.decoder = decoder;
    }

    public PacketBuilder<T> encoder(NotNullBiConsumer<T, FriendlyByteBuf> encoder) {
        Objects.requireNonNull(encoder);
        this.encoder = encoder;
        return this;
    }

    public PacketBuilder<T> onClient(NotNullSupplier<NotNullConsumer<T>> clientHandler) {
        Objects.requireNonNull(onRegister);
        this.clientHandler = clientHandler;
        return this;
    }

    public PacketBuilder<T> onServer(NotNullSupplier<NotNullConsumer<T>> serverHandler) {
        Objects.requireNonNull(onRegister);
        this.serverHandler = serverHandler;
        return this;
    }

    public PacketBuilder<T> onRegister(NotNullConsumer<PacketBehavior<T>> onRegister) {
        Objects.requireNonNull(onRegister);
        this.onRegister = this.onRegister.andThen(onRegister);
        return this;
    }

    private PacketBehavior<T> create() {
        return new PacketBehavior<>(clazz, name, decoder, encoder, clientHandler, serverHandler);
    }

    public PacketEntry<T> register() {
        var promise = callback.markForRegistration(name, this::create, onRegister);
        return new PacketEntry<>(promise);
    }

}
