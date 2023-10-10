package com.jozufozu.yoyos.infrastructure.register.data;

import net.minecraft.resources.ResourceLocation;

public interface ModelBuilder {
    default ResourceLocation mcLoc(String path) {
        return new ResourceLocation("minecraft", path);
    }

    ModelBuilder parentModel(ResourceLocation resourceLocation);
}
