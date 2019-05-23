/*
 * Copyright (c) 2018 Jozsef Augusztiny
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.jozufozu.yoyos.tinkers;

import com.google.common.collect.Lists;
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
    public static Modifier FARMING;
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

        ConfigMaterials.load();
        
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

        FARMING = new ModFarming();
        FARMING.addItem(Items.DIAMOND_HOE);
        
        COLLECTING = new ModCollecting();
        COLLECTING.addRecipeMatch(new RecipeMatch.ItemCombination(1, new ItemStack(Blocks.HOPPER), new ItemStack(Blocks.CHEST)));
        
        GLUEY = new ModGluey();
        GLUEY.addItem("slimeball", 2, 1);
        
        for (Item item : TinkersYoyos.modItems)
            event.getRegistry().register(item);
    }
    
    @SubscribeEvent
    public static void addMaterialStats(MaterialEvent.MaterialRegisterEvent event)
    {
        Set<IMaterialStats> stats = MASTER_STATS.get(event.material.identifier);
        
        if (stats == null) return;
        
        for (IMaterialStats stat : stats)
            TinkerRegistry.addMaterialStats(event.material, stat);
        
        MASTER_STATS.remove(event.material.identifier);
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
        MASTER_STATS.clear();
        
        for (Material material : TinkerRegistry.getAllMaterials())
        {
            for (ITrait trait : material.getAllTraitsForStats(YoyoMaterialTypes.HEAD))
                material.addTrait(trait, YoyoMaterialTypes.BODY);
    
            for (ITrait trait : material.getAllTraitsForStats(YoyoMaterialTypes.HANDLE))
                material.addTrait(trait, YoyoMaterialTypes.AXLE);
        }
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
        if (ModConfig.stationCrafting)
            TinkerRegistry.registerToolStationCrafting(YOYO);

        TinkerRegistry.registerToolForgeCrafting(YOYO);
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
