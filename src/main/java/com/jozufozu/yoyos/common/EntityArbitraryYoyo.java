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

import com.jozufozu.yoyos.common.yotools.YoToolData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityArbitraryYoyo extends EntityYoyo
{
    public EntityArbitraryYoyo(World world)
    {
        super(world);
    }

    public EntityArbitraryYoyo(World world, EntityPlayer player)
    {
        super(world, player);
    }

    @Nullable
    @Override
    protected IYoyo checkAndGetYoyoObject()
    {
        yoyoStack = thrower.getHeldItemMainhand();

        if (yoyoStack == ItemStack.EMPTY || !YoToolData.hasData(yoyoStack))
        {
            yoyoStack = thrower.getHeldItemOffhand();

            if (yoyoStack != ItemStack.EMPTY)
                hand = EnumHand.OFF_HAND;
        }
        else hand = EnumHand.MAIN_HAND;

        int currentSlot = hand == EnumHand.MAIN_HAND ? thrower.inventory.currentItem : -2;

        if (!CASTERS.containsKey(thrower) || !YoToolData.hasData(yoyoStack) || (lastSlot != -1 && lastSlot != currentSlot))
        {
            setDead();
            return null;
        }

        if (yoyoStack.isItemStackDamageable() && yoyoStack.getMaxDamage() < yoyoStack.getItemDamage())
        {
            setDead();
            return null;
        }

        if ((!world.isRemote && CASTERS.get(thrower) != this))
        {
            CASTERS.put(thrower, this);
        }

        IYoyo yoyo = new YoToolData(yoyoStack);

        getStats(yoyo);

        lastSlot = currentSlot;

        return yoyo;
    }
}
