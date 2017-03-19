package com.jozufozu.yoyos.common;

import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityStickyYoyo extends EntityYoyo {

    private final List<Entity> carrying = new ArrayList<>();

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
                this.chordLength = yoyo.getLength(this.yoyoStack);
                this.weight = yoyo.getWeight(this.yoyoStack);

                this.shouldGetStats = false;
            }

            this.lastSlot = currentSlot;

            if (duration != -1 && this.ticksExisted >= duration)
                this.forceRetract();

            //handle position
            Vec3d eyePos = new Vec3d(this.thrower.posX, this.thrower.posY + this.thrower.eyeHeight, + this.thrower.posZ);
            Vec3d lookVec = this.thrower.getLookVec();

            Vec3d target = new Vec3d(eyePos.xCoord + lookVec.xCoord * this.chordLength, eyePos.yCoord + lookVec.yCoord * this.chordLength, eyePos.zCoord + lookVec.zCoord * this.chordLength);

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

            Vec3d motionVec = target.subtract(this.posX, this.posY + this.height / 2, this.posZ).scale(Math.min(1/this.weight, 1.5));

            if (this.inWater)
                motionVec = motionVec.scale(0.3);

            this.motionX = motionVec.xCoord;
            this.motionY = motionVec.yCoord;
            this.motionZ = motionVec.zCoord;

            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.onGround = true; //TODO: This is the only way I've found to get the yoyo to throw out smoothly

            Vec3d thisPos = this.getPositionVector();
            Vec3d throwerPos = this.thrower.getPositionVector();
            double distance = thisPos.distanceTo(throwerPos);
            if (distance > this.chordLength + 2) {
                Vec3d dif = this.getPositionVector().subtract(this.thrower.posX, this.thrower.posY + this.thrower.height / 2, this.thrower.posZ).scale(.01 * (distance - this.chordLength - 2));
                this.thrower.addVelocity(dif.xCoord, dif.yCoord, dif.zCoord);
                this.thrower.fallDistance = 0;
            }

            //Pick up stuff
            for (Entity entity : this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expandXyz(0.4))) {
                if (entity != this.thrower) {
                    if (!carrying.contains(entity)) {
                        carrying.add(entity);
                    }
                }
                else if (this.isRetracting())
                    this.setDead();
            }

            for (Entity entity : carrying) {
                entity.fallDistance = 0;

                Vec3d entityMotionVec = thisPos.subtract(entity.posX, entity.posY + entity.height / 2, entity.posZ);

                if (entity.isInWater())
                    entityMotionVec = entityMotionVec.scale(0.3);

                entity.motionX = entityMotionVec.xCoord;
                entity.motionY = entityMotionVec.yCoord;
                entity.motionZ = entityMotionVec.zCoord;
            }
        }
        else
            this.setDead();
    }

    @Override
    public RayTraceResult getTargetLook(Vec3d vec3d, Vec3d vec3d2, boolean stopOnLiquid) {
        double distance = vec3d.distanceTo(vec3d2);
        RayTraceResult objectMouseOver = rayTraceBlocks(this.worldObj, vec3d, vec3d2, stopOnLiquid);
        boolean flag = false;
        double d1 = distance;

        if (distance > 3.0D)
        {
            flag = true;
        }

        if (objectMouseOver != null)
        {
            d1 = objectMouseOver.hitVec.distanceTo(vec3d);
        }

        Vec3d vec3d1 = this.thrower.getLook(1);
        Entity pointedEntity = null;
        Vec3d vec3d3 = null;
        List<Entity> list = this.worldObj.getEntitiesInAABBexcluding(this.thrower, this.thrower.getEntityBoundingBox().addCoord(vec3d1.xCoord * distance, vec3d1.yCoord * distance, vec3d1.zCoord * distance).expand(1.0D, 1.0D, 1.0D),
                Predicates.and(EntitySelectors.NOT_SPECTATING, entity -> entity != null && entity.canBeCollidedWith() && !carrying.contains(entity)));
        double d2 = d1;

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = list.get(j);
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expandXyz((double)entity1.getCollisionBorderSize());
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

            if (axisalignedbb.isVecInside(vec3d))
            {
                if (d2 >= 0.0D)
                {
                    pointedEntity = entity1;
                    vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
                    d2 = 0.0D;
                }
            }
            else if (raytraceresult != null)
            {
                double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                if (d3 < d2 || d2 == 0.0D)
                {
                    if (entity1.getLowestRidingEntity() == this.thrower.getLowestRidingEntity() && !this.thrower.canRiderInteract())
                    {
                        if (d2 == 0.0D)
                        {
                            pointedEntity = entity1;
                            vec3d3 = raytraceresult.hitVec;
                        }
                    }
                    else
                    {
                        pointedEntity = entity1;
                        vec3d3 = raytraceresult.hitVec;
                        d2 = d3;
                    }
                }
            }
        }

        if (pointedEntity != null && flag)
        {
            pointedEntity = null;
            objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS, vec3d3, null, new BlockPos(vec3d3));
        }

        if (pointedEntity != null && objectMouseOver == null)
        {
            objectMouseOver = new RayTraceResult(pointedEntity, vec3d3);
        }

        return objectMouseOver;
    }

}
