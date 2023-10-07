package com.jozufozu.yoyos.common.api

import com.jozufozu.yoyos.common.YoyoEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

typealias EntityInteraction = (ItemStack, PlayerEntity, Hand, YoyoEntity, Entity) -> Boolean
typealias BlockInteraction = (ItemStack, PlayerEntity, BlockPos, BlockState, Block, YoyoEntity) -> Boolean

typealias YoyoFactory = (World, PlayerEntity, Hand) -> YoyoEntity