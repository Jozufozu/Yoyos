package com.jozufozu.yoyos.core.control;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.world.entity.Entity;

public record SimpleYoyoController(SimpleYoyoMover mover, SimpleYoyoCollider simpleYoyoCollider) {

    public void tick(Yoyo yoyo) {
        if (yoyo.level().isClientSide) {
            mover.tick(yoyo, $ -> {});
            return;
        }

        var collisionCollector = new CollisionCollector();

        mover.tick(yoyo, collisionCollector::markHit);

        simpleYoyoCollider.tick(yoyo, collisionCollector);
    }

    public void onThrow(Yoyo yoyo, Entity owner) {
        var ownerPos = owner.position();
        yoyo.setPos(ownerPos.x, ownerPos.y + owner.getEyeHeight() * 0.75, ownerPos.z);
    }
}
