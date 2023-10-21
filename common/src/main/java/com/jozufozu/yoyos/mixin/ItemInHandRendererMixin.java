package com.jozufozu.yoyos.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.jozufozu.yoyos.core.YoyoTracker;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    // TODO: Track thrown status
    // TODO: Don't use redirect?
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack yoyos$thrownYoyoMeansHoldingAirMainHand(LocalPlayer localPlayer) {
        if (YoyoTracker.on(localPlayer).hasYoyo(InteractionHand.MAIN_HAND)) {
            return ItemStack.EMPTY;
        }
        return localPlayer.getMainHandItem();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getOffhandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack yoyos$thrownYoyoMeansHoldingAirOffhand(LocalPlayer localPlayer) {
        if (YoyoTracker.on(localPlayer).hasYoyo(InteractionHand.OFF_HAND)) {
            return ItemStack.EMPTY;
        }
        return localPlayer.getOffhandItem();
    }
}
