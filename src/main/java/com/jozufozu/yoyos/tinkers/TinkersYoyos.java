package com.jozufozu.yoyos.tinkers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.common.ModConfig;
import com.jozufozu.yoyos.tinkers.materials.AxleMaterialStats;
import com.jozufozu.yoyos.tinkers.materials.BodyMaterialStats;
import com.jozufozu.yoyos.tinkers.materials.CordMaterialStats;
import com.jozufozu.yoyos.tinkers.materials.YoyoMaterialTypes;
import com.jozufozu.yoyos.tinkers.modifiers.*;
import com.jozufozu.yoyos.tinkers.traits.TraitSticky;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.events.MaterialEvent;
import slimeknights.tconstruct.library.materials.IMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.Pattern;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.tools.ToolPart;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.tools.TinkerMaterials;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.*;

public class TinkersYoyos
{
    public static List<Item> modItems = Lists.newArrayList();
    
    static List<ToolPart> toolParts = Lists.newArrayList();
    private static List<Pair<Item, ToolPart>> toolPartPatterns = Lists.newArrayList();
    
    public static final HashMap<String, Set<IMaterialStats>> MASTER_STATS = new HashMap<>();
    
    public static Item BOOK;
    
    public static ToolPart YOYO_AXLE;
    public static ToolPart YOYO_BODY;
    public static ToolPart YOYO_CORD;
    
    public static ToolCore YOYO;
    
    public static Modifier EXTENSION;
    public static Modifier FLOATING;
    public static Modifier LUBRICATED;
    public static Modifier GARDENING;
    public static Modifier GLUEY;
    public static Modifier COLLECTING;
    
    public static AbstractTrait STICKY = new TraitSticky();
    
    public static TinkersProxy proxy;
    
    @SubscribeEvent
    public static void onRegistryRegister(RegistryEvent.Register<Item> event)
    {
        Material.UNKNOWN.addStats(new BodyMaterialStats(1F, 2F, 400));
        Material.UNKNOWN.addStats(new AxleMaterialStats(0.5F, 1F));
        Material.UNKNOWN.addStats(new CordMaterialStats(0.2F, 5F));
        
        if (ModConfig.tinkersMaterials.loadTinkersMaterials)
            registerMaterialStats();
        if (ModConfig.tinkersMaterials.loadPlusTiCMaterials)
            Compatibility.registerMaterialStats();
        if (ModConfig.tinkersMaterials.loadCustomMaterials)
            ConfigMaterials.load();
        
        processAdditionalMaterials();
        ConfigMaterials.save(MASTER_STATS);
    
        Iterator<Map.Entry<String, Set<IMaterialStats>>> iterator = MASTER_STATS.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<String, Set<IMaterialStats>> entry = iterator.next();
            Material material = TinkerRegistry.getMaterial(entry.getKey());
            
            if (material == Material.UNKNOWN)
                continue;
    
            for (IMaterialStats stat : entry.getValue())
                TinkerRegistry.addMaterialStats(material, stat);
            
            iterator.remove();
        }
        
        TinkerMaterials.slime.addTrait(STICKY, YoyoMaterialTypes.BODY);
        TinkerMaterials.blueslime.addTrait(STICKY, YoyoMaterialTypes.BODY);
        TinkerMaterials.magmaslime.addTrait(STICKY, YoyoMaterialTypes.BODY);
        TinkerMaterials.knightslime.addTrait(STICKY, YoyoMaterialTypes.BODY);
        
        registerToolParts();
        registerTools();
        
        BOOK = registerItem(new ItemYoyoBook(), "book");
        
        for (Pair<Item, ToolPart> toolPartPattern : toolPartPatterns)
        {
            registerStencil(toolPartPattern.getLeft(), toolPartPattern.getRight());
        }
    
        EXTENSION = new ModExtension();
        EXTENSION.addItem("string");
        EXTENSION.addItem(new ItemStack(Blocks.WOOL), 1, 4);
        EXTENSION.addItem(Yoyos.CORD, 1, 8);
    
        FLOATING = new ModFloating(3);
        FLOATING.addItem(new ItemStack(Items.FISH, 1, ItemFishFood.FishType.PUFFERFISH.getMetadata()), 1, 1);
    
        LUBRICATED = new ModLubricated(5);
        LUBRICATED.addItem(new ItemStack(Items.DYE, 1, 0), 1, 1);
    
        GARDENING = new ModGardening();
        GARDENING.addItem(Items.SHEARS);
        
        COLLECTING = new ModCollecting();
        COLLECTING.addRecipeMatch(RecipeMatch.of(Lists.newArrayList(new ItemStack(Blocks.HOPPER), new ItemStack(Blocks.CHEST))));
        
        GLUEY = new ModGluey();
        GLUEY.addItem("slimeball", 2, 1);
        
