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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IYoyo
{
    float getWeight(ItemStack yoyo);
    
    float getLength(ItemStack yoyo);
    
    int getDuration(ItemStack yoyo);
    
    int getAttackSpeed(ItemStack yoyo);
    
    boolean gardening(ItemStack yoyo);
    
    boolean collecting(ItemStack yoyo);
    
    void damageItem(ItemStack yoyo, EntityLivingBase player);
    
    void attack(ItemStack yoyo, EntityPlayer player, EnumHand hand, EntityYoyo yoyoEntity, Entity targetEntity);

    boolean interactsWithBlocks(ItemStack yoyo);

    void blockInteraction(ItemStack yoyo, EntityPlayer player, World world, BlockPos pos, IBlockState state, Block block, EntityYoyo yoyoEntity);
    
    @SideOnly(Side.CLIENT)
    default int getCordColor(ItemStack yoyo)
    {
        return 0xDDDDDD;
    }

    @SideOnly(Side.CLIENT)
    default int getLeftColor(ItemStack yoyo)
    {
        return 0xDDDDDD;
    }

    @SideOnly(Side.CLIENT)
    default int getRightColor(ItemStack yoyo)
    {
        return 0xDDDDDD;
    }

    @SideOnly(Side.CLIENT)
    default int getAxleColor(ItemStack yoyo)
    {
        return 0xDDDDDD;
    }
}
