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

import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.common.RenderOrientation;
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
     * and any {@link net.minecraft.init.Enchantments} that might be applied.
     * @param yoyo Stack that was used to launch the yoyo.
     * @param amount Amount of damage to be done to the yoyo.
     * @param player Player wielding the yoyo.
     */
    void damageItem(ItemStack yoyo, int amount, EntityLivingBase player);

    /**
     * Do things that should happen when the yoyo entity touches another entity here.
     * @param yoyo Stack that was used to launch the yoyo.
     * @param player Player wielding the yoyo.
     * @param hand Hand the yoyo is held in.
     * @param yoyoEntity Yoyo entity doing the touching.
     * @param targetEntity Entity the yoyo is touching.
     */
    void entityInteraction(ItemStack yoyo, EntityPlayer player, EnumHand hand, EntityYoyo yoyoEntity, Entity targetEntity);

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
    void blockInteraction(ItemStack yoyo, EntityPlayer player, World world, BlockPos pos, IBlockState state, Block block, EntityYoyo yoyoEntity);

    /**
     * Other things that should happen in the yoyo entity's ticking
     * @param yoyo Stack that was used to launch the yoyo.
     * @param yoyoEntity Yoyo entity being updated.
     */
    default void onUpdate(ItemStack yoyo, EntityYoyo yoyoEntity) {}

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
    @SideOnly(Side.CLIENT)
    default int getCordColor(ItemStack yoyo, float ticks)
    {
        return 0xDDDDDD;
    }

    /**
     * Primarily used for gardening yoyos. The orientation the yoyos should be spinning in.
     * @param yoyo Stack that was used to launch the yoyo.
     */
    @SideOnly(Side.CLIENT)
    default RenderOrientation getRenderOrientation(ItemStack yoyo)
    {
        return RenderOrientation.Vertical;
    }
}
