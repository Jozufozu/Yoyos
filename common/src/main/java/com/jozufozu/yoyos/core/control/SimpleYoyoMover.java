package com.jozufozu.yoyos.core.control;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SimpleYoyoMover {
    private final float targetDistance = 8;
    private final float weight = 1;

    private final Vector3d motion = new Vector3d();

    public void tick(Yoyo yoyo, Consumer<Entity> collisionCollector) {
        chaseTarget(yoyo);

        integrateMotion(yoyo, collisionCollector);
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

    private void applyMotion(Yoyo yoyo, Consumer<Entity> collisionCollector) {
        yoyo.setPos(yoyo.position().add(motion.x, motion.y, motion.z));

        var entities = yoyo.level()
            .getEntities(yoyo, getAttackBoundingBox(yoyo), getCollisionPredicate(yoyo));

        entities.forEach(collisionCollector);
    }

    private void integrateMotion(Yoyo yoyo, Consumer<Entity> collisionCollector) {
        var start = yoyo.position();
        var end = start.add(motion.x, motion.y, motion.z);
        var delta = end.subtract(start);
        var dir = delta.normalize();

        var searchBox = yoyo.getBoundingBox().minmax(yoyo.getBoundingBox().move(delta))
            .inflate(1);

        var entities = yoyo.level()
            .getEntities(yoyo, searchBox, getCollisionPredicate(yoyo));

        if (!entities.isEmpty()) {
            double distance = delta.length();

            // TODO: replace with mathy collision detection based on the prism between the start and end bounding box.
            double stepsPerBlock = 64.;

            int numberOfSteps = Mth.ceil(distance * stepsPerBlock);

            var increment = dir.scale(1. / stepsPerBlock);
            var accumulate = Vec3.ZERO;
            for (int i = 0; i < numberOfSteps && !entities.isEmpty(); i++) {
                var boxThisStep = yoyo.getBoundingBox()
                    .move(accumulate);

                // Iterate using removeIf to filter the list as we go.
                entities.removeIf(entity -> {
                    var entityBox = entity.getBoundingBox();

                    if (entityBox.intersects(boxThisStep)) {
                        collisionCollector.accept(entity);
                        return true;
                    } else {
                        return false;
                    }
                });

                accumulate = accumulate.add(increment);
            }
        }

        yoyo.setPos(end);
        yoyo.setDeltaMovement(motion.x, motion.y, motion.z);
    }

    @Nullable
    private Vec3 getTarget(Yoyo yoyo) {
        var owner = yoyo.getOwner();

        if (owner == null) {
            return null;
        }

        // Mostly copied from ProjectileUtil

        Vec3 lookVec = owner.getViewVector(0.0F).scale(targetDistance);
        Level level = owner.level();
        Vec3 eyePos = owner.getEyePosition();
        Vec3 maxTarget = eyePos.add(lookVec);

        // check blocks
        HitResult hitResult = level.clip(new ClipContext(eyePos, maxTarget, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        if (hitResult.getType() != HitResult.Type.MISS) {
            maxTarget = hitResult.getLocation();
        }

        // check entities
        AABB searchBounds = owner.getBoundingBox().expandTowards(lookVec).inflate(1.0);
        double closestSeenDistanceSqr = targetDistance * targetDistance;
        Entity closestSeenEntity = null;
        Vec3 closestSeenPosition = null;

        for (Entity entity : level.getEntities(owner, searchBounds, getCollisionPredicate(yoyo))) {
            AABB checkBounds = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> maybeHit = checkBounds.clip(eyePos, maxTarget);
            if (checkBounds.contains(eyePos)) {
                if (closestSeenDistanceSqr >= 0.0) {
                    closestSeenEntity = entity;
                    closestSeenPosition = maybeHit.orElse(eyePos);
                    closestSeenDistanceSqr = 0.0;
                }
            } else if (maybeHit.isPresent()) {
                Vec3 hitPos = maybeHit.get();
                double distanceSqr = eyePos.distanceToSqr(hitPos);
                if (distanceSqr < closestSeenDistanceSqr || closestSeenDistanceSqr == 0.0) {
                    if (entity.getRootVehicle() == owner.getRootVehicle()) {
                        if (closestSeenDistanceSqr == 0.0) {
                            closestSeenEntity = entity;
                            closestSeenPosition = hitPos;
                        }
                    } else {
                        closestSeenEntity = entity;
                        closestSeenPosition = hitPos;
                        closestSeenDistanceSqr = distanceSqr;
                    }
                }
            }
        }

        if (closestSeenEntity != null) {
            hitResult = new EntityHitResult(closestSeenEntity, closestSeenPosition);
        }

        return hitResult.getLocation();
    }

    private AABB getAttackBoundingBox(Yoyo yoyo) {
        return yoyo.getBoundingBox().inflate(0.1);
    }

    private Predicate<Entity> getCollisionPredicate(Yoyo yoyo) {
        return EntitySelector.NO_SPECTATORS.and(entity -> entity != yoyo && entity != yoyo.getOwner());
    }
}
