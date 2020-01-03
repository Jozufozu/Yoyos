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
package com.jozufozu.yoyos.compat.botania

import com.jozufozu.yoyos.common.ItemYoyo
import com.jozufozu.yoyos.common.YoyoEntity
import com.jozufozu.yoyos.common.YoyosConfig
import com.jozufozu.yoyos.common.api.IYoyo
import com.jozufozu.yoyos.common.api.YoyoFactory
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.IItemTier
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.world.World
import vazkii.botania.api.mana.IManaUsingItem
import vazkii.botania.api.mana.ManaItemHandler
import vazkii.botania.common.item.equipment.tool.ToolCommons

open class ManaYoyoItem(name: String, material: IItemTier, private val manaPerDamage: Int, properties: Properties, yoyoFactory: YoyoFactory) : ItemYoyo(name, material, properties, yoyoFactory), IYoyo, IManaUsingItem {

    constructor(name: String, material: IItemTier, manaPerDamage: Int, properties: Properties) : this(name, material, manaPerDamage, properties, ::YoyoEntity) {}

    override fun inventoryTick(stack: ItemStack, world: World, player: Entity, itemSlot: Int, isSelected: Boolean) {
        if (!world.isRemote && player is PlayerEntity && stack.damage > 0 && ManaItemHandler.requestManaExactForTool(stack, player, manaPerDamage * 2, true)) {
            stack.damage--
        }
    }

    override fun usesMana(itemStack: ItemStack): Boolean {
        return true
    }

    override fun getAttackDamage(yoyo: ItemStack): Double {
        return YoyosConfig.botaniaYoyos.manasteelYoyo.damage.get()
    }

    override fun getWeight(yoyo: ItemStack): Double {
        return YoyosConfig.botaniaYoyos.manasteelYoyo.weight.get()
    }

    override fun getLength(yoyo: ItemStack): Double {
        return YoyosConfig.botaniaYoyos.manasteelYoyo.length.get()
    }

    override fun getDuration(yoyo: ItemStack): Int {
        return YoyosConfig.botaniaYoyos.manasteelYoyo.duration.get()
    }

    override fun <T : LivingEntity> damageItem(stack: ItemStack, hand: Hand, amount: Int, entity: T) {
        ToolCommons.damageItem(stack, 1, entity, manaPerDamage)
    }

    override fun getCordColor(yoyo: ItemStack, ticks: Float): Int {
        return 0xadf4e2 // Mana mint
    }

}