package com.jozufozu.yoyos.infrastructure.register.data.providers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.jozufozu.yoyos.infrastructure.register.Register;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class RegisterDataProvider implements DataProvider {

    static final BiMap<String, ProviderType<?>> TYPES = HashBiMap.create();

    private final Map<ProviderType<?>, DataProvider> subProviders = new LinkedHashMap<>();

    private final CompletableFuture<HolderLookup.Provider> lookupProvider;


    public RegisterDataProvider(Register parent, CompletableFuture<HolderLookup.Provider> lookupProvider, PackOutput generator) {
        this.lookupProvider = lookupProvider;
        for (String id : TYPES.keySet()) {
            ProviderType<?> type = TYPES.get(id);
            DataProvider prov = type.create(parent, generator);
            subProviders.put(type, prov);
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return lookupProvider.thenCompose(provider -> {
            var list = Lists.<CompletableFuture<?>>newArrayList();

            for (var e : subProviders.entrySet()) {
                list.add(e.getValue().run(cache));
            }

            return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Register provider";
    }
}
