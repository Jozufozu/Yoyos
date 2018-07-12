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
import com.jozufozu.yoyos.network.MessageReelState;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Yoyos.MODID)
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
    private int reelDirection;

    private static int reel;
    private static int lastReel;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onTickWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) return;

        reel = 0;

        Minecraft mc = Minecraft.getMinecraft();
        EntityYoyo yoyo = EntityYoyo.CASTERS.get(mc.player);

        if (yoyo instanceof EntityStickyYoyo)
        {
            if (mc.gameSettings.keyBindJump.isKeyDown()) reel += 1;
            if (mc.gameSettings.keyBindSneak.isKeyDown()) reel -= 1;

            int abs = Math.abs(reel);
            if (abs > 1)
                reel /= abs;

            if (reel != lastReel)
            {
                YoyoNetwork.INSTANCE.sendToServer(new MessageReelState(((EntityStickyYoyo) yoyo), reel));
            }
        }

        lastReel = reel;
    }

    public void setReelDirection(int reelDirection)
    {
        this.reelDirection = reelDirection;
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

            if (reelDirection < 0 && cordLength > 0.1)
            {
                cordLength -= 0.05 * reelTime;

                if (reelTime < 10)
                    reelTime++;
            }
            else
            {
                reelTime = 0;
            }

            if (reelDirection > 0 && cordLength < maxLength)
                cordLength += 0.1;

            if (!world.getCollisionBoxes(this, getEntityBoundingBox().grow(0.1)).isEmpty() && !isRetracting())
            {
                motionX = 0;
                motionY = 0;
                motionZ = 0;

                if (!stuck)
                {
                    stuckSince = ticksExisted;
                    double dx = thrower.posX - posX;
                    double dy = thrower.posY - posY;
                    double dz = thrower.posZ - posZ;
                    cordLength = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
                    world.playSound(null, posX, posY, posZ, Yoyos.YOYO_STICK, SoundCategory.PLAYERS, 0.7f, 3.0f);
                }
                stuck = true;
            }
            else
            {
                if (duration >= 0 && ticksExisted >= duration)
                    forceRetract();

                updateMotion();

                moveAndCollide();

                if (!world.isRemote && interactsWithBlocks)
                    worldInteraction();

                stuck = false;
            }

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
