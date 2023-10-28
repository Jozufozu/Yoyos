package com.jozufozu.yoyos.core.control;

import org.joml.Vector3d;

import com.jozufozu.yoyos.core.Yoyo;
import com.jozufozu.yoyos.infrastructure.util.YoyoUtil;

import net.minecraft.world.entity.Entity;

public class AttackBehavior implements YoyoBehavior {
    private final float damage = 4;

    private final int coolDownDurationTicks = 15;
    private final int coolDownGraceDurationTicks = 3;

    // Number of ticks we have left before we can attack again.
    private int remainingCoolDownTicks;

    // Number of ticks remaining before the cooldown kicks in.
    // Allows one attack to span across multiple ticks.
    // Significantly improves the feel of hitting many mobs at once.
    private int remainingCoolDownGrace;

    private final Vector3d scratch = new Vector3d();

    @Override
    public void tick(Yoyo yoyo) {
        if (remainingCoolDownTicks > 0) {
            remainingCoolDownTicks--;
            return;
        }

        if (remainingCoolDownGrace > 0) {
            // Count down the grace period.
            remainingCoolDownGrace--;
            if (remainingCoolDownGrace <= 0) {
                // Out of grace, now we're on cooldown.
                remainingCoolDownTicks = coolDownDurationTicks;
            }
        }
    }

    @Override
    public void onCollide(Yoyo yoyo, Entity other, YoyoContext c) {
        if (remainingCoolDownTicks > 0) {
            return;
        }

        if (yoyo.isEntityOwnerOrOwnersMount(other)) {
            return;
        }

        if (!other.level().isClientSide) {
            other.hurt(yoyo.damageSources().cactus(), damage);
        }

        // Add a strong impulse away from the entity
        YoyoUtil.storeEntityCenter(scratch, other)
            .sub(c.ourPos)
            .normalize(-0.5 * c.invMass)
            .add(c.velocity, c.velocity);

        // First tick of attack, start the grace period.
        if (remainingCoolDownGrace <= 0) {
            remainingCoolDownGrace = coolDownGraceDurationTicks;
        }
    }

}
