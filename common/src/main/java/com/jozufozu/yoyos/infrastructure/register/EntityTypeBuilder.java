package com.jozufozu.yoyos.infrastructure.register;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class EntityTypeBuilder<T extends Entity> {

    private final ResourceLocation name;
    private final EntityType.EntityFactory<T> factory;
    private final MobCategory category;
    private final Callback<EntityType<?>, EntityType<T>> callback;

    private Function<EntityType.Builder<T>, EntityType.Builder<T>> buildSteps = b -> b;

    public EntityTypeBuilder(Callback<EntityType<?>, EntityType<T>> callback, ResourceLocation name, EntityType.EntityFactory<T> factory, MobCategory category) {
        this.callback = callback;
        this.name = name;
        this.factory = factory;
        this.category = category;
    }

    public EntityTypeBuilder<T> and(Function<EntityType.Builder<T>, EntityType.Builder<T>> mutator) {
        buildSteps = buildSteps.andThen(mutator);
        return this;
    }

    public EntityTypeEntry<T> register() {
        var future = callback.markForRegistration(name, creator(), null);
        return new EntityTypeEntry<>(name, future);
    }

    public Supplier<EntityType<T>> creator() {
        return () -> buildSteps.apply(EntityType.Builder.of(factory, category)).build(name.getPath());
    }
}
