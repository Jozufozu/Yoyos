package com.jozufozu.yoyos.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;

public class YoyoRenderer<T extends Yoyo> extends EntityRenderer<T> {
    public YoyoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(T yoyo, float $$1, float pt, PoseStack poseStack, MultiBufferSource multiBufferSource, int light) {
        var controller = yoyo.getController();
        if (controller == null) {
            // Bad things, just skip render
            return;
        }

        super.render(yoyo, $$1, pt, poseStack, multiBufferSource, light);

        var blockRenderer = Minecraft.getInstance()
            .getBlockRenderer();

        poseStack.pushPose();

        // Center on model
        poseStack.translate(0, yoyo.getBbHeight() / 2, 0);

        // Yaw away from tail pos
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(pt, yoyo.yRotO, yoyo.getYRot())));

        // Spin!
        poseStack.mulPose(Axis.XP.rotationDegrees(-20 * ((float) (yoyo.tickCount % 360) + pt)));

        // Uncenter
        poseStack.translate(-yoyo.getBbWidth() / 2, -yoyo.getBbHeight() / 2, -yoyo.getBbWidth() / 2);

        poseStack.scale(0.25f, 0.25f, 0.25f);
        blockRenderer.renderSingleBlock(Blocks.DIAMOND_BLOCK.defaultBlockState(), poseStack, multiBufferSource, light, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(T var1) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
