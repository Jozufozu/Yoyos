package com.jozufozu.yoyos.tinkers;

import com.google.common.collect.Sets;
import com.jozufozu.yoyos.tinkers.materials.AxleMaterialStats;
import com.jozufozu.yoyos.tinkers.materials.BodyMaterialStats;
import com.jozufozu.yoyos.tinkers.materials.YoyoMaterialTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.events.MaterialEvent;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.IMaterialStats;
import slimeknights.tconstruct.library.materials.Material;

import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class Compatibility
{
    public static final HashMap<String, Set<IMaterialStats>> STATS = new HashMap<>();
    
    public static void registerMaterialStats()
    {
        MinecraftForge.EVENT_BUS.register(Compatibility.class);
        addBodies();
        addAxles();
    }
    
    public static void addBodies()
    {
        //Vanilla
        tryAddStats("invar", new BodyMaterialStats(5.0f, 2.2f, 600));
        tryAddStats("emerald_plustic", new BodyMaterialStats(7.0f, 0.3f, 1222));
        tryAddStats("alumite", new BodyMaterialStats(5.5f, 1.2f, 700));
        tryAddStats("nickel", new BodyMaterialStats(4.5f, 1.6f, 460));
        tryAddStats("iridium", new BodyMaterialStats(5.8f, 1.8f, 520));
    
        //Botania
        tryAddStats("terrasteel", new BodyMaterialStats(6.5f, 1.1f, 1562));
        tryAddStats("elementium", new BodyMaterialStats(6.0f, 0.7f, 540));
        tryAddStats("manasteel", new BodyMaterialStats(6.0f, 0.9f, 540));
        tryAddStats("livingwood_plustic", new BodyMaterialStats(2.8f, 1.5f, 50));
        tryAddStats("mirion", new BodyMaterialStats(9.0f, 0.3f, 1919));
    
        //Screw organization!
        tryAddStats("steel", new BodyMaterialStats(6.0f, 4.4f, 540));
        tryAddStats("osmiridium", new BodyMaterialStats(8.0f, 3.9f, 1300));
        tryAddStats("void_actadd_plustic", new BodyMaterialStats(4.4f, 0.3f, 480));
        tryAddStats("amber", new BodyMaterialStats(5.7f, 2.2f, 730));
        tryAddStats("ruby", new BodyMaterialStats(6.4f, 1.4f, 660));
        tryAddStats("platinum_plustic", new BodyMaterialStats(6.0f, 4.8f, 720));
        tryAddStats("amethyst", new BodyMaterialStats(10.0f, 1.9f, 1200));
        tryAddStats("refinedglowstone", new BodyMaterialStats(10.0f, 0.9f, 450));
        tryAddStats("sapphire", new BodyMaterialStats(6.4f, 1.8f, 700));
        tryAddStats("guardianscale", new BodyMaterialStats(7.0f, 0.8f, 600));
        tryAddStats("restonia_actadd_plustic", new BodyMaterialStats(6.1f, 1.0f, 640));
        tryAddStats("enori_actadd_plustic", new BodyMaterialStats(5.2f, 1.0f, 600));
        tryAddStats("enderium_plustic", new BodyMaterialStats(7.0f, 2.2f, 800));
        tryAddStats("malachite_gem", new BodyMaterialStats(6.1f,  2.1f, 640));
        tryAddStats("topaz", new BodyMaterialStats(6.0f, 2.1f, 690));
        tryAddStats("psigem", new BodyMaterialStats(5.0f, 0.9f, 620));
        tryAddStats("psimetal", new BodyMaterialStats(5.0f, 3.7f, 620));
        tryAddStats("peridot", new BodyMaterialStats(6.1f, 1.3f, 640));
        tryAddStats("signalum_plustic", new BodyMaterialStats(5.2f, 2.8f, 690));
        tryAddStats("refinedobsidian", new BodyMaterialStats(11.0f, 1.5f, 2500));
        tryAddStats("palis_actadd_plustic", new BodyMaterialStats(5.8f, 1.0f, 800));
        tryAddStats("diamatine_actadd_plustic", new BodyMaterialStats(6.3f, 0.8f, 1700));
        tryAddStats("emeradic_actadd_plustic", new BodyMaterialStats(7.7f, 0.8f, 1400));
        tryAddStats("blackquartz_plustic", new BodyMaterialStats(4.5f, 0.9f, 380));
        tryAddStats("certusquartz_plustic", new BodyMaterialStats(4.5f, 0.9f, 250));
        tryAddStats("osgloglas", new BodyMaterialStats(11.0f, 4.3f, 2800));
        tryAddStats("fluixcrystal_plustic", new BodyMaterialStats(6.2f, 0.5f, 700));
        tryAddStats("tanzanite", new BodyMaterialStats(7.0f, 3.3f, 650));
        tryAddStats("lumium_plustic", new BodyMaterialStats(6.5f, 1.0f, 830));
        tryAddStats("osmium", new BodyMaterialStats(5.8f, 2.8f, 500));
        tryAddStats("fusewood_plustic", new BodyMaterialStats(4.0f, 0.6f, 430));
        tryAddStats("ghostwood_plustic", new BodyMaterialStats(2.5f, 0.7f, 300));
        tryAddStats("darkwood_plustic", new BodyMaterialStats(3.0f, 1.1f, 350));
        tryAddStats("redmatter", new BodyMaterialStats(15.0f, 5.0f, 2017));
        tryAddStats("darkmatter", new BodyMaterialStats(10.5f, 2.5f, 1729));
        tryAddStats("mica", new BodyMaterialStats(6.0f, 0.1f, 680));
        tryAddStats("awakened_plustic", new BodyMaterialStats(35.0f, 2.0f, 5000));
        tryAddStats("wyvern_plustic", new BodyMaterialStats(15.0f, 3.5f, 2000));
        tryAddStats("chaotic_plustic", new BodyMaterialStats(48.0f, 0.5f, 10000));
    
    }
    
    public static void addAxles()
    {
        tryAddStats("steel", new AxleMaterialStats(1.4f, 0.9f));
        tryAddStats("osmiridium", new AxleMaterialStats(1.0f, 1.5f));
        tryAddStats("void_actadd_plustic", new AxleMaterialStats(0.01f, 1.0f));
        tryAddStats("amber", new AxleMaterialStats(0.7f, 1.0f));
        tryAddStats("iridium", new AxleMaterialStats(0.9f, 1.1f));
        tryAddStats("ruby", new AxleMaterialStats(0.7f, 1.2f));
        tryAddStats("platinum_plustic", new AxleMaterialStats(1.1f, 1.0f));
        tryAddStats("amethyst", new AxleMaterialStats(0.7f, 1.6f));
        tryAddStats("refinedglowstone", new AxleMaterialStats(0.5f, 0.9f));
        tryAddStats("sapphire", new AxleMaterialStats(0.4f, 1.0f));
        tryAddStats("guardianscale", new AxleMaterialStats(1.4f, 0.9f));
        tryAddStats("restonia_actadd_plustic", new AxleMaterialStats(0.1f, 1.1f));
        tryAddStats("enori_actadd_plustic", new AxleMaterialStats(1.4f, 1.2f));
        tryAddStats("invar", new AxleMaterialStats(1.3f, 1.3f));
        tryAddStats("enderium_plustic", new AxleMaterialStats(0.8f, 1.0f));
        tryAddStats("malachite_gem", new AxleMaterialStats(0.7f, 1.3f));
        tryAddStats("topaz", new AxleMaterialStats(0.6f, 0.8f));
        tryAddStats("peridot", new AxleMaterialStats(0.5f, 1.3f));
        tryAddStats("signalum_plustic", new AxleMaterialStats(1.1f, 1.2f));
        tryAddStats("refinedobsidian", new AxleMaterialStats(0.6f, 1.5f));
        tryAddStats("palis_actadd_plustic", new AxleMaterialStats(0.7f, 1.3f));
        tryAddStats("nickel", new AxleMaterialStats(1.2f, 1.0f));
        tryAddStats("diamatine_actadd_plustic", new AxleMaterialStats(0.7f, 1.2f));
        tryAddStats("emeradic_actadd_plustic", new AxleMaterialStats(0.7f, 1.1f));
        tryAddStats("blackquartz_plustic", new AxleMaterialStats(0.1f, 0.8f));
        tryAddStats("certusquartz_plustic", new AxleMaterialStats(0.1f, 0.8f));
        tryAddStats("osgloglas", new AxleMaterialStats(1.3f, 1.5f));
        tryAddStats("fluixcrystal_plustic", new AxleMaterialStats(0.01f, 1.0f));
        tryAddStats("tanzanite", new AxleMaterialStats(0.7f, 0.7f));
        tryAddStats("alumite", new AxleMaterialStats(0.8f, 1.1f));
        tryAddStats("lumium_plustic", new AxleMaterialStats(0.7f, 1.1f));
        tryAddStats("osmium", new AxleMaterialStats(1.1f, 1.2f));
        tryAddStats("fusewood_plustic", new AxleMaterialStats(1.1f, 1.0f));
        tryAddStats("ghostwood_plustic", new AxleMaterialStats(3.1f, 1.1f));
        tryAddStats("darkwood_plustic", new AxleMaterialStats(3.1f, 1.3f));
        tryAddStats("emerald_plustic", new AxleMaterialStats(0.7f, 1.1f));
        tryAddStats("darkmatter", new AxleMaterialStats(0.0f, 1.7f));
        tryAddStats("redmatter", new AxleMaterialStats(2.1f, 2.0f));
        tryAddStats("mica", new AxleMaterialStats(2.9f, 0.9f));
        tryAddStats("awakened_plustic", new AxleMaterialStats(0.5f, 1.8f));
        tryAddStats("wyvern_plustic", new AxleMaterialStats(1.0f, 1.6f));
        tryAddStats("chaotic_plustic", new AxleMaterialStats(0.0f, 2.3f));
    
    }
    
    @SubscribeEvent
    public static void addMaterialStats(MaterialEvent.MaterialRegisterEvent event)
    {
        Set<IMaterialStats> stats = STATS.get(event.material.identifier);
        
        if (stats == null) return;
    
        for (IMaterialStats stat : stats)
        {
            TinkerRegistry.addMaterialStats(event.material, stat);
        }
    }
    
    public static void tryAddStats(String identifier, IMaterialStats stats, IMaterialStats... stats2)
    {
        Material material = TinkerRegistry.getMaterial(identifier);
        if (material != Material.UNKNOWN)
        {
            TinkerRegistry.addMaterialStats(material, stats, stats2);
            return;
        }
        
        //Sometimes materials don't like to exist
        //This is a way of waiting patiently for them to exist
        //It's super hacky, but it works
        Set<IMaterialStats> statsSet = STATS.getOrDefault(identifier, Sets.newHashSet());
        
        statsSet.add(stats);
        Collections.addAll(statsSet, stats2);
        
        if (!STATS.containsKey(identifier))
            STATS.put(identifier, statsSet);
    }
    
    /** This exists so I don't have to bang my head going through all the dang items **/
    public static void dumpMissingMaterials()
    {
        try
        {
            File desktop = new File("S:\\User\\Desktop\\");
            File b = new File(desktop, "Bodies.java");
            File a = new File(desktop, "Axles.java");
            b.createNewFile();
            a.createNewFile();
            
            FileWriter bodies = new FileWriter(b);
            FileWriter axles = new FileWriter(a);
            
            for (Material material : TinkerRegistry.getAllMaterials().stream().filter(material -> material.getStats(YoyoMaterialTypes.BODY) == null || material.getStats(YoyoMaterialTypes.AXLE) == null).collect(Collectors.toList()))
            {
                HeadMaterialStats headStats = material.getStats(YoyoMaterialTypes.HEAD);
                if (headStats != null)
                {
                    bodies.write("tryAddStats(\"");
                    bodies.write(material.identifier);
                    bodies.write("\", new BodyMaterialStats(");
                    bodies.write(String.format("%.1ff, -1, %s));\n", headStats.attack, headStats.durability));
                }
                
                HandleMaterialStats handleStats = material.getStats(YoyoMaterialTypes.HANDLE);
                if (handleStats != null)
                {
                    axles.write("tryAddStats(\"");
                    axles.write(material.identifier);
                    axles.write("\", new AxleMaterialStats(");
                    axles.write(String.format("1.0f, %.1f));\n", handleStats.modifier));
                }
            }
            bodies.close();
            axles.close();
        }
        catch (Exception e)
        {
        
        }
    }
}
