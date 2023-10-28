package com.jozufozu.yoyos.infrastructure.register.data.providers;

import javax.annotation.Nonnull;

import com.jozufozu.yoyos.infrastructure.register.Register;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public interface ProviderType<T extends DataProvider> {
    ProviderType<RegisterLangProvider> LANG = register("lang", RegisterLangProvider::new);

    T create(Register parent, PackOutput generator);

    @Nonnull
    static <T extends DataProvider> ProviderType<T> register(String name, ProviderType<T> type) {
        RegisterDataProvider.TYPES.put(name, type);
        return type;
    }
}
