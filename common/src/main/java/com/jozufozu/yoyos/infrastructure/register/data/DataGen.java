package com.jozufozu.yoyos.infrastructure.register.data;

import java.util.function.Consumer;
import java.util.function.Function;

import com.jozufozu.yoyos.infrastructure.register.Register;
import com.jozufozu.yoyos.infrastructure.types.Pair;

public class DataGen<R, T extends R> {
    private Pair<String, String> primaryLang;
    private Register.Future<T> registerFuture;

    private Function<ModelBuilder, ModelBuilder> modelBuilderFunction = b -> b;

    public void setLang(String key, String value) {
        primaryLang = new Pair<>(key, value);
    }

    public void _collectLang(Consumer<Pair<String, String>> out) {
        if (primaryLang != null) {
            out.accept(primaryLang);
        }
    }

    public void inject(Register.Future<T> registerFuture) {
        this.registerFuture = registerFuture;
    }

    public T get() {
        return registerFuture.get();
    }

    public void model(Function<ModelBuilder, ModelBuilder> mutator) {
        modelBuilderFunction = modelBuilderFunction.andThen(mutator);
    }

    public ModelBuilder applyModelFunction(ModelBuilder builder) {
        return modelBuilderFunction.apply(builder);
    }
}
