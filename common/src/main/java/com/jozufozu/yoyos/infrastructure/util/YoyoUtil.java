package com.jozufozu.yoyos.infrastructure.util;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class YoyoUtil {

    private static final double EPSILON = 1.0E-7;

    public static Vector3d storeEntityCenter(Vector3d dest, Entity entity) {
        var pos = entity.position();
        return dest.set(pos.x, pos.y + entity.getBbHeight() / 2, pos.z);
    }

    public static Vector3d storeEntityViewVec(Vector3d dest, Entity entity, float pt) {
        float $$0 = entity.getViewXRot(pt);
        float $$1 = entity.getViewYRot(pt);
        float $$2 = $$0 * (float) (Math.PI / 180.0);
        float $$3 = -$$1 * (float) (Math.PI / 180.0);
        float $$4 = Mth.cos($$3);
        float $$5 = Mth.sin($$3);
        float $$6 = Mth.cos($$2);
        float $$7 = Mth.sin($$2);
        return dest.set($$5 * $$6, -$$7, $$4 * $$6);
    }

    public static Vector3d set(Vector3d dest, Vec3 source) {
        return dest.set(source.x, source.y, source.z);
    }

    public static Vec3 mVec(Vector3d vec) {
        return new Vec3(vec.x, vec.y, vec.z);
    }

    public static boolean clip(AABB self, Vector3dc start, Vector3dc end, Vector3d dest) {
        double[] pctPtr = new double[]{1.0};
        double dx = end.x() - start.x();
        double dy = end.y() - start.y();
        double dz = end.z() - start.z();
        Direction direction = getDirection(self, start, pctPtr, dx, dy, dz);
        if (direction == null) {
            return false;
        } else {
            double pct = pctPtr[0];
            dest.set(start)
                .add(pct * dx, pct * dy, pct * dz);
            return true;
        }
    }

    @Nullable
    public static Direction getDirection(AABB box, Vector3dc start, double[] outPctPtr, double dx, double dy, double dz) {
        Direction out = null;
        if (dx > EPSILON) {
            out = clipPoint(outPctPtr, Direction.WEST, out, dx, dy, dz, box.minX, box.minY, box.maxY, box.minZ, box.maxZ, start.x(), start.y(), start.z());
        } else if (dx < -EPSILON) {
            out = clipPoint(outPctPtr, Direction.EAST, out, dx, dy, dz, box.maxX, box.minY, box.maxY, box.minZ, box.maxZ, start.x(), start.y(), start.z());
        }

        if (dy > EPSILON) {
            out = clipPoint(outPctPtr, Direction.DOWN, out, dy, dz, dx, box.minY, box.minZ, box.maxZ, box.minX, box.maxX, start.y(), start.z(), start.x());
        } else if (dy < -EPSILON) {
            out = clipPoint(outPctPtr, Direction.UP, out, dy, dz, dx, box.maxY, box.minZ, box.maxZ, box.minX, box.maxX, start.y(), start.z(), start.x());
        }

        if (dz > EPSILON) {
            out = clipPoint(outPctPtr, Direction.NORTH, out, dz, dx, dy, box.minZ, box.minX, box.maxX, box.minY, box.maxY, start.z(), start.x(), start.y());
        } else if (dz < -EPSILON) {
            out = clipPoint(outPctPtr, Direction.SOUTH, out, dz, dx, dy, box.maxZ, box.minX, box.maxX, box.minY, box.maxY, start.z(), start.x(), start.y());
        }

        return out;
    }

    @Nullable
    public static Direction clipPoint(
        double[] outPctPtr, Direction clipAxis, @Nullable Direction otherwise,
        double da,
        double db,
        double dc,
        double boundA,
        double minB,
        double maxB,
        double minC,
        double maxC,
        double startA,
        double startB,
        double startC
    ) {
        double aPct = (boundA - startA) / da;
        double bPos = startB + aPct * db;
        double cPos = startC + aPct * dc;
        if (0.0 < aPct && aPct < outPctPtr[0] && epsilonWithin(bPos, minB, maxB) && epsilonWithin(cPos, minC, maxC)) {
            outPctPtr[0] = aPct;
            return clipAxis;
        } else {
            return otherwise;
        }
    }

    private static boolean epsilonWithin(double x, double lowerBound, double upperBound) {
        return lowerBound - EPSILON < x && x < upperBound + EPSILON;
    }
}
