package com.jozufozu.yoyos.core.control;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MotionResolver {
    private final Vector3d scratchA = new Vector3d();
    private final Vector3d scratchB = new Vector3d();

    public void solve(Yoyo yoyo, YoyoContext p, CollisionListener collider) {
        var collisionHitBox = yoyo.getBoundingBox();
        var entityHitBox = collisionHitBox.inflate(0.2);
        var searchBox = entityHitBox.minmax(entityHitBox.move(p.velocity.x, p.velocity.y, p.velocity.z))
            .inflate(1);

        var level = yoyo.level();
        var entities = level.getEntities(yoyo, searchBox, yoyo.getCollisionPredicate());

        var collisionsShapes = new ArrayList<VoxelShape>();
        level.getCollisions(yoyo, searchBox).forEach(collisionsShapes::add);


        // TODO: replace with mathy collision detection based on the prism between the start and end bounding box.
        double stepsPerBlock = 64.;

        int numberOfSteps = Mth.ceil(p.velocity.length() * stepsPerBlock);

        var step = scratchA.set(p.velocity)
            .normalize(1 / stepsPerBlock);

        var accumulatedMotion = scratchB.zero();

        // No need to continue stepping once we run out of entities.
        for (int i = 0; i < numberOfSteps; i++) {

            hitAndFilterEntities(yoyo, p, collider, entities, entityHitBox.move(accumulatedMotion.x, accumulatedMotion.y, accumulatedMotion.z));

            collideWithShapes(step, collisionHitBox.move(accumulatedMotion.x, accumulatedMotion.y, accumulatedMotion.z), collisionsShapes, step);

            accumulatedMotion.add(step);
        }

        p.velocity.set(accumulatedMotion);

        scratchA.set(p.ourPos)
            .add(accumulatedMotion);

        yoyo.setCenterPos(scratchA);
        yoyo.setDeltaMovement(p.velocity.x, p.velocity.y, p.velocity.z);

        rotateYoyo(yoyo, p);
    }

    private static void hitAndFilterEntities(Yoyo yoyo, YoyoContext p, CollisionListener collider, List<Entity> entities, AABB boxThisStep) {
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
    }

    private Vector3d collideWithShapes(Vector3dc velocity, AABB box, List<VoxelShape> shapes, Vector3d target) {
        if (shapes.isEmpty()) {
            return target.set(velocity);
        } else {
            double dx = velocity.x();
            double dy = velocity.y();
            double dz = velocity.z();
            if (dy != 0.0) {
                dy = Shapes.collide(Direction.Axis.Y, box, shapes, dy);
                if (dy != 0.0) {
                    box = box.move(0.0, dy, 0.0);
                }
            }

            boolean moreZ = Math.abs(dx) < Math.abs(dz);
            if (moreZ && dz != 0.0) {
                dz = Shapes.collide(Direction.Axis.Z, box, shapes, dz);
                if (dz != 0.0) {
                    box = box.move(0.0, 0.0, dz);
                }
            }

            if (dx != 0.0) {
                dx = Shapes.collide(Direction.Axis.X, box, shapes, dx);
                if (!moreZ && dx != 0.0) {
                    box = box.move(dx, 0.0, 0.0);
                }
            }

            if (!moreZ && dz != 0.0) {
                dz = Shapes.collide(Direction.Axis.Z, box, shapes, dz);
            }

            return target.set(dx, dy, dz);
        }
    }

    public void rotateYoyo(Yoyo yoyo, YoyoContext c) {
        var pointVec = scratchA.set(c.ourPos)
            .sub(c.tailPos);

        if (pointVec.lengthSquared() == 0.0) {
            return;
        }

        // Mostly copied from ProjectileUtil::rotateTowardsMovement

        double xzShadow = Math.sqrt(Math.fma(pointVec.x, pointVec.x, pointVec.z * pointVec.z));
        yoyo.setYRot((float)(Mth.atan2(pointVec.z, pointVec.x) * 180. / Math.PI) + 90);
        yoyo.setXRot((float)(Mth.atan2(xzShadow, pointVec.y) * 180. / Math.PI) - 90);

        while (yoyo.getXRot() - yoyo.xRotO < -180.0F) {
            yoyo.xRotO -= 360.0F;
        }

        while (yoyo.getXRot() - yoyo.xRotO >= 180.0F) {
            yoyo.xRotO += 360.0F;
        }

        while (yoyo.getYRot() - yoyo.yRotO < -180.0F) {
            yoyo.yRotO -= 360.0F;
        }

        while (yoyo.getYRot() - yoyo.yRotO >= 180.0F) {
            yoyo.yRotO += 360.0F;
        }

        yoyo.setXRot(Mth.lerp(0.5f, yoyo.xRotO, yoyo.getXRot()));
        yoyo.setYRot(Mth.lerp(0.5f, yoyo.yRotO, yoyo.getYRot()));
    }
}
