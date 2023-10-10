package com.jozufozu.yoyos.core;

import java.util.UUID;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Yoyo extends Entity implements TraceableEntity {
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;

    private float targetDistance = 8;

    private float damage = 4;

    private int remainingCooldownTicks;

    private final TargetProviderStack targetProviderStack = new TargetProviderStack();

    public Yoyo(EntityType<? extends Yoyo> entityType, Level level) {
        super(entityType, level);
    }

    public Yoyo(Level level, Player player) {
        super(AllThings.YOYO_ENTITY_TYPE.get(), level);
        setOwner(player);
    }

    public void pushTarget(TargetProvider targetProvider) {
        targetProviderStack.push(targetProvider);
    }

    @Override
    public void tick() {
        super.tick();

        chaseTarget();

        if (cooldown()) {
            hitEntities();
        }
    }

    /**
     * @return true if the cooldown is up and we can attack.
     */
    private boolean cooldown() {
        if (remainingCooldownTicks <= 0) {
            remainingCooldownTicks = 0;
            return true;
        }

        remainingCooldownTicks--;
        return false;
    }

    private void chaseTarget() {
        var target = getTarget();

        if (target == null) {
            return;
        }

        setPos(target);
    }

    @Nullable
    private Vec3 getTarget() {
        var provider = targetProviderStack.get();

        if (provider == null) {
            return null;
        }

        return provider.target();
    }

    private AABB getAttackBoundingBox() {
        return getBoundingBox().inflate(0.1);
    }

    private Predicate<Entity> getAttackEntitySelector() {
        return EntitySelector.NO_SPECTATORS.and(entity -> entity != getOwner())
            .and(Entity::isAttackable);
    }

    private void hitEntities() {
        var toAttack = level().getEntities(this, getAttackBoundingBox(), getAttackEntitySelector());

        if (toAttack.isEmpty()) {
            return;
        }

        for (Entity entity : toAttack) {
            entity.hurt(damageSources().cactus(), damage);
        }

        remainingCooldownTicks = 40;
    }

    public void setItem(ItemStack yoyoStack) {

    }

    public void onThrow() {
        var owner = getOwner();
        if (owner == null) {
            return;
        }

        var ownerPos = owner.position();
        setPos(ownerPos.x, ownerPos.y + owner.getEyeHeight() * 0.75, ownerPos.z);

        pushTarget(new FollowLookTarget());
    }

    public void setOwner(@Nullable Entity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

    @Nullable
    @Override
    public Entity getOwner() {
        if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel) {
            this.cachedOwner = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag var1) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag var1) {

    }

    @Override
    public boolean shouldRender(double $$0, double $$1, double $$2) {
        return true;
    }

    class FollowLookTarget implements TargetProvider {
        @Override
        public boolean isExpired() {
            // This one never expires.
            return false;
        }

        @Override
        @Nullable
        public Vec3 target() {
            var owner = getOwner();

            if (owner == null) {
                return null;
            }

            var ownerEyes = owner.getEyePosition();

            var lookVec = owner.getLookAngle();

            var maxLook = ownerEyes.add(lookVec.scale(targetDistance));
            var hitResult = level().clip(new ClipContext(ownerEyes, maxLook, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, Yoyo.this));

            return hitResult.getLocation();
        }
    }
}
