package com.jozufozu.yoyos.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.jozufozu.yoyos.core.AllThings;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    // TODO: Track thrown status
    // TODO: Don't use redirect?
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack yoyos$thrownYoyoMeansHoldingAirMainHand(LocalPlayer localPlayer) {
        var out = localPlayer.getMainHandItem();
        if (AllThings.DIAMOND_YOYO.matches(out)) {
            return ItemStack.EMPTY;
        }
        return out;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getOffhandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack yoyos$thrownYoyoMeansHoldingAirOffhand(LocalPlayer localPlayer) {
        var out = localPlayer.getOffhandItem();
        if (AllThings.DIAMOND_YOYO.matches(out)) {
            return ItemStack.EMPTY;
        }
        return out;
    }
}
