/*
 * Copyright (c) 2018 Jozsef Augusztiny
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.jozufozu.yoyos.compat;

import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.network.MessageAcquireTarget;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.helper.Vector3;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class EntityChaserYoyo extends EntityYoyo
{
    private Entity target;

    private int ticksChasing;

    public EntityChaserYoyo(World world)
    {
        super(world);
    }

    public EntityChaserYoyo(World world, EntityPlayer player)
    {
        super(world, player);
    }

    public void setTargetEntity(@Nullable Entity target)
    {
        this.target = target;
    }

    public void forgetTargetEntity()
    {
        target = null;

        if (!world.isRemote)
        {
            ticksChasing = 0;
            YoyoNetwork.INSTANCE.sendToAllTracking(new MessageAcquireTarget.MessageTargetUpdate(this), this);
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (target != null)
            particles();

        if (!world.isRemote)
        {
            if (target != null)
            {
                if (ticksChasing++ > 20) forgetTargetEntity();
            }
            else if (ticksChasing > 0)
            {
                ticksChasing = 0;
            }
        }
    }

    @Override
    public void interactWithEntity(Entity entity)
    {
        if (attackCool >= maxCool && entity == target) forgetTargetEntity();

        super.interactWithEntity(entity);
    }

    @Override
    public Vec3d getTarget()
    {
        if (target != null && !isRetracting && !target.isDead)
            return new Vec3d(target.posX, target.posY + target.height * 0.5, target.posZ);

        return super.getTarget();
    }

    @Override
    protected void handlePlayerPulling()
    {
        if (target == null) super.handlePlayerPulling();
    }

    public void acquireTargetEntity()
    {
        if (isRetracting) return;
        if (!ManaItemHandler.requestManaExact(yoyoStack, thrower, 200, false)) return;

        AxisAlignedBB searchBox = new AxisAlignedBB(thrower.posX - maxLength, thrower.posY - maxLength, thrower.posZ - maxLength, thrower.posX + maxLength, thrower.posY + maxLength, thrower.posZ + maxLength);

        Vec3d lookVec = thrower.getLookVec();

        Entity lastTarget = target;
        Entity bestTarget = null;

        double maxCloseness = 0;

        List<Entity> chaseable = world.getEntitiesInAABBexcluding(this, searchBox, entity ->
                (isCollecting() && entity instanceof EntityItem) || entity instanceof EntityLivingBase
        );

        for (Entity testEntity : chaseable)
        {
            if (testEntity.isDead || testEntity == thrower) continue;

            if (testEntity instanceof EntityLivingBase && ((EntityLivingBase) testEntity).deathTime > 0) continue;

            Vec3d testVec = new Vec3d(testEntity.posX - thrower.posX, testEntity.posY - thrower.posY + testEntity.height * 0.5, testEntity.posZ - thrower.posZ);

            if (testVec.lengthSquared() > maxLength * maxLength) continue; // It's farther away than we can reach

            double closeness = testVec.normalize().dotProduct(lookVec);

            // Priority multipliers
            if (testEntity instanceof EntityAnimal) closeness *= 0.5;
            else if (testEntity instanceof EntityItem) closeness *= 1.5;
            else if (!(testEntity instanceof EntityAmbientCreature))
                closeness *= 2.0; // If it's not an animal or an item, it's probably a monster

            if (closeness > maxCloseness)
            {
                if (canEntityBeSeen(testEntity) && thrower.canEntityBeSeen(testEntity))
                {
                    maxCloseness = closeness;
                    bestTarget = testEntity;
                }
            }
        } // Here, we take the entity most closely aligned with the player's view

        if (bestTarget != lastTarget)
        {
            target = bestTarget;
            ManaItemHandler.requestManaExact(yoyoStack, thrower, 200, true);
            world.playSound(null, thrower.posX, thrower.posY + thrower.eyeHeight, thrower.posZ, Yoyos.YOYO_CHASE, SoundCategory.PLAYERS, 0.4F, 1.2F + world.rand.nextFloat() * 0.6F);

            YoyoNetwork.INSTANCE.sendToAllTracking(new MessageAcquireTarget.MessageTargetUpdate(this, bestTarget), this);
        }
    }

    private void particles()
    {
        if (this.world.isRemote)
        {
            Color color = new Color(2162464);

            float r = (float) color.getRed() / 255.0F;
            float g = (float) color.getGreen() / 255.0F;
            float b = (float) color.getBlue() / 255.0F;
            float osize = 0.4f;
            float size;

            boolean monocle = Botania.proxy.isClientPlayerWearingMonocle();
            if (monocle)
            {
                Botania.proxy.setWispFXDepthTest(false);
            }

            if (ConfigHandler.subtlePowerSystem)
            {
                Botania.proxy.wispFX(this.posX, this.posY + height * 0.5, this.posZ, r, g, b, 0.1F * osize, (float) (Math.random() - 0.5D) * 0.02F, (float) (Math.random() - 0.5D) * 0.02F, (float) (Math.random() - 0.5D) * 0.01F);
            }
            else
            {
                float or = r;
                float og = g;
                float ob = b;
                double luminance = 0.2126D * (double) r + 0.7152D * (double) g + 0.0722D * (double) b;
                double savedPosX = this.posX;
                double savedPosY = this.posY;
                double savedPosZ = this.posZ;
                Vector3 currentPos = Vector3.fromEntity(this);
                Vector3 oldPos = new Vector3(this.prevPosX, this.prevPosY, this.prevPosZ);
                Vector3 diffVec = oldPos.subtract(currentPos);
                Vector3 diffVecNorm = diffVec.normalize();
                double distance = 0.095D;

                do
                {
                    if (luminance < 0.1D)
                    {
                        r = or + (float) Math.random() * 0.125F;
                        g = og + (float) Math.random() * 0.125F;
                        b = ob + (float) Math.random() * 0.125F;
                    }

                    size = osize + ((float) Math.random() - 0.5F) * 0.065F + (float) Math.sin((double) (new Random(this.entityUniqueID.getMostSignificantBits())).nextInt(9001)) * 0.4F;
                    Botania.proxy.wispFX(this.posX, this.posY + height * 0.5, this.posZ, r, g, b, 0.2F * size, (float) (-this.motionX) * 0.01F, (float) (-this.motionY) * 0.01F, (float) (-this.motionZ) * 0.01F);
                    this.posX += diffVecNorm.x * distance;
                    this.posY += diffVecNorm.y * distance;
                    this.posZ += diffVecNorm.z * distance;
                    currentPos = Vector3.fromEntity(this);
                    diffVec = oldPos.subtract(currentPos);
                }
                while (!this.getEntityData().hasKey("orbit") && Math.abs(diffVec.mag()) > distance);

                Botania.proxy.wispFX(this.posX, this.posY + height * 0.5, this.posZ, or, og, ob, 0.1F * size, (float) (Math.random() - 0.5D) * 0.06F, (float) (Math.random() - 0.5D) * 0.06F, (float) (Math.random() - 0.5D) * 0.06F);
                this.posX = savedPosX;
                this.posY = savedPosY;
                this.posZ = savedPosZ;
            }

            if (monocle)
            {
                Botania.proxy.setWispFXDepthTest(true);
            }
        }
    }

    /**
     * returns true if the entity provided in the argument can be seen. (Raytrace)
     */
    public boolean canEntityBeSeen(Entity entityIn)
    {
        return this.world.rayTraceBlocks(new Vec3d(posX, posY + height * 0.5, posZ), new Vec3d(entityIn.posX, entityIn.posY + (double) entityIn.getEyeHeight(), entityIn.posZ), false, true, false) == null;
    }
}
