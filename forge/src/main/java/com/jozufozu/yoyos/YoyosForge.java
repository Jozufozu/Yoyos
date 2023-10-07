package com.jozufozu.yoyos;

import com.jozufozu.yoyos.core.AllThings;
import com.jozufozu.yoyos.register.datagen.Datagenner;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class YoyosForge {
    
    public YoyosForge() {
    
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
    
        // Use Forge to bootstrap the Common mod.
        Constants.LOG.info("Hello Forge world!");
        Yoyos.init();

        var modEventBus = FMLJavaModLoadingContext.get()
            .getModEventBus();

        modEventBus.addListener(this::register);

        modEventBus.addListener(Datagenner::gatherData);
    }

    public void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            AllThings.REGISTER._registerItems(helper::register);
        });

//        event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
//            AllThings.YOYO_ENTITY_TYPE.doRegister(helper::register);
//        });
    }
}