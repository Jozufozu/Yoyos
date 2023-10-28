package com.jozufozu.yoyos.core.control;

import org.joml.Vector3dc;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public interface YoyoBehavior {
    default void tick(Yoyo yoyo) {}

    default void onTouchBlock(Yoyo yoyo, YoyoContext ctx, BlockPos touchedPos, Vector3dc touchedExact) {}

    default void onCollide(Yoyo yoyo, Entity other, YoyoContext c) {}
}
