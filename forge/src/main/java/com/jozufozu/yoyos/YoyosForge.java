package com.jozufozu.yoyos;

import com.jozufozu.yoyos.core.AllThings;
import com.jozufozu.yoyos.register.datagen.YoyosDatagen;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class YoyosForge {
    
    public YoyosForge() {
        Yoyos.init();

        var modEventBus = FMLJavaModLoadingContext.get()
            .getModEventBus();

        modEventBus.addListener(this::register);

        modEventBus.addListener(YoyosDatagen::gatherData);
    }

    public void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            AllThings.REGISTER._register(ForgeRegistries.Keys.ITEMS, helper::register);
        });

        event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
            AllThings.REGISTER._register(ForgeRegistries.Keys.ENTITY_TYPES, helper::register);
        });
    }
}