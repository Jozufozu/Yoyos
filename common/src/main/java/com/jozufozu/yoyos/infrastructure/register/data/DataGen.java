package com.jozufozu.yoyos.infrastructure.register.data;

import java.util.function.Consumer;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullFunction;
import com.jozufozu.yoyos.infrastructure.register.Register;
import com.jozufozu.yoyos.infrastructure.util.Pair;

public class DataGen<R, T extends R> {
    private Pair<String, String> primaryLang;
    private Register.Promise<T> registerPromise;

    private NotNullFunction<ModelBuilder, ModelBuilder> modelBuilderFunction = NotNullFunction.identity();

    public void setLang(String key, String value) {
        primaryLang = new Pair<>(key, value);
    }

    public void _collectLang(Consumer<Pair<String, String>> out) {
        if (primaryLang != null) {
            out.accept(primaryLang);
        }
    }

    public void inject(Register.Promise<T> registerPromise) {
        this.registerPromise = registerPromise;
    }

    public T get() {
        return registerPromise.get();
    }

    public void model(NotNullFunction<ModelBuilder, ModelBuilder> mutator) {
        modelBuilderFunction = modelBuilderFunction.andThen(mutator);
    }

    public ModelBuilder applyModelFunction(ModelBuilder builder) {
        return modelBuilderFunction.apply(builder);
    }
}
