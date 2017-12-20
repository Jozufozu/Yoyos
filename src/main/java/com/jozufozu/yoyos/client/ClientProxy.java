package com.jozufozu.yoyos.client;

import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.common.CommonProxy;
import com.jozufozu.yoyos.common.EntityStickyYoyo;
import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.common.ModConfig;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event)
    {
        registerModel(Yoyos.CORD);
        
        if (!ModConfig.vanillaYoyos.enable)
            return;
        
        registerModel(Yoyos.WOODEN_YOYO);
        registerModel(Yoyos.STONE_YOYO);
        registerModel(Yoyos.IRON_YOYO);
        registerModel(Yoyos.DIAMOND_YOYO);
        registerModel(Yoyos.GOLD_YOYO);
        registerModel(Yoyos.SHEAR_YOYO);
        registerModel(Yoyos.STICKY_YOYO);
        registerModel(Yoyos.CREATIVE_YOYO);
    }
    
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        
        RenderingRegistry.registerEntityRenderingHandler(EntityYoyo.class, RenderYoYo::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityStickyYoyo.class, RenderYoYo::new);
    }
    
    @Override
    public boolean runningOnClient()
    {
        return true;
    }
    
    public static void registerModel(Item item)
    {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
