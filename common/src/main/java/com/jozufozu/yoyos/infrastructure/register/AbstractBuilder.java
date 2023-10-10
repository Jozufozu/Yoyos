package com.jozufozu.yoyos.infrastructure.register;

import java.util.Objects;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullConsumer;
import com.jozufozu.yoyos.infrastructure.register.data.DataGen;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractBuilder<R, T extends R, Self extends AbstractBuilder<R, T, Self>> {
    protected final ResourceLocation name;
    protected final RegistrationCallback<R, T> registrationCallback;
    protected final DataGen<R, T> dataGen = new DataGen<>();
    private final ResourceKey<? extends Registry<R>> resourceKey;

    protected NotNullConsumer<T> onRegister = $ -> {};

    protected AbstractBuilder(ResourceLocation name, RegistrationCallback<R, T> registrationCallback, ResourceKey<? extends Registry<R>> resourceKey) {
        this.name = name;
        this.registrationCallback = registrationCallback;
        this.resourceKey = resourceKey;
    }

    public Entry<T> register() {
        return wrap(registrationCallback.markForRegistration(resourceKey, name, this::create, dataGen, onRegister));
    }

    protected abstract Entry<T> wrap(Register.Promise<T> promise);

    protected abstract T create();

    public Self onRegister(NotNullConsumer<T> onRegister) {
        Objects.requireNonNull(onRegister);
        this.onRegister = this.onRegister.andThen(onRegister);
        return self();
    }

    @SuppressWarnings("unchecked")
    public Self self() {
        return (Self) this;
    }
}
