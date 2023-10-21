package com.jozufozu.yoyos.core.control;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.world.entity.Entity;

public class RetractBehavior implements YoyoBehavior {

    @Override
    public void onCollide(Yoyo yoyo, Entity other, YoyoContext c) {
        if (yoyo.isEntityOwnerOrOwnersMount(other)) {
            yoyo.discard();
        }
    }

    @Override
    public void tick(Yoyo yoyo) {

    }
}
