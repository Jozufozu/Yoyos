package com.jozufozu.yoyos;

import com.jozufozu.yoyos.common.*;
import com.jozufozu.yoyos.tinkers.TinkersYoyos;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.io.File;
import java.util.Arrays;

@Mod(name = Yoyos.NAME, modid = Yoyos.MODID, version = Yoyos.VERSION, dependencies = "after:tconstruct;after:plustic", acceptedMinecraftVersions = "[1.12, 1.13)")
public class Yoyos
{
    @Mod.Instance(value = Yoyos.MODID)
    public static Yoyos INSTANCE;

    public static final String MODID = "yoyos";
    public static final String NAME = "Yoyos";
    public static final String VERSION = "@VERSION@";

    @SidedProxy(clientSide = "com.jozufozu.yoyos.client.ClientProxy", serverSide = "com.jozufozu.yoyos.common.CommonProxy")
    public static CommonProxy proxy;

    public static File CONFIG_DIR;

    public static Item CORD;
    public static Item CREATIVE_YOYO;
    public static Item WOODEN_YOYO;
    public static Item STONE_YOYO;
    public static Item IRON_YOYO;
    public static Item DIAMOND_YOYO;
    public static Item GOLD_YOYO;
    public static Item SHEAR_YOYO;
    public static Item STICKY_YOYO;

    public static Enchantment COLLECTING;
    public static EnumEnchantmentType YOYO_ENCHANTMENT_TYPE = EnumHelper.addEnchantmentType("collecting", item -> item instanceof IYoyo);

    public static SoundEvent YOYO_THROW;

    public Yoyos()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(CORD = new Item().setCreativeTab(CreativeTabs.MATERIALS).setRegistryName(MODID, "cord").setUnlocalizedName("yoyos.cord"));

        if (!ModConfig.vanillaYoyos.enable) return;

        registry.register(CREATIVE_YOYO = new ItemYoyo("creative_yoyo", Item.ToolMaterial.GOLD).setMaxDamage(0));
        registry.register(WOODEN_YOYO = new ItemYoyo("wooden_yoyo", Item.ToolMaterial.WOOD));
        registry.register(STONE_YOYO = new ItemYoyo("stone_yoyo", Item.ToolMaterial.STONE));
        registry.register(IRON_YOYO = new ItemYoyo("iron_yoyo", Item.ToolMaterial.IRON));
        registry.register(DIAMOND_YOYO = new ItemYoyo("diamond_yoyo", Item.ToolMaterial.DIAMOND));
        registry.register(GOLD_YOYO = new ItemYoyo("gold_yoyo", Item.ToolMaterial.GOLD));
        registry.register(SHEAR_YOYO = new ItemYoyo("shear_yoyo", Item.ToolMaterial.IRON, true));
        registry.register(STICKY_YOYO = new ItemStickyYoyo());
    }

    @SubscribeEvent
    public void registerEnchantment(RegistryEvent.Register<Enchantment> event)
    {
        COLLECTING = new EnchantmentCollecting(Enchantment.Rarity.UNCOMMON, YOYO_ENCHANTMENT_TYPE, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND});

        EnumEnchantmentType[] enchantmentTypes = CreativeTabs.COMBAT.getRelevantEnchantmentTypes();

        int length = enchantmentTypes.length;
        enchantmentTypes = Arrays.copyOf(enchantmentTypes, length + 1);
        enchantmentTypes[length] = YOYO_ENCHANTMENT_TYPE;

        CreativeTabs.COMBAT.setRelevantEnchantmentTypes(enchantmentTypes);

        event.getRegistry().register(COLLECTING);
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event)
    {
        ResourceLocation name = new ResourceLocation(MODID, "entity.yoyo.throw");
        YOYO_THROW = new SoundEvent(name);
        YOYO_THROW.setRegistryName(name);
        event.getRegistry().register(YOYO_THROW);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        CONFIG_DIR = new File(event.getModConfigurationDirectory(), "/" + MODID);

        MinecraftForge.EVENT_BUS.register(proxy);
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
