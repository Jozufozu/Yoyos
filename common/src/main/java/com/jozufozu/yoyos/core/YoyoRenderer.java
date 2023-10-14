package com.jozufozu.yoyos.core;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;

public class YoyoRenderer<T extends Yoyo> extends EntityRenderer<T> {
    public YoyoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(T yoyo, float $$1, float $$2, PoseStack poseStack, MultiBufferSource multiBufferSource, int light) {
        super.render(yoyo, $$1, $$2, poseStack, multiBufferSource, light);

        var blockRenderer = Minecraft.getInstance()
            .getBlockRenderer();

        poseStack.pushPose();

        var dimensions = yoyo.getDimensions(null);
        poseStack.translate(-dimensions.width / 2, 0, -dimensions.width / 2);
        poseStack.scale(0.25f, 0.25f, 0.25f);

        blockRenderer.renderSingleBlock(Blocks.DIAMOND_BLOCK.defaultBlockState(), poseStack, multiBufferSource, light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(T var1) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
