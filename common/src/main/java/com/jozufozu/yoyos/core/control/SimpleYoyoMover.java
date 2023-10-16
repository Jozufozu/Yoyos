package com.jozufozu.yoyos.core.control;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.joml.Vector3d;

import com.jozufozu.yoyos.core.Yoyo;
import com.jozufozu.yoyos.infrastructure.util.JomlUtil;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class SimpleYoyoMover {
    // How far away from the player we try to be.
    private final double targetDistance = 8;

    private final double dragCoefficient = 15;

    // Arbitrary unit. How heavy we are.
    private final double mass = 5;
    private final double invMass = 1. / mass;


    // Arbitrary unit. How heavy the player is.
    private final double playerMass = 64;

    // The velocity of this yoyo. Persists between ticks and only changes when a force is applied.
    private final Vector3d velocity = new Vector3d();

    // The eye position of our owner.
    private final Vector3d eyePos = new Vector3d();

    // Where we should be.
    private final Vector3d targetPos = new Vector3d();

    // Where we are.
    private final Vector3d ourPos = new Vector3d();

    // Pre-allocated scratch vectors for misc math that can't mutate the above.
    // Only A is needed in most cases, but for motion stepping we need both.
    private final Vector3d scratchA = new Vector3d();
    private final Vector3d scratchB = new Vector3d();

    public void tick(Yoyo yoyo, Consumer<Entity> collisionCollector) {
        yoyo.getCenterPos(ourPos);
        updateTargetAndEye(yoyo);

        applyTargetingForce();

        applyDrag();

        applyNearDrag();

        constrainWithinDistance();

        integrateMotion(yoyo, collisionCollector);
    }

    // Apply a constant outward force. Helps to stabilize the yoyo.
    private void applyOutwardForce() {
        var a = 0.01 * invMass;

        scratchA.set(ourPos)
            .sub(eyePos)
            .normalize(a)
            .add(velocity, velocity);
    }

    // Near drag and targeting - the two main forces dictating movement
    // https://www.desmos.com/calculator/stdprtgsoh

    // Apply drag to simulate air resistance and smooth out motion.
    private void applyDrag() {
        double v2 = velocity.lengthSquared();
        double f = v2 / 2. * (0.25 * 0.25) * dragCoefficient;

        // Don't let drag push us backwards or truly zero out velocity.
        double a = Math.min(f * invMass, 0.95 * Math.sqrt(v2));

        scratchA.set(velocity)
            .normalize(-a)
            .add(velocity, velocity);
    }

    // Counteract motion when near the target. Not a true force, or at least lets pretend mass cancels out.
    private void applyNearDrag() {
        scratchA.set(targetPos)
            .sub(ourPos);

        double d2 = scratchA.lengthSquared();
        double v = velocity.length();

        double f = v * Math.pow(100, -d2 * invMass);

        double a = f / Math.max(mass, 1);

        scratchA.set(velocity)
            .normalize(-a)
            .add(velocity, velocity);
    }

    // Apply the force that pushes this yoyo towards where our owner is looking.
    private void applyTargetingForce() {
        scratchA.set(targetPos)
            .sub(ourPos);

        double d2 = scratchA.lengthSquared();
        double d = Math.sqrt(d2);
        double d4 = d2 * d2;

        double f = 1 - Math.pow(10, -d4) + d / 4.;

        double a = f * invMass;

        scratchA.normalize(a)
            .add(velocity, velocity);
    }

    private void integrateMotion(Yoyo yoyo, Consumer<Entity> collisionCollector) {
        var collisionBox = yoyo.getBoundingBox().inflate(0.2);
        var searchBox = collisionBox.minmax(collisionBox.move(velocity.x, velocity.y, velocity.z))
            .inflate(1);

        var entities = yoyo.level()
            .getEntities(yoyo, searchBox, getCollisionPredicate(yoyo));

        if (!entities.isEmpty()) {
            // TODO: replace with mathy collision detection based on the prism between the start and end bounding box.
            double stepsPerBlock = 64.;

            int numberOfSteps = Mth.ceil(velocity.length() * stepsPerBlock);

            var stepVec = scratchA.set(velocity)
                .normalize()
                .div(stepsPerBlock);

            var checkVec = scratchB.zero();

            for (int i = 0; i < numberOfSteps && !entities.isEmpty(); i++) {
                var boxThisStep = collisionBox.move(checkVec.x, checkVec.y, checkVec.z);

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

                checkVec.add(stepVec);
            }
        }

        scratchA.set(ourPos)
            .add(velocity);

        yoyo.setCenterPos(scratchA);
        yoyo.setDeltaMovement(velocity.x, velocity.y, velocity.z);
    }

    private void constrainWithinDistance() {
        var nextDistance = scratchA.set(ourPos)
            .add(velocity)
            .distance(eyePos);

        if (nextDistance > targetDistance) {

            // add an impulse to bring us back to the target distance.
            scratchA.sub(eyePos)
                .normalize(targetDistance - nextDistance)
                .add(velocity, velocity);
        }
    }

    private void updateTargetAndEye(Yoyo yoyo) {
        var owner = yoyo.getOwner();

        if (owner == null) {
            return;
        }

        // Mostly copied from ProjectileUtil, adapted for JOML

        setEyePos(owner);

        JomlUtil.storeEntityViewVec(scratchA, owner, 0)
            .mul(targetDistance);

        AABB entitySearchBounds = owner.getBoundingBox()
            .expandTowards(scratchA.x, scratchA.y, scratchA.z)
            .inflate(1.0);

        targetPos.set(eyePos)
            .add(scratchA);

        // check blocks
        var level = yoyo.level();
        HitResult hitResult = level.clip(new ClipContext(JomlUtil.mVec(eyePos), JomlUtil.mVec(targetPos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        if (hitResult.getType() != HitResult.Type.MISS) {
            JomlUtil.set(targetPos, hitResult.getLocation());
        }

        // check entities
        double closestSeenDistanceSqr = targetPos.distanceSquared(eyePos);

        for (Entity entity : level.getEntities(owner, entitySearchBounds, getCollisionPredicate(yoyo))) {
            AABB checkBounds = entity.getBoundingBox().inflate(0.15);

            boolean hit = JomlUtil.clip(checkBounds, eyePos, targetPos, scratchA);

            if (checkBounds.contains(eyePos.x, eyePos.y, eyePos.z)) {
                if (closestSeenDistanceSqr >= 0.0) {
                    if (hit) {
                        targetPos.set(scratchA);
                    } else {
                        targetPos.set(eyePos);
                        // Can't get any closer.
                        return;
                    }
                    closestSeenDistanceSqr = 0.0;
                }
            } else if (hit) {
                double distanceSqr = eyePos.distanceSquared(scratchA);
                if (distanceSqr < closestSeenDistanceSqr) {
                    targetPos.set(scratchA);
                    closestSeenDistanceSqr = distanceSqr;
                }
            }
        }
    }

    private void setEyePos(Entity owner) {
        var pos = owner.position();
        eyePos.set(pos.x, pos.y + owner.getEyeHeight(), pos.z);
    }

    private Predicate<Entity> getCollisionPredicate(Yoyo yoyo) {
        var owner = yoyo.getOwner();
        var out = EntitySelector.NO_SPECTATORS.and(entity -> entity != yoyo);
        if (owner != null) {
            // Don't collide with our owner or anything they're riding.
            return out.and(entity -> entity != owner && !entity.isPassengerOfSameVehicle(owner));
        }
        return out;
    }
}
