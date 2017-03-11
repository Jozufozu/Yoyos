package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public abstract class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        EntityRegistry.registerModEntity(EntityYoyo.class, "YoYo", 0, Yoyos.INSTANCE, 64, 4, true);
    }

    public void init(FMLInitializationEvent event) {
        YoyoNetwork.initialize();
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public boolean isServer() {
        return true;
    }

    public static class ServerProxy extends CommonProxy {

    }
}
