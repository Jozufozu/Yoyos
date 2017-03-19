package com.jozufozu.yoyos.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityStickyYoyo extends EntityYoyo {

    public EntityStickyYoyo(World world) {
        super(world);
    }

    public EntityStickyYoyo(World world, EntityPlayer player) {
        super(world, player);
    }

    @Override
    public void onUpdate() {
        if (!this.worldObj.isRemote) {
            this.setFlag(6, this.isGlowing());
        }

        this.onEntityUpdate();

        if (this.thrower != null) {

            this.yoyoStack = this.thrower.getHeldItemMainhand();

            if (this.yoyoStack == null || !(yoyoStack.getItem() instanceof IYoyo)) {
                this.yoyoStack = this.thrower.getHeldItemOffhand();

                if (this.yoyoStack != null)
                    this.hand = EnumHand.OFF_HAND;
            }
            else
                this.hand = EnumHand.MAIN_HAND;

            int currentSlot = this.hand == EnumHand.MAIN_HAND ? this.thrower.inventory.currentItem : -2;

            if (!CASTERS.containsKey(this.thrower) || this.yoyoStack == null || (this.lastSlot != -1 && this.lastSlot != currentSlot) || this.yoyoStack.getMaxDamage() - this.yoyoStack.getItemDamage() <= 0 || !(yoyoStack.getItem() instanceof IYoyo)) {
                this.setDead();
                return;
            }

            if ((!worldObj.isRemote && CASTERS.get(this.thrower) != this)) {
                CASTERS.put(this.thrower, this);
            }

            IYoyo yoyo = (IYoyo) this.yoyoStack.getItem();

            if (this.shouldGetStats) {
                this.maxCool = yoyo.getAttackSpeed(this.yoyoStack);
                this.duration = yoyo.getDuration(this.yoyoStack);
                this.cordLength = yoyo.getLength(this.yoyoStack);
                this.weight = yoyo.getWeight(this.yoyoStack);

                this.shouldGetStats = false;
            }

            this.lastSlot = currentSlot;

            if (duration != -1 && this.ticksExisted >= duration)
                this.forceRetract();

            if (this.thrower.isSneaking() && this.cordLength > 0.5)
                this.cordLength -= 0.1F;


            if (this.isCollided && !this.isRetracting()) {
                this.motionX = 0;
                this.motionY = 0;
                this.motionZ = 0;
            }
            else  {
                //handle position
                Vec3d eyePos = new Vec3d(this.thrower.posX, this.thrower.posY + this.thrower.eyeHeight, + this.thrower.posZ);
                Vec3d lookVec = this.thrower.getLookVec();

                Vec3d target = new Vec3d(eyePos.xCoord + lookVec.xCoord * this.cordLength, eyePos.yCoord + lookVec.yCoord * this.cordLength, eyePos.zCoord + lookVec.zCoord * this.cordLength);

                if (this.isRetracting()) {
                    if (retractionTimeout++ >= MAX_RETRACT_TIME) {
                        this.setDead();
                        return;
                    }
                    target = this.getPlayerHandPos(1);
                }
                else {
                    retractionTimeout = 0;
                    RayTraceResult rayTraceResult = this.getTargetLook(eyePos, target, false);

                    if (rayTraceResult != null)
                        target = rayTraceResult.hitVec;
                }

                Vec3d motionVec = target.subtract(this.posX, this.posY + this.height / 2, this.posZ).scale(Math.min(1 / this.weight, 1.5));

                if (this.inWater)
                    motionVec = motionVec.scale(0.3);

                this.motionX = motionVec.xCoord;
                this.motionY = motionVec.yCoord;
                this.motionZ = motionVec.zCoord;

                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                this.onGround = true; //TODO: This is the only way I've found to get the yoyo to throw out smoothly
            }

            Vec3d thisPos = this.getPositionVector();
            Vec3d throwerPos = this.thrower.getPositionVector();
            double distance = thisPos.distanceTo(throwerPos);
            if (distance > this.cordLength) {
                Vec3d dif = this.getPositionVector().subtract(this.thrower.posX, this.thrower.posY + this.thrower.height / 2, this.thrower.posZ).scale(.01 * (distance - this.cordLength));
                this.thrower.addVelocity(dif.xCoord, dif.yCoord, dif.zCoord);
                this.thrower.fallDistance = 0;

                if (this.isRetracting)
                    this.setDead();
            }
        }
        else
            this.setDead();
    }
}
