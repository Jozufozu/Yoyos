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

package com.jozufozu.yoyos.common.api

import com.jozufozu.yoyos.common.YoyoEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

interface IYoyo {
    /**
     * The weight of the yoyo. Used to calculate movement speed.
     * TODO(1.13+): Change this to return the actual movement speed given a distance to a target
     * @param yoyo The [ItemStack] that was used to launch the yoyo
     */
    fun getWeight(yoyo: ItemStack): Double

    /**
     * The maximum distance the yoyo can be from the player.
     * @param yoyo The [ItemStack] that was used to launch the yoyo
     */
    fun getLength(yoyo: ItemStack): Double

    /**
     * The maximum number of ticks the yoyo can stay out.
     * @param yoyo The [ItemStack] that was used to launch the yoyo
     */
    fun getDuration(yoyo: ItemStack): Int

    /**
     * The minimum time inbetween attacks made by the yoyo.
     * @param yoyo The [ItemStack] that was used to launch the yoyo
     */
    fun getAttackInterval(yoyo: ItemStack): Int

    /**
     * The maximum number of *ITEMS* that the yoyo can hold.
     * @param yoyo Stack that was used to launch the yoyo
     */
    fun getMaxCollectedDrops(yoyo: ItemStack): Int

    /**
     * Should damage the yoyo by amount, also taking into account whether the player is creative or not,
     * and any [net.minecraft.enchantment.Enchantments] that might be applied.
     * @param yoyo Stack that was used to launch the yoyo.
     * @param hand The hand the yoyo is held in.
     * @param amount Amount of damage to be done to the yoyo.
     * @param entity Entity wielding the yoyo.
     */
    fun <T : LivingEntity> damageItem(yoyo: ItemStack, hand: Hand, amount: Int, entity: T)

    /**
     * Do things that should happen when the yoyo entity touches another entity here.
     * @param yoyo Stack that was used to launch the yoyo.
     * @param player Player wielding the yoyo.
     * @param hand Hand the yoyo is held in.
     * @param yoyoEntity Yoyo entity doing the touching.
     * @param targetEntity Entity the yoyo is touching.
     */
    fun entityInteraction(yoyo: ItemStack, player: PlayerEntity, hand: Hand, yoyoEntity: YoyoEntity, targetEntity: Entity)

    /**
     * Does the yoyo entity interact with blocks at all?
     * @param yoyo The [ItemStack] that was used to launch the yoyo.
     * @return Whether or not to calculate and handle block collision actions.
     */
    fun interactsWithBlocks(yoyo: ItemStack): Boolean

    /**
     * Do things that should happen when the yoyo entity touches blocks here.
     * @param yoyo Stack that was used to launch the yoyo.
     * @param player Player wielding the yoyo.
     * @param world World in which the interaction takes place.
     * @param pos Position of the block being touched.
     * @param state State of the block being touched.
     * @param block Block being touched.
     * @param yoyoEntity Yoyo entity doing the touching.
     */
    fun blockInteraction(yoyo: ItemStack, player: PlayerEntity, world: World, pos: BlockPos, state: BlockState, block: Block, yoyoEntity: YoyoEntity)

    /**
     * Other things that should happen in the yoyo entity's ticking
     * @param yoyo Stack that was used to launch the yoyo.
     * @param yoyoEntity Yoyo entity being updated.
     */
    fun onUpdate(yoyo: ItemStack, yoyoEntity: YoyoEntity) {}

    /**
     * A multiplier to the yoyo entity's movement speed when it is in water.
     * @param yoyo Stack that was used to launch the yoyo.
     */
    fun getWaterMovementModifier(yoyo: ItemStack): Float {
        return 0.3f
    }

    /**
     * The color the cord should be.
     * @param yoyo Stack that was used to launch the yoyo.
     * @param ticks The exact amount of ticks (and partial ticks) that the yoyo has existed.
     */
    @OnlyIn(Dist.CLIENT)
    fun getCordColor(yoyo: ItemStack, ticks: Float): Int {
        return 0xDDDDDD
    }

    /**
     * Primarily used for gardening yoyos. The orientation the yoyos should be spinning in.
     * @param yoyo Stack that was used to launch the yoyo.
     */
    @OnlyIn(Dist.CLIENT)
    fun getRenderOrientation(yoyo: ItemStack): RenderOrientation {
        return RenderOrientation.Vertical
    }
}
