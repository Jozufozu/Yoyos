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

package com.jozufozu.yoyos.common.yotools;

import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.common.IYoyo;
import com.jozufozu.yoyos.common.ItemYoyo;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A wrapper class for dealing with yoyoified items
 */
public class YoToolData implements IYoyo
{
    public static final String ROOT_TAG = "YoToolData";
    public static final String LENGTH = "length";
    public static final String DURATION = "duration";
    public static final String WEIGHT = "weight";
    public static final String COLOR = "color";

    public final ItemStack yoTool;

    public float length;
    public float weight;
    public int duration;
    public int color;

    public YoToolData(ItemStack yoTool)
    {
        this.yoTool = yoTool;
        load();
    }

    public void save()
    {
        NBTTagCompound tag = yoTool.getTagCompound();
        if (tag == null) return;

        NBTTagCompound compound = tag.getCompoundTag(ROOT_TAG);

        compound.setFloat(LENGTH, length);
        compound.setFloat(WEIGHT, weight);
        compound.setInteger(DURATION, duration);
        compound.setInteger(COLOR, color);
    }

    public void load()
    {
        NBTTagCompound tag = yoTool.getTagCompound();
        if (tag == null) return;

        NBTTagCompound compound = tag.getCompoundTag(ROOT_TAG);

        length = compound.getFloat(LENGTH);
        weight = compound.getFloat(WEIGHT);
        duration = compound.getInteger(DURATION);
        color = compound.getInteger(COLOR);
    }

    /**
     * Takes a stack and blindly adds all necessary data to allow it to function as a yoyo
     * @param stack
     */
    public static void applyYoToolNBT(ItemStack stack)
    {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setFloat(LENGTH, 6);
        compound.setFloat(WEIGHT, 1.5f);
        compound.setInteger(DURATION, 160);
        compound.setInteger(COLOR, 0xFFFFFF);

        stack.setTagInfo(YoToolData.ROOT_TAG, compound);

        String displayName = stack.getDisplayName();
        stack.getOrCreateSubCompound("display").setString("Name", I18n.format("yoyos.display.prefix") + displayName);
    }

    public static boolean hasData(ItemStack stack)
    {
        NBTTagCompound compound = stack.getTagCompound();

        return compound != null && compound.hasKey(YoToolData.ROOT_TAG);
    }

    @Override
    public float getAttackDamage(ItemStack yoyo)
    {
        return 0;
    }

    @Override
    public float getWeight(ItemStack yoyo)
    {
        return weight;
    }

    @Override
    public float getLength(ItemStack yoyo)
    {
        return length;
    }

    @Override
    public int getDuration(ItemStack yoyo)
    {
        return duration;
    }

    @Override
    public int getAttackSpeed(ItemStack yoyo)
    {
        return 10;
    }

    @Override
    public boolean gardening(ItemStack yoyo)
    {
        return yoyo.getItem() instanceof ItemShears;
    }

    @Override
    public int collecting(ItemStack yoyo)
    {
        return EnchantmentHelper.getEnchantmentLevel(Yoyos.COLLECTING, yoyo);
    }

    @Override
    public void damageItem(ItemStack yoyo, int amount, EntityLivingBase player)
    {
        yoyo.damageItem(1, player);
    }

    @Override
    public void attack(ItemStack yoyo, EntityPlayer player, EnumHand hand, EntityYoyo yoyoEntity, Entity targetEntity)
    {
        boolean attack = true;
        if (targetEntity instanceof EntityLivingBase)
        {
            attack = !yoyo.getItem().itemInteractionForEntity(yoyo, player, ((EntityLivingBase) targetEntity), hand);

            if (attack && targetEntity instanceof EntityAgeable)
                attack = !((EntityAgeable) targetEntity).processInteract(player, hand);
        }

        if (attack)
            ItemYoyo.attackEntity(targetEntity, yoyo, player, yoyoEntity, hand);
    }

    @Override
    public boolean interactsWithBlocks(ItemStack yoyo)
    {
        Item itemType = yoyo.getItem();

        return itemType instanceof ItemShears || itemType instanceof ItemTool;
    }

    @Override
    public void blockInteraction(ItemStack yoyo, EntityPlayer player, World world, BlockPos pos, IBlockState state, Block block, EntityYoyo yoyoEntity)
    {
        Item itemType = yoyo.getItem();

        if (itemType instanceof ItemShears)
            ItemYoyo.garden(yoyo, this, player, world, pos, state, block, yoyoEntity);

        if (itemType instanceof ItemTool)
        {
            ItemTool tool = (ItemTool) itemType;

            if (tool.getDestroySpeed(yoyo, state) > 1.0)
            {
                if (block.removedByPlayer(state, world, pos, player, true))
                {
                    yoyoEntity.markBlockForDropGathering(pos);

                    block.harvestBlock(world, player, pos, state, world.getTileEntity(pos), yoyo);
                    block.breakBlock(world, pos, state);

                    if (!player.isCreative()) yoyo.damageItem(1, player);

                    world.playSound(null, pos, block.getSoundType(state, world, pos, yoyoEntity).getBreakSound(), SoundCategory.BLOCKS, 1, 1);
                    world.playEvent(2001, pos.toImmutable(), Block.getStateId(state));

                    yoyoEntity.forceRetract();
                }
            }
        }
    }
}
