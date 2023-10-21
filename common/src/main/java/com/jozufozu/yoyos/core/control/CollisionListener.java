package com.jozufozu.yoyos.core.control;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.world.entity.Entity;

public interface CollisionListener {
    void onCollide(Yoyo yoyo, Entity other, YoyoContext c);
}
