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

package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.Yoyos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityStickyYoyo extends EntityYoyo
{
    private static final DataParameter<Byte> REEL_DIRECTION = EntityDataManager.createKey(EntityStickyYoyo.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> STUCK = EntityDataManager.createKey(EntityStickyYoyo.class, DataSerializers.BOOLEAN);

    private int stuckSince;

    public EntityStickyYoyo(World world)
    {
        super(world);
    }

    public EntityStickyYoyo(World world, EntityPlayer player, EnumHand hand)
    {
        super(world, player, hand);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.getDataManager().register(REEL_DIRECTION, (byte) 0);
        this.getDataManager().register(STUCK, false);
    }

    public int getReelDirection()
    {
        return this.getDataManager().get(REEL_DIRECTION);
    }

    public void setReelDirection(byte reelDirection)
    {
        this.getDataManager().set(REEL_DIRECTION, reelDirection);
        this.getDataManager().setDirty(REEL_DIRECTION);
    }

    public boolean isStuck()
    {
        return this.getDataManager().get(STUCK);
    }

    public void setStuck(boolean stuck)
    {
        this.getDataManager().set(STUCK, stuck);
        this.getDataManager().setDirty(STUCK);
    }

    public void changeLength(float amount)
    {
        setCurrentLength(Math.min(getCurrentLength() + amount, getMaxLength()));
    }

    @Override
    public void onUpdate()
    {
        if (!world.isRemote)
        {
            setFlag(6, isGlowing());
        }

        onEntityUpdate();

        if (thrower != null && !thrower.isDead)
        {
            yoyo = checkAndGetYoyoObject();

            if (yoyo == null) return;

            double dx = thrower.posX - posX;
            double dy = (thrower.posY + thrower.eyeHeight) - (posY + height * 0.5);
            double dz = thrower.posZ - posZ;
            double distanceSqr = dx * dx + dy * dy + dz * dz;

            if (getReelDirection() < 0 && getCurrentLength() > 0.1 && distanceSqr < getCurrentLength() * getCurrentLength() + 8)
                changeLength(-0.5f);

            if (getReelDirection() > 0 && getCurrentLength() < getMaxLength())
            {
                if (distanceSqr < getCurrentLength() * getCurrentLength() + 2)
                    changeLength(0.1f);
                else
                    setCurrentLength(MathHelper.sqrt(distanceSqr));
            }

            if (!isRetracting() && !world.getCollisionBoxes(this, getEntityBoundingBox().grow(0.1)).isEmpty())
            {
                motionX = 0;
                motionY = 0;
                motionZ = 0;

                if (!isStuck())
                {
                    stuckSince = ticksExisted;
                    setCurrentLength(MathHelper.sqrt(distanceSqr));
                    world.playSound(null, posX, posY, posZ, Yoyos.YOYO_STICK, SoundCategory.PLAYERS, 0.7f, 3.0f);
                    yoyo.damageItem(getYoyoStack(), 1, thrower);
                }
                setStuck(true);

                handlePlayerPulling();
            }
            else
            {
                if (isRetracting() && isStuck()) setDead();

                if (decrementRemainingTime() == 0)
                    forceRetract();

                updateMotion();

                moveAndCollide();

                if (!world.isRemote && doesBlockInteraction())
                    worldInteraction();

                setStuck(false);
            }

            if (isCollecting())
                updateCapturedDrops();

            resetOrIncrementAttackCooldown();
        }
        else setDead();
    }

    @Override
    public float getRotation(int age, float partialTicks)
    {
        return isStuck() ? super.getRotation(stuckSince, 0) : super.getRotation(age - stuckSince, partialTicks);
    }
}
