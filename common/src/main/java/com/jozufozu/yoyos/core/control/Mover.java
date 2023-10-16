package com.jozufozu.yoyos.core.control;

import org.joml.Vector3d;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.util.Mth;

public class Mover {

    private final Vector3d scratchA = new Vector3d();

    public void tick(Yoyo yoyo, YoyoContext c) {
        applyDrag(c);

        applyNearDrag(c);

        applyTargetingForce(c);

        constrainWithinDistance(c);
    }

    // Apply a constant outward force. Helps? to stabilize the yoyo.
    private void applyOutwardForce(YoyoContext c) {
        var a = 0.05 * c.invMass;

        scratchA.set(c.ourPos)
            .sub(c.eyePos)
            .normalize(a)
            .add(c.velocity, c.velocity);
    }

    // Near drag and targeting - the two main forces dictating movement
    // https://www.desmos.com/calculator/stdprtgsoh

    // Apply drag to simulate air resistance and smooth out motion.
    private void applyDrag(YoyoContext c) {
        double v2 = c.velocity.lengthSquared();

        // velocity will normalize to NaN
        if (Mth.equal(v2, 0)) {
            return;
        }

        double f = v2 / 2. * (0.25 * 0.25) * c.dragCoefficient;

        // Don't let drag push us backwards.
        double a = Math.min(f * c.invMass, Math.sqrt(v2));

        scratchA.set(c.velocity)
            .normalize(-a)
            .add(c.velocity, c.velocity);
    }

    // Counteract motion when near the target. Not physically accurate but it works really well.
    private void applyNearDrag(YoyoContext c) {
        double v = c.velocity.length();

        // velocity will normalize to NaN
        if (Mth.equal(v, 0)) {
            return;
        }

        scratchA.set(c.targetPos)
            .sub(c.ourPos);

        double d2 = scratchA.lengthSquared();

        double f = v * Math.pow(100, -d2 * c.invMass);

        double a = f / Math.max(c.mass, 1);

        scratchA.set(c.velocity)
            .normalize(-a)
            .add(c.velocity, c.velocity);
    }

    // Apply the force that pushes this yoyo towards where our owner is looking.
    private void applyTargetingForce(YoyoContext c) {
        scratchA.set(c.targetPos)
            .sub(c.ourPos);

        double d2 = scratchA.lengthSquared();
        double d = Math.sqrt(d2);
        double d4 = d2 * d2;

        double f = 1 - Math.pow(10, -d4) + d / 4.;

        double a = f * c.invMass;

        scratchA.normalize(a)
            .add(c.velocity, c.velocity);
    }

    private void constrainWithinDistance(YoyoContext c) {
        var nextDistance = scratchA.set(c.ourPos)
            .add(c.velocity)
            .distance(c.eyePos);

        if (nextDistance > c.targetDistance) {

            // add an impulse to bring us back to the target distance.
            scratchA.sub(c.eyePos)
                .normalize(c.targetDistance - nextDistance)
                .add(c.velocity, c.velocity);
        }
    }


}
