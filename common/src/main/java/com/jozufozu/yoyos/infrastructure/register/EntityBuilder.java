package com.jozufozu.yoyos.infrastructure.register;

import java.util.Objects;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullFunction;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;
import com.jozufozu.yoyos.mixin.EntityRenderersInvoker;
import com.jozufozu.yoyos.platform.Services;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class EntityBuilder<T extends Entity> extends AbstractBuilder<EntityType<?>, EntityType<T>, EntityBuilder<T>> {
    private final EntityType.EntityFactory<T> factory;
    private final MobCategory category;

    private NotNullFunction<EntityType.Builder<T>, EntityType.Builder<T>> buildSteps = NotNullFunction.identity();

    private NotNullSupplier<EntityRendererProvider<? super T>> renderer;

    public EntityBuilder(RegistrationCallback<EntityType<?>, EntityType<T>> registrationCallback, ResourceLocation name, EntityType.EntityFactory<T> factory, MobCategory category) {
        super(name, registrationCallback, Registries.ENTITY_TYPE);
        this.factory = factory;
        this.category = category;
    }

    public EntityBuilder<T> properties(NotNullFunction<EntityType.Builder<T>, EntityType.Builder<T>> mutator) {
        buildSteps = buildSteps.andThen(mutator);
        return this;
    }

    public EntityBuilder<T> renderer(NotNullSupplier<EntityRendererProvider<? super T>> supplier) {
        Objects.requireNonNull(supplier);

        if (renderer == null) {
            onRegister(entityType -> Services.PLATFORM_HELPER.runOnClient(() -> () -> {
                EntityRenderersInvoker.register(entityType, this.renderer.get());
            }));
        }

        renderer = supplier;

        return this;
    }

    @Override
    public EntityEntry<T> register() {
        return (EntityEntry<T>) super.register();
    }

    @Override
    protected EntityEntry<T> wrap(Register.Promise<EntityType<T>> promise) {
        return new EntityEntry<>(promise);
    }

    @Override
    protected EntityType<T> create() {
        return buildSteps.apply(EntityType.Builder.of(factory, category)).build(name.getPath());
    }
}
