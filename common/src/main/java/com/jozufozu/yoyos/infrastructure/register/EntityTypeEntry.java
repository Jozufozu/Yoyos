package com.jozufozu.yoyos.infrastructure.register;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityTypeEntry<T extends Entity> extends Entry<EntityType<T>> {
    public EntityTypeEntry(ResourceLocation loc, Supplier<EntityType<T>> supplier) {
        super(loc, supplier);
    }
}
