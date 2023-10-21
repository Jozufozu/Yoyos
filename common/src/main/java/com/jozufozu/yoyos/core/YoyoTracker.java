package com.jozufozu.yoyos.core;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

/**
 * Every {@link LivingEntity} has a YoyoTracker mixin'd.
 */
public class YoyoTracker {
    public static YoyoTracker on(LivingEntity living) {
        return ((LivingEntityExtension) living).yoyos$getYoyoTracker();
    }

    private Yoyo mainHandYoyo = null;
    private Yoyo offhandYoyo = null;

    public boolean hasYoyo(InteractionHand hand) {
        return getAndCheckYoyoInHand(hand) != null;
    }

    @Nullable
    public Yoyo getAndCheckYoyoInHand(InteractionHand hand) {
        var out = getYoyoInHand(hand);

        if (out != null && out.isRemoved()) {
            setYoyoInHand(hand, null);
            return null;
        }

        return out;
    }

    @Nullable
    private Yoyo getYoyoInHand(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> mainHandYoyo;
            case OFF_HAND -> offhandYoyo;
        };
    }

    public void setYoyoInHand(InteractionHand hand, @Nullable Yoyo yoyo) {
        switch (hand) {
            case MAIN_HAND -> mainHandYoyo = yoyo;
            case OFF_HAND -> offhandYoyo = yoyo;
        };
    }

}