        for (Item item : TinkersYoyos.modItems)
        {
            event.getRegistry().register(item);
        }
    }
    
    @SubscribeEvent
    public static void addMaterialStats(MaterialEvent.MaterialRegisterEvent event)
    {
        Set<IMaterialStats> stats = MASTER_STATS.get(event.material.identifier);
        
        if (stats == null) return;
        
        for (IMaterialStats stat : stats)
            TinkerRegistry.addMaterialStats(event.material, stat);
    }
    
    public static void preInit(FMLPreInitializationEvent event)
    {
        if (Yoyos.proxy.runningOnClient())
            proxy = new TinkersClientProxy();
        else
            proxy = new TinkersProxy();
    
        MinecraftForge.EVENT_BUS.register(TinkersYoyos.class);
        MinecraftForge.EVENT_BUS.register(proxy);
    }
    
    public static void init(FMLInitializationEvent event)
    {
        registerToolBuilding();
    
        proxy.init(event);
    }
    
    public static void postInit(FMLPostInitializationEvent event)
    {
        Compatibility.STATS.clear(); //Free up some space
    
        for (Material material : TinkerRegistry.getAllMaterials())
            for (ITrait trait : material.getAllTraitsForStats(YoyoMaterialTypes.HEAD))
                material.addTrait(trait, YoyoMaterialTypes.BODY);
    }
    
    private static void registerToolParts()
    {
        YOYO_AXLE = registerToolPart(new ToolPart(Material.VALUE_Ingot * 2), "yoyo_axle");
        YOYO_BODY = registerToolPart(new ToolPart(Material.VALUE_Ingot * 4), "yoyo_body");
        YOYO_CORD = registerToolPart(new ToolPart(Material.VALUE_Ingot * 3), "yoyo_cord");
    }
    
    private static void registerTools()
    {
        YOYO = registerItem(new YoyoCore(), "yoyo");
    }
    
    private static void registerToolBuilding()
    {
        TinkerRegistry.registerToolForgeCrafting(YOYO);
    }
    
    private static void registerMaterialStats()
    {
        /*Metals*/
        addMaterialStats(TinkerMaterials.bronze, new BodyMaterialStats(3.5F, 2F, 450), new AxleMaterialStats(0.5F, 1F));
        addMaterialStats(TinkerMaterials.copper, new BodyMaterialStats(3.0F, 1.9F, 210), new AxleMaterialStats(0.4F, 1F));
        addMaterialStats(TinkerMaterials.electrum, new BodyMaterialStats(3.0F, .3F, 50), new AxleMaterialStats(0.0F, 0.8F));
        addMaterialStats(TinkerMaterials.lead, new BodyMaterialStats(3.5F, 6F, 350), new AxleMaterialStats(1.6F, 0.7F));
        addMaterialStats(TinkerMaterials.silver, new BodyMaterialStats(5.0F, 1.7F, 250), new AxleMaterialStats(0.3F, 0.95F));
        
        addMaterialStats(TinkerMaterials.iron, new BodyMaterialStats(4.0F, 3.5F, 204), new AxleMaterialStats(0.9F, 0.85F));
        addMaterialStats(TinkerMaterials.pigiron, new BodyMaterialStats(4.5F, 3.7F, 380), new AxleMaterialStats(1.2F, 1.2F));

        /*Nether*/
        addMaterialStats(TinkerMaterials.ardite, new BodyMaterialStats(3.6F, 3.2F, 990), new AxleMaterialStats(0.1F, 1.4F));
        addMaterialStats(TinkerMaterials.cobalt, new BodyMaterialStats(4.4F, 0.3F, 530), new AxleMaterialStats(0.4F, 2.3F));
        addMaterialStats(TinkerMaterials.manyullyn, new BodyMaterialStats(8.7F, 4.0F, 820), new AxleMaterialStats(0.1F, 1.2F));
        addMaterialStats(TinkerMaterials.firewood, new BodyMaterialStats(5.0F, 2.7F, 550), new AxleMaterialStats(3.8F, 0.95F));
        addMaterialStats(TinkerMaterials.netherrack, new BodyMaterialStats(3.0F, 3.4F, 230), new AxleMaterialStats(3.6F, 0.8F));
        addMaterialStats(TinkerMaterials.blaze, new AxleMaterialStats(0.2F, 0.8F));

        /*Naturals*/
        addMaterialStats(TinkerMaterials.cactus, new AxleMaterialStats(2.5F, 0.6F), new BodyMaterialStats(3.4F, 0.8F, 210));
        addMaterialStats(TinkerMaterials.flint, new AxleMaterialStats(0.0F, 0.2F), new BodyMaterialStats(2.9F, 0.2F, 150));
        addMaterialStats(TinkerMaterials.prismarine, new AxleMaterialStats(0.7F, 0.9F), new BodyMaterialStats(6.0F, 1.3F, 430));
        addMaterialStats(TinkerMaterials.endstone, new AxleMaterialStats(0.5F, 0.85F), new BodyMaterialStats(2.9F, 0.4F, 420));
        addMaterialStats(TinkerMaterials.sponge, new AxleMaterialStats(4F, 1.2F), new BodyMaterialStats(0.0F, 0.3F, 1050));
        addMaterialStats(TinkerMaterials.obsidian, new AxleMaterialStats(0.1F, 0.9F), new BodyMaterialStats(4.2F, 1.2F, 120));
        addMaterialStats(TinkerMaterials.stone, new AxleMaterialStats(2.0F, 0.5F), new BodyMaterialStats(3.0F, 2.0F, 120));
        addMaterialStats(TinkerMaterials.wood, new AxleMaterialStats(3.1F, 1.0F), new BodyMaterialStats(2.0F, 1.1F, 35));

        /*Misc*/
        addMaterialStats(TinkerMaterials.paper, new BodyMaterialStats(1.5F, 0.1F, 5), new AxleMaterialStats(1.2F, 0.2F));
        addMaterialStats(TinkerMaterials.string, new CordMaterialStats(0.05F, 6F));
        addMaterialStats(TinkerMaterials.bone, new BodyMaterialStats(2.5F, .3F, 200), new AxleMaterialStats(0.05F, 0.9F));
        addMaterialStats(TinkerMaterials.ice, new BodyMaterialStats(4.5F, 0.9F, 20), new AxleMaterialStats(0.0F, 0.1F));
        addMaterialStats(TinkerMaterials.vine, new CordMaterialStats(1.2F, 8F));


        /*Slime*/
        addMaterialStats(TinkerMaterials.blueslime, new BodyMaterialStats(1.2F, 0.6F, 800), new AxleMaterialStats(2F, 0.3F), new CordMaterialStats(1.0F, 8F));
        addMaterialStats(TinkerMaterials.magmaslime, new BodyMaterialStats(3.3F, 0.6F, 450), new AxleMaterialStats(1.3F, 0.3F), new CordMaterialStats(1.0F, 8F));
        addMaterialStats(TinkerMaterials.knightslime, new BodyMaterialStats(6.1F, 0.6F, 600), new AxleMaterialStats(2F, 0.3F), new CordMaterialStats(2.0F, 16F));
        addMaterialStats(TinkerMaterials.slime, new BodyMaterialStats(1.2F, 0.6F, 1000), new AxleMaterialStats(2F, 0.3F), new CordMaterialStats(1.0F, 8F));

        addMaterialStats(TinkerMaterials.slimevine_blue, new CordMaterialStats(1.5F, 10F));
        
        addMaterialStats(TinkerMaterials.slimevine_purple, new CordMaterialStats(1.9F, 14F));
    }
    
    public static void addMaterialStats(Material material, IMaterialStats stats, IMaterialStats... stats2)
    {
        String identifier = material.identifier;
        Set<IMaterialStats> statsSet = MASTER_STATS.getOrDefault(identifier, Sets.newHashSet());
        
        statsSet.add(stats);
        Collections.addAll(statsSet, stats2);
        
        if (!MASTER_STATS.containsKey(identifier))
            MASTER_STATS.put(identifier, statsSet);
    }
    
    private static void processAdditionalMaterials()
    {
        MASTER_STATS.putAll(Compatibility.STATS);
        
        ConfigMaterials.STATS.forEach(((name, stats) ->
        {
            if (MASTER_STATS.containsKey(name))
            {
                Set<IMaterialStats> statsSet = MASTER_STATS.get(name);
    
                Iterator<IMaterialStats> iterator = statsSet.iterator();
                while (iterator.hasNext())
                {
                    IMaterialStats materialStats = iterator.next();
                    for (IMaterialStats stat : stats)
                        if (materialStats.getClass().equals(stat.getClass()))
                            iterator.remove();
                }
                
                statsSet.addAll(stats);
            }
            else
                MASTER_STATS.put(name, stats);
        }));
    }
    
    private static ToolPart registerToolPart(ToolPart part, String name)
    {
        ToolPart ret = registerItem(part, name);
    
        toolPartPatterns.add(Pair.of(TinkerTools.pattern, ret));
    
        toolParts.add(ret);
    
        return ret;
    }
    
    private static void registerStencil(Item pattern, ToolPart toolPart)
    {
        for (ToolCore toolCore : TinkerRegistry.getTools())
        {
            for (PartMaterialType partMaterialType : toolCore.getRequiredComponents())
            {
                if (partMaterialType.getPossibleParts().contains(toolPart))
                {
                    ItemStack stencil = new ItemStack(pattern);
                    Pattern.setTagForPart(stencil, toolPart);
                    TinkerRegistry.registerStencilTableCrafting(stencil);
                    return;
                }
            }
        }
    }
    
    private static <T extends Item> T registerItem(T item, String name)
    {
        if (!name.equals(name.toLowerCase(Locale.US)))
        {
            throw new IllegalArgumentException(String.format("Unlocalized names need to be all lowercase! Item: %s", name));
        }
        
        item.setUnlocalizedName(String.format("%s.%s", Yoyos.MODID, name));
        item.setRegistryName(new ResourceLocation(Yoyos.MODID, name));
        modItems.add(item);
        return item;
    }
}
