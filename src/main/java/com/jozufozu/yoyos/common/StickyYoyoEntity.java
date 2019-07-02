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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class StickyYoyoEntity extends YoyoEntity
{
    public StickyYoyoEntity(EntityType<?> type, World world)
    {
        super(type, world);
    }

    public StickyYoyoEntity(World world)
    {
        super(Yoyos.STICKY_YOYO_ENTITY_TYPE, world);
    }

    public StickyYoyoEntity(EntityType<?> type, World world, PlayerEntity player)
    {
        super(type, world, player);
    }

    public StickyYoyoEntity(World world, PlayerEntity player)
    {
        super(Yoyos.STICKY_YOYO_ENTITY_TYPE, world, player);
    }

    private boolean stuck = false;
    private int stuckSince = 0;

    private int reelDirection;

    public void setReelDirection(int reelDirection)
    {
        this.reelDirection = reelDirection;
    }

    @Override
    public void tick()
    {
        if (!world.isClient)
        {
            setFlag(6, isGlowing());
        }

        baseTick();

        if (thrower != null && !thrower.removed)
        {
            yoyo = checkAndGetYoyoObject();

            if (yoyo == null) return;

            double dx = thrower.x - x;
            double dy = (thrower.y + thrower.getEyeHeight(thrower.getPose())) - (y + getHeight() * 0.5);
            double dz = thrower.z - z;
            double distanceSqr = dx * dx + dy * dy + dz * dz;

            if (reelDirection < 0 && cordLength > 0.1 && distanceSqr < cordLength * cordLength + 8)
                cordLength -= 0.5;

            if (reelDirection > 0 && cordLength < maxLength)
            {
                if (distanceSqr < cordLength * cordLength + 2)
                    cordLength += 0.1;
                else
                    cordLength = MathHelper.sqrt(distanceSqr);
            }

            if (!isRetracting() && !world.doesNotCollide(getBoundingBox().expand(0.1)))
            {
                setVelocity(Vec3d.ZERO);

                if (!stuck)
                {
                    stuckSince = timeoutCounter;
                    cordLength = MathHelper.sqrt(distanceSqr);
                    world.playSound(null, x, y, z, Yoyos.YOYO_STICK, SoundCategory.PLAYERS, 0.7f, 3.0f);
                    yoyo.damageItem(yoyoStack, 1, thrower);
                }
                stuck = true;

                handlePlayerPulling();
            }
            else
            {
                if (isRetracting() && stuck) remove();

                if (duration >= 0 && ++timeoutCounter >= duration)
                    forceRetract();

                updateMotion();

                moveAndCollide();

                if (!world.isClient && interactsWithBlocks)
                    worldInteraction();

                stuck = false;
            }

            if (isCollecting())
                updateCapturedDrops();

            resetOrIncrementAttackCooldown();
        }
        else remove();
    }

    @Override
    public float getRotation(int age, float partialTicks)
    {
        return stuck ? super.getRotation(stuckSince, 0) : super.getRotation(age - stuckSince, partialTicks);
    }
}
