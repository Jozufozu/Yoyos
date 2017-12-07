package com.jozufozu.yoyos;

import com.jozufozu.yoyos.common.CommonProxy;
import com.jozufozu.yoyos.common.ItemStickyYoyo;
import com.jozufozu.yoyos.common.ItemYoyo;
import com.jozufozu.yoyos.common.ModConfig;
import com.jozufozu.yoyos.tinkers.TinkersYoyos;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod(name = Yoyos.NAME, modid = Yoyos.MODID, version = Yoyos.VERSION, dependencies = "after:tconstruct", acceptedMinecraftVersions = "[1.12, 1.13)")
public class Yoyos
{
    
    @Mod.Instance(value = Yoyos.MODID)
    public static Yoyos INSTANCE;
    
    public static final String MODID = "yoyos";
    public static final String NAME = "Yoyos";
    public static final String VERSION = "@VERSION@";
    
    @SidedProxy(clientSide = "com.jozufozu.yoyos.client.ClientProxy", serverSide = "com.jozufozu.yoyos.common.CommonProxy$ServerProxy")
    public static CommonProxy proxy;
    
    public static Item CORD;
    
    public static Item WOODEN_YOYO;
    public static Item STONE_YOYO;
    public static Item IRON_YOYO;
    public static Item DIAMOND_YOYO;
    public static Item GOLD_YOYO;
    public static Item SHEAR_YOYO;
    
    public static Item STICKY_YOYO;
    
    public Yoyos()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onRegistryRegister(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(CORD);
        
        if (!ModConfig.vanillaYoyos.enable)
            return;
        
        registry.register(WOODEN_YOYO);
        registry.register(STONE_YOYO);
        registry.register(IRON_YOYO);
        registry.register(DIAMOND_YOYO);
        registry.register(GOLD_YOYO);
        registry.register(SHEAR_YOYO);
        registry.register(STICKY_YOYO);
    }
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(proxy);
        
        CORD = new Item().setCreativeTab(CreativeTabs.MATERIALS).setRegistryName(MODID, "cord").setUnlocalizedName("cord");
        
        WOODEN_YOYO = new ItemYoyo("wooden_yoyo", Item.ToolMaterial.WOOD);
        STONE_YOYO = new ItemYoyo("stone_yoyo", Item.ToolMaterial.STONE);
        IRON_YOYO = new ItemYoyo("iron_yoyo", Item.ToolMaterial.IRON);
        DIAMOND_YOYO = new ItemYoyo("diamond_yoyo", Item.ToolMaterial.DIAMOND);
        GOLD_YOYO = new ItemYoyo("gold_yoyo", Item.ToolMaterial.GOLD);
        
        SHEAR_YOYO = new ItemYoyo("shear_yoyo", Item.ToolMaterial.IRON, true);
        
        STICKY_YOYO = new ItemStickyYoyo();
        
        proxy.preInit(event);
        
        if (Loader.isModLoaded("tconstruct") && ModConfig.tinkersYoyos)
        {
            TinkersYoyos.preInit(event);
        }
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
        
        if (Loader.isModLoaded("tconstruct") && ModConfig.tinkersYoyos)
        {
            TinkersYoyos.init(event);
        }
    }
    
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        if (Loader.isModLoaded("tconstruct") && ModConfig.tinkersYoyos)
        {
            TinkersYoyos.postInit(event);
        }
    }
}
