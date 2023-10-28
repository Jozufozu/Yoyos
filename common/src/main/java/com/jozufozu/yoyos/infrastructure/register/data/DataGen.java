package com.jozufozu.yoyos.infrastructure.register.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullBiConsumer;
import com.jozufozu.yoyos.infrastructure.register.Register;

import net.minecraft.data.DataProvider;

public class DataGen<R, T extends R> {
    private final Map<ProviderType<?>, NotNullBiConsumer<Register.Promise<R, T>, ? extends DataProvider>> providers = new HashMap<>();

    public <D extends DataProvider> void set(ProviderType<? extends D> providerType, NotNullBiConsumer<Register.Promise<R, T>, D> action) {
        Objects.requireNonNull(action);

        providers.put(providerType, action);
    }

    public <D extends DataProvider> void extend(ProviderType<? extends D> providerType, NotNullBiConsumer<Register.Promise<R, T>, D> action) {
        NotNullBiConsumer<Register.Promise<R, T>, D> existing = getAction(providerType);

        if (existing == null) {
            set(providerType, action);
            return;
        }

        set(providerType, existing.andThen(action));
    }

    public Map<ProviderType<?>, NotNullBiConsumer<Register.Promise<R, T>, ? extends DataProvider>> getProviders() {
        return providers;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <D extends DataProvider> NotNullBiConsumer<Register.Promise<R, T>, D> getAction(ProviderType<? extends D> providerType) {
        return (NotNullBiConsumer<Register.Promise<R, T>, D>) providers.get(providerType);
    }
}
