package com.jozufozu.yoyos;

import com.jozufozu.yoyos.core.AllThings;

import net.minecraft.resources.ResourceLocation;

public class Yoyos {
    public static ResourceLocation rl(String name) {
        return new ResourceLocation(Constants.MOD_ID, name);
    }

    public static void init() {
        AllThings.init();
    }
}