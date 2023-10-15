package com.jozufozu.yoyos;

import com.jozufozu.yoyos.core.AllThings;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

public class YoyosFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Yoyos.init();

        AllThings.REGISTER._register(Registries.ITEM, (rl, item) -> {
            Registry.register(BuiltInRegistries.ITEM, rl, item);
        });

        AllThings.REGISTER._register(Registries.ENTITY_TYPE, (rl, item) -> {
            Registry.register(BuiltInRegistries.ENTITY_TYPE, rl, item);
        });
    }
}
