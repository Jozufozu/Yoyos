package com.jozufozu.yoyos.core;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class YoyoRenderer<T extends Yoyo> extends EntityRenderer<T> {
    public YoyoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(T var1) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
