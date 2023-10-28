package com.jozufozu.yoyos.infrastructure.register.data;

import java.util.Map;
import java.util.TreeMap;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;

public class ItemModelBuilder {
    public final ResourceLocation name;

    @Nullable
    private ResourceLocation parent;
    private final Map<String, String> textures = new TreeMap<>();

    public ItemModelBuilder(ResourceLocation name) {
        this.name = name;
    }

    public static ItemModelBuilder of(ResourceLocation name) {
        return new ItemModelBuilder(name);
    }

    public ItemModelBuilder texture(String key, ResourceLocation texture) {
        return texture(key, texture.toString());
    }

    private ItemModelBuilder texture(String key, String value) {
        textures.put(key, value);
        return this;
    }

    public ItemModelBuilder parent(ResourceLocation loc) {
        parent = loc;
        return this;
    }

    public JsonObject serialize() {
        var out = new JsonObject();

        if (parent != null) {
            out.addProperty("parent", parent.toString());
        }

        if (!this.textures.isEmpty()) {
            JsonObject textures = new JsonObject();
            for (var e : this.textures.entrySet()) {
                textures.addProperty(e.getKey(), serializeLocOrKey(e.getValue()));
            }
            out.add("textures", textures);
        }

        return out;
    }

    private String serializeLocOrKey(String tex) {
        if (tex.charAt(0) == '#') {
            // it's a key
            return tex;
        }
        // it's a loc
        return new ResourceLocation(tex).toString();
    }
}
