package com.jozufozu.yoyos.core.control;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.world.entity.Entity;

public final class Controller {
    private final YoyoContext yoyoContext = new YoyoContext();
    private final Mover mover = new Mover();
    private final Collider collider = new Collider();
    private final Targeter targeter = new Targeter();
    private final MotionResolver motionResolver = new MotionResolver();

    public void tick(Yoyo yoyo) {
        var owner = yoyo.getOwner();

        if (owner == null) {
            return;
        }

        collider.tick(yoyo);

        yoyo.getCenterPos(yoyoContext.ourPos);
        yoyo.getOwnerEyePos(yoyoContext.eyePos);

        targeter.updateTarget(yoyo, owner, yoyoContext);
        mover.tick(yoyo, yoyoContext);

        motionResolver.integrateMotion(yoyo, yoyoContext, collider);
    }

    public void onThrow(Yoyo yoyo, Entity owner) {
        var ownerPos = owner.position();
        yoyo.setPos(ownerPos.x, ownerPos.y + owner.getEyeHeight() * 0.75, ownerPos.z);
    }

}
