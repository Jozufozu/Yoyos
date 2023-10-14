package com.jozufozu.yoyos.core.control;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.world.entity.Entity;

public class SimpleYoyoCollider {

    private float damage = 4;

    private int remainingCooldownTicks;

    public void tick(Yoyo yoyo, CollisionCollector collisionCollector) {
        if (!cooldown()) {
            return;
        }

        if (collisionCollector.isEmpty()) {
            return;
        }

        for (Entity entity : collisionCollector) {
            entity.hurt(yoyo.damageSources().cactus(), damage);
        }

        remainingCooldownTicks = 15;
    }

    /**
     * @return true if the cooldown is up and we can attack.
     */
    private boolean cooldown() {
        if (remainingCooldownTicks <= 0) {
            remainingCooldownTicks = 0;
            return true;
        }

        remainingCooldownTicks--;
        return false;
    }

}
