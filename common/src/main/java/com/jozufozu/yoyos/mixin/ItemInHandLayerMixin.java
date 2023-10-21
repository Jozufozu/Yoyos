package com.jozufozu.yoyos.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.yoyos.core.network.YoyoTracker;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void yoyos$thrownYoyoMeansNoRender(LivingEntity living, ItemStack $$1, ItemDisplayContext $$2, HumanoidArm arm, PoseStack $$4, MultiBufferSource $$5, int $$6, CallbackInfo ci) {
        var interactionHand = living.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (YoyoTracker.on(living).hasYoyo(interactionHand)) {
            ci.cancel();
        }
    }
}
