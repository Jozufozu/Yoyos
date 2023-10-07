package com.jozufozu.yoyos.core;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Yoyo extends Entity implements TraceableEntity {
    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;

    public Yoyo(EntityType<? extends Yoyo> entityType, Level level) {
        super(entityType, level);
    }

    public Yoyo(Level level, Player player) {
        super(AllThings.YOYO_ENTITY_TYPE, level);
        setOwner(player);
    }

    public void setOwner(@Nullable Entity $$0) {
        if ($$0 != null) {
            this.ownerUUID = $$0.getUUID();
            this.cachedOwner = $$0;
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

    public void setItem(ItemStack yoyoStack) {

    }
}
