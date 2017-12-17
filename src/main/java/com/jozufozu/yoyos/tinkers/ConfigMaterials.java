package com.jozufozu.yoyos.tinkers;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.tinkers.materials.AxleMaterialStats;
import com.jozufozu.yoyos.tinkers.materials.BodyMaterialStats;
import com.jozufozu.yoyos.tinkers.materials.CordMaterialStats;
import net.minecraft.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.library.materials.IMaterialStats;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Set;

public class ConfigMaterials
{
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Logger LOG = LogManager.getLogger("Yoyo Materials");
    
    public static final HashMap<String, Set<IMaterialStats>> STATS = new HashMap<>();
    
    public static void load()
    {
        File materialsLoc = new File(Yoyos.CONFIG_DIR, "/materials");
        materialsLoc.mkdirs();
    
        loadMaterialsInDirectoryRecursive(materialsLoc);
    }
    
    public static void loadMaterialsInDirectoryRecursive(File file)
    {
        File[] files = file.listFiles();
        
        if (files == null)
            return;
        
        for (File file1 : files)
        {
            if (file1 == null)
                continue;
            
            if (file1.isDirectory())
                loadMaterialsInDirectoryRecursive(file1);
            else if (file1.getAbsolutePath().endsWith(".json"))
                loadMaterialStatsFromFile(file1);
        }
    }
    
    public static void loadMaterialStatsFromFile(File file)
    {
        String name = file.getName();
    
        if(name.endsWith(".json"))
            name = name.substring(0, name.indexOf(".json"));
        else
            return;
    
        try
        {
            FileReader reader = new FileReader(file);
            JsonObject materialStats = GSON.fromJson(reader, JsonObject.class);
            reader.close();
            
            try
            {
                addStats(name, readBodyFromMaterial(materialStats));
            }
            catch (JsonParseException e)
            {
                LOG.error(String.format("Error parsing body stats for material \"%s\", ", name), e);
            }
    
            try
            {
                addStats(name, readAxleFromMaterial(materialStats));
            }
            catch (JsonParseException e)
            {
                LOG.error(String.format("Error parsing axle stats for material \"%s\", ", name), e);
            }
    
            try
            {
                addStats(name, readCordFromMaterial(materialStats));
            }
            catch (JsonParseException e)
            {
                LOG.error(String.format("Error parsing cord stats for material \"%s\", ", name), e);
            }
        }
        catch (Exception e)
        {
            LOG.error("Error reading file: " + file.getName());
        }
    }
    
    public static void addStats(String material, @Nullable IMaterialStats stats)
    {
        if (stats == null)
            return;
        
        Set<IMaterialStats> statsSet = STATS.getOrDefault(material, Sets.newHashSet());
    
        statsSet.add(stats);
    
        if (!STATS.containsKey(material))
            STATS.put(material, statsSet);
    }
    
    @Nullable
    public static BodyMaterialStats readBodyFromMaterial(JsonObject material) throws JsonParseException
    {
        if (!JsonUtils.hasField(material, "body"))
            return null;
    
        JsonObject body = JsonUtils.getJsonObject(material, "body");
        
        float attack = JsonUtils.getFloat(body, "attack");
        float weight = JsonUtils.getFloat(body, "weight");
        int durability = JsonUtils.getInt(body, "durability");
        
        return new BodyMaterialStats(attack, weight, durability);
    }
    
    @Nullable
    public static CordMaterialStats readCordFromMaterial(JsonObject material) throws JsonParseException
    {
        if (!JsonUtils.hasField(material, "cord"))
            return null;
        
        JsonObject cord = JsonUtils.getJsonObject(material, "cord");
        
        float friction = JsonUtils.getFloat(cord, "friction");
        float length = JsonUtils.getFloat(cord, "length");
        
        return new CordMaterialStats(friction, length);
    }
    
    @Nullable
    public static AxleMaterialStats readAxleFromMaterial(JsonObject material) throws JsonParseException
    {
        if (!JsonUtils.hasField(material, "axle"))
            return null;
        
        JsonObject axle = JsonUtils.getJsonObject(material, "axle");
        
        float friction = JsonUtils.getFloat(axle, "friction");
        float modifier = JsonUtils.getFloat(axle, "modifier");
        
        return new AxleMaterialStats(friction, modifier);
    }
}
