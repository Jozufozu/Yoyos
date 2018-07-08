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
