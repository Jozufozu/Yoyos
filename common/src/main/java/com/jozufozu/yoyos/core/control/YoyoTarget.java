package com.jozufozu.yoyos.core.control;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.world.entity.Entity;

public interface YoyoTarget {
    void updateTarget(Yoyo yoyo, Entity owner, YoyoContext c);
}
