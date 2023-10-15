package com.jozufozu.yoyos.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

@Mixin(EntityRenderers.class)
public interface EntityRenderersInvoker {
    @Invoker("register")
    static <T extends Entity> void register(EntityType<? extends T> entityType, EntityRendererProvider<T> rendererProvider) {
    }
}
