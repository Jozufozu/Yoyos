package com.jozufozu.yoyos.core.control;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3d;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MotionResolver {
    private final Vector3d scratchA = new Vector3d();
    private final Vector3d scratchB = new Vector3d();
    private final Vector3d scratchC = new Vector3d();

    public void solve(Yoyo yoyo, YoyoContext p, List<YoyoBehavior> behaviors) {
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

        scratchA.set(p.velocity)
            .normalize(1 / stepsPerBlock);

        scratchB.zero();

        // No need to continue stepping once we run out of entities.
        // scratchA: step delta
        // scratchB: accumulated motion relative to start pos
        // scratchC: collision pos
        for (int i = 0; i < numberOfSteps; i++) {
            hitAndFilterEntities(yoyo, p, behaviors, entities, entityHitBox.move(scratchB.x, scratchB.y, scratchB.z));

            collideWithShapes(yoyo, p, behaviors, collisionsShapes, collisionHitBox);

            scratchB.add(scratchA);
        }

        p.velocity.set(scratchB);

        scratchA.set(p.ourPos)
            .add(scratchB);

        yoyo.setCenterPos(scratchA);
        yoyo.setDeltaMovement(p.velocity.x, p.velocity.y, p.velocity.z);

        rotateYoyo(yoyo, p);
    }

    private void collideWithShapes(Yoyo yoyo, YoyoContext p, List<YoyoBehavior> behaviors, ArrayList<VoxelShape> collisionsShapes, AABB collisionHitBox) {
        if (collisionsShapes.isEmpty()) {
            return;
        }

        AABB box = collisionHitBox.move(scratchB.x, scratchB.y, scratchB.z);
        double dx = scratchA.x;
        double dy = scratchA.y;
        double dz = scratchA.z;
        if (dy != 0.0) {
            dy = collideAndHandleTouch(Direction.Axis.Y, yoyo, p, behaviors, collisionsShapes, box, dy);
            if (dy != 0.0) {
                box = box.move(0.0, dy, 0.0);
            }
        }

        boolean moreZ = Math.abs(dx) < Math.abs(dz);
        if (moreZ && dz != 0.0) {
            dz = collideAndHandleTouch(Direction.Axis.Z, yoyo, p, behaviors, collisionsShapes, box, dz);
            if (dz != 0.0) {
                box = box.move(0.0, 0.0, dz);
            }
        }

        if (dx != 0.0) {
            dx = collideAndHandleTouch(Direction.Axis.X, yoyo, p, behaviors, collisionsShapes, box, dx);
            if (!moreZ && dx != 0.0) {
                box = box.move(dx, 0.0, 0.0);
            }
        }

        if (!moreZ && dz != 0.0) {
            dz = collideAndHandleTouch(Direction.Axis.Z, yoyo, p, behaviors, collisionsShapes, box, dz);
        }

        scratchA.set(dx, dy, dz);
    }

    private double collideAndHandleTouch(Direction.Axis axis, Yoyo yoyo, YoyoContext p, List<YoyoBehavior> behaviors, ArrayList<VoxelShape> collisionsShapes, AABB box, double da) {
        double daNow = Shapes.collide(axis, box, collisionsShapes, da);
        if (da != daNow) {
            scratchC.set(p.ourPos)
                .add(scratchB);

            int sign = Mth.sign(da);
            if (sign > 0) {
                set(axis, scratchC, box.max(axis) + daNow + Mth.EPSILON);
            } else if (sign < 0) {
                set(axis, scratchC, box.min(axis) + daNow - Mth.EPSILON);
            }

            var hitPos = BlockPos.containing(scratchC.x, scratchC.y, scratchC.z);

            for (YoyoBehavior behavior : behaviors) {
                behavior.onTouchBlock(yoyo, p, hitPos, scratchC);
            }
        }
        return daNow;
    }

    private static void set(Direction.Axis axis, Vector3d vec, double val) {
        switch (axis) {
        case X -> vec.x = val;
        case Y -> vec.y = val;
        case Z -> vec.z = val;
        }
    }

    private static void hitAndFilterEntities(Yoyo yoyo, YoyoContext p, List<YoyoBehavior> behaviors, List<Entity> entities, AABB boxThisStep) {
        // Iterate using removeIf to filter the list as we go.
        entities.removeIf(entity -> {
            var entityBox = entity.getBoundingBox();

            if (entityBox.intersects(boxThisStep)) {
                for (YoyoBehavior behavior : behaviors) {
                    behavior.onCollide(yoyo, entity, p);
                }
                return true;
            } else {
                return false;
            }
        });
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
