package com.jozufozu.yoyos.core;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.yoyos.core.control.*;

import net.minecraft.world.entity.Entity;

public final class Controller {
    public final YoyoContext yoyoContext = new YoyoContext();
    private final List<YoyoBehavior> behaviors = new ArrayList<>();
    private final Mover mover = new Mover();
    private final MotionResolver motionResolver = new MotionResolver();

    private YoyoTarget target = new LookTarget();

    public void tick(Yoyo yoyo) {
        var owner = yoyo.getOwner();

        if (owner == null) {
            return;
        }

        for (YoyoBehavior behavior : behaviors) {
            behavior.tick(yoyo);
        }

        yoyo.getCenterPos(yoyoContext.ourPos);
        yoyo.getOwnerEyePos(yoyoContext.tailPos);

        target.updateTarget(yoyo, owner, yoyoContext);
        mover.tick(yoyo, yoyoContext);

        motionResolver.solve(yoyo, yoyoContext, behaviors);
    }

    public void onThrow(Yoyo yoyo, Entity owner) {
        var ownerPos = owner.position();
        yoyo.setPos(ownerPos.x, ownerPos.y + owner.getEyeHeight() * 0.75, ownerPos.z);

        behaviors.add(new AttackBehavior());
        behaviors.add(new ParticleSprayBehavior());
    }

    public void signalRetract() {
        target = RetractTarget.INSTANCE;

        behaviors.add(new RetractBehavior());
    }
}
