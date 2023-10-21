package com.jozufozu.yoyos.core.control;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.world.entity.Entity;

public enum RetractTarget implements YoyoTarget {
    INSTANCE;

    @Override
    public void updateTarget(Yoyo yoyo, Entity owner, YoyoContext c) {
        c.targetPos.set(c.tailPos);
    }
}
