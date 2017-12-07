package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public abstract class CommonProxy
{
    public void preInit(FMLPreInitializationEvent event)
    {
        EntityRegistry.registerModEntity(new ResourceLocation(Yoyos.MODID, "yoyo"), EntityYoyo.class, "YoYo", 0, Yoyos.INSTANCE, 64, 4, false);
        EntityRegistry.registerModEntity(new ResourceLocation(Yoyos.MODID, "yoyo_sticky"), EntityStickyYoyo.class, "Sticky_YoYo", 1, Yoyos.INSTANCE, 64, 4, true);
    }
    
    public void init(FMLInitializationEvent event)
    {
        YoyoNetwork.initialize();
    }
    
    public static class ServerProxy extends CommonProxy
    {
    
    }
}
