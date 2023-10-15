package com.jozufozu.yoyos.core;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.jozufozu.yoyos.core.control.SimpleYoyoCollider;
import com.jozufozu.yoyos.core.control.SimpleYoyoController;
import com.jozufozu.yoyos.core.control.SimpleYoyoMover;
import com.jozufozu.yoyos.infrastructure.util.EntityDataHolder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;

public class Yoyo extends Entity implements TraceableEntity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(Yoyo.class, EntityDataSerializers.ITEM_STACK);

    @Nullable
    private UUID ownerUUID;
    @Nullable
    private Entity cachedOwner;
    @Nullable
    private SimpleYoyoController controller;

    private final EntityDataHolder<ItemStack> yoyoStack = new EntityDataHolder<>(this, DATA_ITEM_STACK);

    public Yoyo(EntityType<? extends Yoyo> entityType, Level level) {
        super(entityType, level);
        setController(new SimpleYoyoController(new SimpleYoyoMover(), new SimpleYoyoCollider()));
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
        if (controller != null) {
            controller.tick(this);
        }
    }

    public Vector3d getCenterPos(Vector3d dest) {
        var pos = position();
        return dest.set(pos.x, pos.y + getBbHeight() / 2, pos.z);
    }

    public void setCenterPos(Vector3dc centerPos) {
        setPos(centerPos.x(), centerPos.y() - getBbHeight() / 2, centerPos.z());
    }

    @Nullable
    private SimpleYoyoController getController() {
        return controller;
    }

    /**
     * @return true if this yoyo has reached an invalid state and should be removed.
     */
    private boolean shouldDiscard() {
        return getYoyoStack().isEmpty() || getOwner() == null || getController() == null || !isStillHeld();
    }

    private boolean isStillHeld() {
        if (getOwner() instanceof LivingEntity living) {
            return getYoyoStack().equals(living.getMainHandItem()) || getYoyoStack().equals(living.getOffhandItem());
        }
        return false;
    }

    public void setController(SimpleYoyoController controller) {
        this.controller = controller;
    }

    public void setYoyoStack(ItemStack yoyoStack) {
        this.yoyoStack.set(yoyoStack);
    }

    public ItemStack getYoyoStack() {
        return this.yoyoStack.get();
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
        this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
            this.cachedOwner = null;
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        Entity owner = this.getOwner();
        return new ClientboundAddEntityPacket(this, owner == null ? 0 : owner.getId());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        Entity owner = this.level().getEntity(packet.getData());
        if (owner != null) {
            this.setOwner(owner);
        }
    }

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    @Nullable
    @Override
    public Team getTeam() {
        var owner = getOwner();
        return owner != null ? owner.getTeam() : super.getTeam();
    }

    @Override
    public boolean isControlledByLocalInstance() {
        // Prevent janky motion caused by packets arriving a few ticks late.
        if (getOwner() instanceof Player player) {
            return player.isLocalPlayer();
        } else {
            return false;
        }
    }
}
