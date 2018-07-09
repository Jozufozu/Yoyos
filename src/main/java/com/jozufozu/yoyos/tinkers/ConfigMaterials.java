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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOG = LogManager.getLogger("Yoyo Materials");
    
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
    private static JsonObject dumpMissingBodyStats(Material material)
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
    private static JsonObject dumpMissingAxleStats(Material material)
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

    private static void resourceMaterialsFor(BiConsumer<String, JsonObject> action)
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
        catch (IOException | URISyntaxException e)
        {
            LOG.error("Couldn't get a list of all material files", e);
        }
        finally
        {
            IOUtils.closeQuietly(filesystem);
        }
    }

    private static void loadConfigMaterials(File file)
    {
        File[] files = file.listFiles();
        
        if (files == null)
            return;
        
        for (File file1 : files)
            if (file1 != null && file1.getAbsolutePath().endsWith(".json"))
                loadMaterialStatsFromFile(file1);
    }

    private static void loadMaterialStatsFromFile(File file)
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

    private static void processJson(String name, JsonObject materialStats)
    {
        try
        {
            addStats(name, BodyMaterialStats.deserialize(materialStats));
        }
        catch (JsonParseException e)
        {
            LOG.error(String.format("Error parsing body stats for material \"%s\", ", name), e);
        }
    
        try
        {
            addStats(name, AxleMaterialStats.deserialize(materialStats));
        }
        catch (JsonParseException e)
        {
            LOG.error(String.format("Error parsing axle stats for material \"%s\", ", name), e);
        }
    
        try
        {
            addStats(name, CordMaterialStats.deserialize(materialStats));
        }
        catch (JsonParseException e)
        {
            LOG.error(String.format("Error parsing cord stats for material \"%s\", ", name), e);
        }
    }

    private static void addStats(String material, @Nullable IMaterialStats stats)
    {
        if (stats == null)
            return;
        
        Set<IMaterialStats> statsSet = TinkersYoyos.MASTER_STATS.getOrDefault(material, Sets.newHashSet());
    
        statsSet.add(stats);
    
        if (!TinkersYoyos.MASTER_STATS.containsKey(material))
            TinkersYoyos.MASTER_STATS.put(material, statsSet);
    }
}
