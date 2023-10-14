package com.jozufozu.yoyos.core.control;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SimpleYoyoMover {
    private final float targetDistance = 8;
    private final float weight = 1;

    private final Vector3d motion = new Vector3d();

    public void tick(Yoyo yoyo, CollisionCollector collisionCollector) {
        chaseTarget(yoyo);

        applyMotion(yoyo, collisionCollector);
    }

    private void chaseTarget(Yoyo yoyo) {
        var target = getTarget(yoyo);

        if (target == null) {
            return;
        }

        var pos = yoyo.position();
        motion.set(target.x, target.y, target.z)
            .sub(pos.x, pos.y + yoyo.getBbHeight() / 2, pos.z)
            .mul(0.15 + 0.85 * Math.pow(1.1, -(weight * weight)));
    }

    private void applyMotion(Yoyo yoyo, CollisionCollector collisionCollector) {
        yoyo.setPos(yoyo.position().add(motion.x, motion.y, motion.z));

        var entities = yoyo.level()
            .getEntities(yoyo, getAttackBoundingBox(yoyo), getAttackEntitySelector(yoyo));

        collisionCollector.markHit(entities);
    }

    @Nullable
    private Vec3 getTarget(Yoyo yoyo) {
        var owner = yoyo.getOwner();

        if (owner == null) {
            return null;
        }

        var ownerEyes = owner.getEyePosition();

        var lookVec = owner.getLookAngle();

        var maxLook = ownerEyes.add(lookVec.scale(targetDistance));
        var hitResult = yoyo.level()
            .clip(new ClipContext(ownerEyes, maxLook, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, yoyo));

        return hitResult.getLocation();
    }

    private AABB getAttackBoundingBox(Yoyo yoyo) {
        return yoyo.getBoundingBox().inflate(0.1);
    }

    private Predicate<Entity> getAttackEntitySelector(Yoyo yoyo) {
        return EntitySelector.NO_SPECTATORS.and(entity -> entity != yoyo.getOwner());
    }
}
