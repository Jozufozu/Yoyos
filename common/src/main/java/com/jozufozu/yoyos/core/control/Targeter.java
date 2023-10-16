package com.jozufozu.yoyos.core.control;

import org.joml.Vector3d;

import com.jozufozu.yoyos.core.Yoyo;
import com.jozufozu.yoyos.infrastructure.util.YoyoUtil;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class Targeter {
    private final Vector3d scratch = new Vector3d();

    public void updateTarget(Yoyo yoyo, Entity owner, YoyoContext c) {
        // Mostly copied from ProjectileUtil, adapted for JOML

        YoyoUtil.storeEntityViewVec(scratch, owner, 0)
            .mul(c.targetDistance);

        AABB entitySearchBounds = owner.getBoundingBox()
            .expandTowards(scratch.x, scratch.y, scratch.z)
            .inflate(1.0);

        c.targetPos.set(c.eyePos)
            .add(scratch);

        // check blocks
        var level = yoyo.level();
        HitResult hitResult = level.clip(new ClipContext(YoyoUtil.mVec(c.eyePos), YoyoUtil.mVec(c.targetPos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        if (hitResult.getType() != HitResult.Type.MISS) {
            YoyoUtil.set(c.targetPos, hitResult.getLocation());
        }

        // check entities
        double closestSeenDistanceSqr = c.targetPos.distanceSquared(c.eyePos);

        for (Entity entity : level.getEntities(owner, entitySearchBounds, yoyo.getCollisionPredicate())) {
            AABB checkBounds = entity.getBoundingBox().inflate(0.15);

            boolean hit = YoyoUtil.clip(checkBounds, c.eyePos, c.targetPos, scratch);

            if (checkBounds.contains(c.eyePos.x, c.eyePos.y, c.eyePos.z)) {
                if (closestSeenDistanceSqr >= 0.0) {
                    if (hit) {
                        c.targetPos.set(scratch);
                    } else {
                        c.targetPos.set(c.eyePos);
                        // Can't get any closer.
                        return;
                    }
                    closestSeenDistanceSqr = 0.0;
                }
            } else if (hit) {
                double distanceSqr = c.eyePos.distanceSquared(scratch);
                if (distanceSqr < closestSeenDistanceSqr) {
                    c.targetPos.set(scratch);
                    closestSeenDistanceSqr = distanceSqr;
                }
            }
        }
    }
}
