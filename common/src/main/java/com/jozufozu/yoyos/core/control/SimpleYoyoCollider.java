package com.jozufozu.yoyos.core.control;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.world.entity.Entity;

public class SimpleYoyoCollider {
    private final float damage = 0;

    private final int cooldownDurationTicks = 15;
    private final int cooldownGraceDurationTicks = 3;

    // Number of ticks we have left before we can attack again.
    private int remainingCooldownTicks;

    // Number of ticks remaining before the cooldown kicks in.
    // Allows one attack to span across multiple ticks.
    // Significantly improves the feel of hitting many mobs at once.
    private int remainingCooldownGrace;

    public void tick(Yoyo yoyo, CollisionCollector collisionCollector) {
        if (remainingCooldownTicks > 0) {
            remainingCooldownTicks--;
            return;
        }

        if (!collisionCollector.isEmpty()) {
            for (Entity entity : collisionCollector) {
                entity.hurt(yoyo.damageSources().cactus(), damage);
            }

            // First tick of attack, start the grace period.
            if (remainingCooldownGrace <= 0) {
                remainingCooldownGrace = cooldownGraceDurationTicks;
            }
        }

        if (remainingCooldownGrace > 0) {
            // Count down the grace period.
            remainingCooldownGrace--;
            if (remainingCooldownGrace <= 0) {
                // Out of grace, next tick we're on cooldown.
                remainingCooldownTicks = cooldownDurationTicks;
            }
        }
    }

}
