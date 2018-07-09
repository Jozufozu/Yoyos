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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityStickyYoyo extends EntityYoyo
{
    public EntityStickyYoyo(World world)
    {
        super(world);
    }

    public EntityStickyYoyo(World world, EntityPlayer player)
    {
        super(world, player);
    }

    private boolean stuck = false;
    private int stuckSince = 0;

    private int reelTime;

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
            IYoyo yoyo = checkAndGetYoyoObject();

            if (yoyo == null) return;

            if (thrower.isSneaking() && cordLength > 0.1)
            {
                cordLength -= 0.01F * reelTime;

                reelTime++;
            }
            else
            {
                reelTime = 0;
            }

            if (!world.getCollisionBoxes(this, getEntityBoundingBox().grow(0.1)).isEmpty() && !isRetracting())
            {
                motionX = 0;
                motionY = 0;
                motionZ = 0;

                if (!stuck)
                {
                    stuckSince = ticksExisted;
                    cordLength = (float) (new Vec3d(thrower.posX - posX, thrower.posY - posY, thrower.posZ - posZ)).lengthVector();
                    world.playSound(null, posX, posY, posZ, Yoyos.YOYO_STICK, SoundCategory.PLAYERS, 0.7f, 3.0f);
                }
                stuck = true;
            }
            else
            {
                if (duration >= 0 && ticksExisted >= duration) forceRetract();
                updateMotion();

                moveAndCollide(yoyo);

                stuck = false;
            }

            if (!world.isRemote && gardening)
                worldInteraction(yoyo);

            handleSwing();
            if (collecting)
                updateCapturedDrops();
        }
        else setDead();
    }

    @Override
    public float getRotation(int age, float partialTicks)
    {
        return stuck ? super.getRotation(stuckSince, 0) : super.getRotation(age - stuckSince, partialTicks);
    }
}
