package com.jozufozu.yoyos.infrastructure.register.data;

import javax.annotation.Nonnull;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullBiFunction;
import com.jozufozu.yoyos.infrastructure.register.Register;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public interface ProviderType<T extends DataProvider> {
    ProviderType<RegisterLangProvider> LANG = register("lang", RegisterLangProvider::new);
    ProviderType<RegisterItemModelProvider> ITEM_MODEL = register("item_model", RegisterItemModelProvider::new);

    T create(Register parent, PackOutput generator);

    @Nonnull
    static <T extends DataProvider> ProviderType<T> register(String name, NotNullBiFunction<Register, PackOutput, T> factory) {
        var out = new ProviderType<T>() {
            @Override
            public T create(Register parent, PackOutput generator) {
                return factory.apply(parent, generator);
            }

            @Override
            public String toString() {
                return "ProviderType: " + name;
            }
        };
        RegisterDataProvider.TYPES.put(name, out);
        return out;
    }
}
