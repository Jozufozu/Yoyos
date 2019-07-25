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

package com.jozufozu.yoyos.common.api;

import com.jozufozu.yoyos.common.RenderOrientation;
import com.jozufozu.yoyos.common.YoyoEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IYoyo
{
    /**
     * The weight of the yoyo. Used to calculate movement speed.
     * TODO(1.13+): Change this to return the actual movement speed given a distance to a target
     * @param yoyo The {@link ItemStack} that was used to launch the yoyo
     */
    float getWeight(ItemStack yoyo);

    /**
     * The maximum distance the yoyo can be from the player.
     * @param yoyo The {@link ItemStack} that was used to launch the yoyo
     */
    float getLength(ItemStack yoyo);

    /**
     * The maximum number of ticks the yoyo can stay out.
     * @param yoyo The {@link ItemStack} that was used to launch the yoyo
     */
    int getDuration(ItemStack yoyo);

    /**
     * The minimum time in between attacks made by the yoyo.
     * @param yoyo The {@link ItemStack} that was used to launch the yoyo
     */
    int getAttackInterval(ItemStack yoyo);

    /**
     * The maximum number of <em>ITEMS</em> that the yoyo can hold.
     * @param yoyo Stack that was used to launch the yoyo
     */
    int getMaxCollectedDrops(ItemStack yoyo);

    /**
     * Should damage the yoyo by amount, also taking into account whether the player is creative or not,
     * and any {@link net.minecraft.enchantment.Enchantments} that might be applied.
     * @param yoyo Stack that was used to launch the yoyo.
     * @param hand
     * @param amount Amount of damage to be done to the yoyo.
     * @param entity Entity wielding the yoyo.
     */
    <T extends LivingEntity> void damageItem(ItemStack yoyo, Hand hand, int amount, T entity);

    /**
     * Do things that should happen when the yoyo entity touches another entity here.
     * @param yoyo Stack that was used to launch the yoyo.
     * @param player Player wielding the yoyo.
     * @param hand Hand the yoyo is held in.
     * @param yoyoEntity Yoyo entity doing the touching.
     * @param targetEntity Entity the yoyo is touching.
     */
    void entityInteraction(ItemStack yoyo, PlayerEntity player, Hand hand, YoyoEntity yoyoEntity, Entity targetEntity);

    /**
     * Does the yoyo entity interact with blocks at all?
     * @param yoyo The {@link ItemStack} that was used to launch the yoyo.
     * @return Whether or not to calculate and handle block collision actions.
     */
    boolean interactsWithBlocks(ItemStack yoyo);

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
    void blockInteraction(ItemStack yoyo, PlayerEntity player, World world, BlockPos pos, BlockState state, Block block, YoyoEntity yoyoEntity);

    /**
     * Other things that should happen in the yoyo entity's ticking
     * @param yoyo Stack that was used to launch the yoyo.
     * @param yoyoEntity Yoyo entity being updated.
     */
    default void onUpdate(ItemStack yoyo, YoyoEntity yoyoEntity) {}

    /**
     * A multiplier to the yoyo entity's movement speed when it is in water.
     * @param yoyo Stack that was used to launch the yoyo.
     */
    default float getWaterMovementModifier(ItemStack yoyo) { return 0.3f; }

    /**
     * The color the cord should be.
     * @param yoyo Stack that was used to launch the yoyo.
     * @param ticks The exact amount of ticks (and partial ticks) that the yoyo has existed.
     */
    @OnlyIn(Dist.CLIENT)
    default int getCordColor(ItemStack yoyo, float ticks)
    {
        return 0xDDDDDD;
    }

    /**
     * Primarily used for gardening yoyos. The orientation the yoyos should be spinning in.
     * @param yoyo Stack that was used to launch the yoyo.
     */
    @OnlyIn(Dist.CLIENT)
    default RenderOrientation getRenderOrientation(ItemStack yoyo)
    {
        return RenderOrientation.Vertical;
    }
}
