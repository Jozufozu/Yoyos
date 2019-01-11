package com.jozufozu.yoyos.common;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface BlockInteraction
{
    void doInteraction(IYoyo yoyo, ItemStack yoyoStack, EntityPlayer player, World world, BlockPos pos, IBlockState state, Block block, EntityYoyo yoyoEntity);
}
