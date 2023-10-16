package com.jozufozu.yoyos.core.control;

import java.util.List;

import org.joml.Vector3d;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class MotionResolver {
    private final Vector3d scratchA = new Vector3d();
    private final Vector3d scratchB = new Vector3d();

    public void integrateMotion(Yoyo yoyo, YoyoContext p, Collider collider) {
        var collisionBox = yoyo.getBoundingBox().inflate(0.2);
        var searchBox = collisionBox.minmax(collisionBox.move(p.velocity.x, p.velocity.y, p.velocity.z))
            .inflate(1);

        var entities = yoyo.level()
            .getEntities(yoyo, searchBox, yoyo.getCollisionPredicate());

        if (!entities.isEmpty()) {
            // TODO: replace with mathy collision detection based on the prism between the start and end bounding box.
            stepAndCollide(yoyo, collisionBox, collider, entities, p);
        }

        scratchA.set(p.ourPos)
            .add(p.velocity);

        yoyo.setCenterPos(scratchA);
        yoyo.setDeltaMovement(p.velocity.x, p.velocity.y, p.velocity.z);
    }

    private void stepAndCollide(Yoyo yoyo, AABB collisionBox, Collider collider, List<Entity> entities, YoyoContext p) {
        double stepsPerBlock = 64.;

        int numberOfSteps = Mth.ceil(p.velocity.length() * stepsPerBlock);

        var stepVec = scratchA.set(p.velocity)
            .normalize()
            .div(stepsPerBlock);

        var checkVec = scratchB.zero();

        // No need to continue stepping once we run out of entities.
        for (int i = 0; i < numberOfSteps && !entities.isEmpty(); i++) {
            var boxThisStep = collisionBox.move(checkVec.x, checkVec.y, checkVec.z);

            // Iterate using removeIf to filter the list as we go.
            entities.removeIf(entity -> {
                var entityBox = entity.getBoundingBox();

                if (entityBox.intersects(boxThisStep)) {
                    collider.onCollide(yoyo, entity, p);
                    return true;
                } else {
                    return false;
                }
            });

            checkVec.add(stepVec);
        }
    }
}
