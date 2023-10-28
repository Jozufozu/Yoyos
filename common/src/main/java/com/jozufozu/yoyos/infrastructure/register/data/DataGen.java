package com.jozufozu.yoyos.infrastructure.register.data;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullBiConsumer;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullFunction;
import com.jozufozu.yoyos.infrastructure.register.Register;
import com.jozufozu.yoyos.infrastructure.register.data.providers.ProviderType;

import net.minecraft.data.DataProvider;

public class DataGen<R, T extends R> {
    private final Map<ProviderType<?>, NotNullBiConsumer<Register.Promise<R, T>, ? extends DataProvider>> providers = new HashMap<>();

    private NotNullFunction<ModelBuilder, ModelBuilder> modelBuilderFunction = NotNullFunction.identity();


    public <D extends DataProvider> void setData(ProviderType<? extends D> providerType, NotNullBiConsumer<Register.Promise<R, T>, D> action) {
        providers.put(providerType, action);
    }

    public Map<ProviderType<?>, NotNullBiConsumer<Register.Promise<R, T>, ? extends DataProvider>> getProviders() {
        return providers;
    }

    public void model(NotNullFunction<ModelBuilder, ModelBuilder> mutator) {
        modelBuilderFunction = modelBuilderFunction.andThen(mutator);
    }

    public ModelBuilder applyModelFunction(ModelBuilder builder) {
        return modelBuilderFunction.apply(builder);
    }
}
