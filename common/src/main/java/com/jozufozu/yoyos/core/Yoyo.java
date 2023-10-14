package com.jozufozu.yoyos.core;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.yoyos.core.control.SimpleYoyoController;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Yoyo extends Entity implements TraceableEntity {
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    @Nullable
    private SimpleYoyoController controller;

    @NotNull
    private ItemStack yoyoStack = ItemStack.EMPTY;

    public Yoyo(EntityType<? extends Yoyo> entityType, Level level) {
        super(entityType, level);
    }

    public Yoyo(Level level) {
        super(AllThings.YOYO_ENTITY_TYPE.get(), level);
    }

    @Override
    public void tick() {
        if (!level().isClientSide && shouldDiscard()) {
            discard();
            return;
        }

        super.tick();

        var controller = getController();
        if (controller != null && !level().isClientSide) {
            controller.tick(this);
        }
    }

    @Nullable
    private SimpleYoyoController getController() {
        return controller;
    }

    /**
     * @return true if this yoyo has reached an invalid state and should be removed.
     */
    private boolean shouldDiscard() {
        return yoyoStack.isEmpty() || getOwner() == null || getController() == null || !isStillHeld();
    }

    private boolean isStillHeld() {
        if (getOwner() instanceof LivingEntity living) {
            return yoyoStack.equals(living.getMainHandItem()) || yoyoStack.equals(living.getOffhandItem());
        }
        return false;
    }

    public void setController(SimpleYoyoController controller) {
        this.controller = controller;
    }

    public void setYoyoStack(ItemStack yoyoStack) {
        this.yoyoStack = yoyoStack;
    }

    public void onThrow() {
        var owner = getOwner();
        var yoyoController = getController();

        if (owner == null || yoyoController == null) {
            discard();
            return;
        }

        yoyoController.onThrow(this, owner);
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
}
