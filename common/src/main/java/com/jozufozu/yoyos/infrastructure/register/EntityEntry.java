package com.jozufozu.yoyos.infrastructure.register;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityEntry<T extends Entity> extends Entry<EntityType<?>, EntityType<T>> {
    public EntityEntry(Register.Promise<EntityType<?>, EntityType<T>> promise) {
        super(promise);
    }
}
