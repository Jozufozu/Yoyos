package com.jozufozu.yoyos.core;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.jozufozu.yoyos.Constants;
import com.jozufozu.yoyos.core.network.RetractYoyoPacket;
import com.jozufozu.yoyos.core.network.YoyoTracker;
import com.jozufozu.yoyos.infrastructure.util.EntityDataHolder;
import com.jozufozu.yoyos.infrastructure.util.YoyoUtil;
import com.jozufozu.yoyos.platform.Services;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;

public class Yoyo extends Entity implements TraceableEntity {
    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(Yoyo.class, EntityDataSerializers.ITEM_STACK);

    @Nullable
    private LivingEntity owner;
    private final Controller controller = new Controller();

    private InteractionHand hand;
    private final EntityDataHolder<ItemStack> yoyoStack = new EntityDataHolder<>(this, DATA_ITEM_STACK);

    public Yoyo(EntityType<? extends Yoyo> entityType, Level level) {
        super(entityType, level);
    }

    public Yoyo(Level level) {
        super(AllThings.YOYO_ENTITY_TYPE.get(), level);
    }

    public void sendRetract() {
        if (level().isClientSide) {
            return;
        }

        controller.signalRetract();

        Services.NETWORK.sendToAllTracking(this, AllThings.RETRACT_PACKET.get(), new RetractYoyoPacket(this, true));
    }

    @Override
    public void tick() {
        if (!level().isClientSide && shouldDiscard()) {
            discard();
            return;
        }

        super.tick();

        getController().tick(this);
    }

    @Override
    public void remove(RemovalReason removalReason) {
        super.remove(removalReason);
        if (!level().isClientSide && owner != null) {
            YoyoTracker.on(owner)
                .setYoyoInHand(hand, null);
        }
    }

    public Vector3d getCenterPos(Vector3d dest) {
        return YoyoUtil.storeEntityCenter(dest, this);
    }

    public void setCenterPos(Vector3dc centerPos) {
        setPos(centerPos.x(), centerPos.y() - getBbHeight() / 2, centerPos.z());
    }

    public Controller getController() {
        return controller;
    }

    /**
     * @return true if this yoyo has reached an invalid state and should be removed.
     */
    private boolean shouldDiscard() {
        return getYoyoStack().isEmpty() || getOwner() == null || !isStillHeld();
    }

    private boolean isStillHeld() {
        var owner = getOwner();
        if (owner != null) {
            return getYoyoStack().equals(owner.getMainHandItem()) || getYoyoStack().equals(owner.getOffhandItem());
        }
        return false;
    }

    public void setYoyoStack(ItemStack yoyoStack) {
        this.yoyoStack.set(yoyoStack);
    }

    public ItemStack getYoyoStack() {
        return this.yoyoStack.get();
    }

    public void onThrow(LivingEntity owner, InteractionHand hand) {
        setOwner(owner);
        setYoyoStack(owner.getItemInHand(hand));

        getController().onThrow(this, owner);

        this.hand = hand;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        if (owner != null) {
            this.owner = owner;
        }
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        return owner;
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {

    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        var owner = this.getOwner();
        return new ClientboundAddEntityPacket(this, owner == null ? 0 : owner.getId());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        var ownerId = packet.getData();
        var maybeOwner = level().getEntity(ownerId);
        if (maybeOwner instanceof LivingEntity living) {
            this.setOwner(living);
        } else {
            Constants.LOG.error("Yoyo created with invalid owner {} (id: {})", maybeOwner, ownerId);
            discard();
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

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    public Predicate<Entity> getCollisionPredicate() {
        return EntitySelector.NO_SPECTATORS.and(entity -> !(entity instanceof Yoyo));
    }

    public void getOwnerEyePos(Vector3d eyePos) {
        var owner = getOwner();
        if (owner == null) {
            return;
        }

        var pos = owner.position();
        eyePos.set(pos.x, pos.y + owner.getEyeHeight(), pos.z);
    }

    public boolean isEntityOwnerOrOwnersMount(Entity other) {
        var owner = getOwner();

        if (owner == null) {
            return false;
        }

        return other == owner || other.isPassengerOfSameVehicle(owner);
    }
}
