package com.jozufozu.yoyos;

import com.jozufozu.yoyos.core.AllThings;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class YoyosFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        Yoyos.init();

        AllThings.REGISTER._registerItems((rl, item) -> {
            Registry.register(BuiltInRegistries.ITEM, rl, item);
        });
    }
}
