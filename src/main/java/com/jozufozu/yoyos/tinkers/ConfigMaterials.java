package com.jozufozu.yoyos.tinkers;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.common.ModConfig;
import com.jozufozu.yoyos.tinkers.materials.AxleMaterialStats;
import com.jozufozu.yoyos.tinkers.materials.BodyMaterialStats;
import com.jozufozu.yoyos.tinkers.materials.CordMaterialStats;
import com.jozufozu.yoyos.tinkers.materials.YoyoMaterialTypes;
import net.minecraft.util.JsonUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.IMaterialStats;
import slimeknights.tconstruct.library.materials.Material;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiConsumer;

public class ConfigMaterials
{
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Logger LOG = LogManager.getLogger("Yoyo Materials");
    
    public static void dumpMissing(File saveLoc)
    {
        saveLoc.mkdirs();
        Collection<Material> materials = TinkerRegistry.getAllMaterials();
    
        for (Material material : materials)
        {
            File materialSave = new File(saveLoc, material.identifier + ".json");

            JsonObject materialJson = new JsonObject();
    
            JsonObject bodyStats = dumpMissingBodyStats(material);
            JsonObject axleStats = dumpMissingAxleStats(material);
            
            if (bodyStats == null && axleStats == null)
                continue;
            
            if (bodyStats != null)
                materialJson.add("body", bodyStats);
    
            if (axleStats != null)
                materialJson.add("axle", axleStats);
    
            try
            {
                try (FileWriter writer = new FileWriter(materialSave))
                {
                    writer.write(GSON.toJson(materialJson));
                }
            }
            catch (Exception e)
            {
                LOG.error(String.format("Error writing material \"%s\" to file: ", material.identifier), e);
            }
        }
    }
    
    @Nullable
    public static JsonObject dumpMissingBodyStats(Material material)
    {
        HeadMaterialStats head = material.getStats(YoyoMaterialTypes.HEAD);
        if (head == null || material.getStats(YoyoMaterialTypes.BODY) != null)
            return null;
        
        JsonObject statJson = new JsonObject();
        statJson.addProperty("attack", head.attack);
        statJson.addProperty("weight", 0);
        statJson.addProperty("durability", head.durability);
        
        return statJson;
    }
    
    @Nullable
    public static JsonObject dumpMissingAxleStats(Material material)
    {
        HandleMaterialStats handle = material.getStats(YoyoMaterialTypes.HANDLE);
        if (handle == null || material.getStats(YoyoMaterialTypes.AXLE) != null)
            return null;
        
        JsonObject statJson = new JsonObject();
        statJson.addProperty("friction", 0);
        statJson.addProperty("modifier", handle.modifier);
        
        return statJson;
    }
    
    public static void load()
    {
        File materialsLoc = new File(Yoyos.CONFIG_DIR, "/materials");
    
        if (ModConfig.configMaterials)
        {
            if (materialsLoc.mkdirs())
                resourceMaterialsFor((name, json) ->
                {
                    File to = new File(materialsLoc, name + ".json");
    
                    if (to.exists())
                        return;
    
                    try (FileWriter writer = new FileWriter(to))
                    {
                        writer.write(GSON.toJson(json));
                    }
                    catch (Exception e)
                    {
                        LOG.error(e);
                    }
                });
    
            loadConfigMaterials(materialsLoc);
        }
        else
        {
            resourceMaterialsFor(ConfigMaterials::processJson);
        }
    }
    
    public static void resourceMaterialsFor(BiConsumer<String, JsonObject> action)
    {
        FileSystem filesystem = null;
    
        try
        {
            URL url = ConfigMaterials.class.getResource("/assets/yoyos/materials");
        
            if (url != null)
            {
                URI uri = url.toURI();
                Path path;
            
                if ("file".equals(uri.getScheme()))
                {
                    path = Paths.get(ConfigMaterials.class.getResource("/assets/yoyos/materials").toURI());
                }
                else
                {
                    if (!"jar".equals(uri.getScheme()))
                    {
                        LOG.error("Unsupported scheme " + uri + " trying to list all materials");
                        return;
                    }
                
                    filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    path = filesystem.getPath("/assets/yoyos/materials");
                }
            
                Iterator<Path> iterator = Files.walk(path).iterator();
            
                while (iterator.hasNext())
                {
                    Path path1 = iterator.next();
                
                    if ("json".equals(FilenameUtils.getExtension(path1.toString())))
                    {
                        String name = FilenameUtils.removeExtension(path1.getFileName().toString());
                        try (BufferedReader bufferedreader = Files.newBufferedReader(path1))
                        {
                            action.accept(name, JsonUtils.fromJson(GSON, bufferedreader, JsonObject.class));
                        }
                    }
                }
            }
        }
        catch (IOException | URISyntaxException urisyntaxexception)
        {
            LOG.error("Couldn't get a list of all material files", urisyntaxexception);
        }
        finally
        {
            IOUtils.closeQuietly(filesystem);
        }
    }
    
    public static void loadConfigMaterials(File file)
    {
        File[] files = file.listFiles();
        
        if (files == null)
            return;
        
        for (File file1 : files)
            if (file1 != null && file1.getAbsolutePath().endsWith(".json"))
                loadMaterialStatsFromFile(file1);
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
            processJson(name, materialStats);
        }
        catch (Exception e)
        {
            LOG.error(String.format("Error reading material \"%s\" from file: ", name), e);
        }
    }
    
    public static void processJson(String name, JsonObject materialStats)
    {
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
    
    public static void addStats(String material, @Nullable IMaterialStats stats)
    {
        if (stats == null)
            return;
        
        Set<IMaterialStats> statsSet = TinkersYoyos.MASTER_STATS.getOrDefault(material, Sets.newHashSet());
    
        statsSet.add(stats);
    
        if (!TinkersYoyos.MASTER_STATS.containsKey(material))
            TinkersYoyos.MASTER_STATS.put(material, statsSet);
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
