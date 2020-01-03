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

import com.jozufozu.yoyos.common.YoyosConfig
import net.minecraft.item.IItemTier
import net.minecraft.item.ItemStack
import vazkii.botania.api.item.IPixieSpawner

class ElementiumYoyoItem(name: String, material: IItemTier, manaPerDamage: Int, properties: Properties) : ManaYoyoItem(name, material, manaPerDamage, properties), IPixieSpawner {
    override fun getMaxCollectedDrops(yoyo: ItemStack): Int {
        return 64 + super.getMaxCollectedDrops(yoyo)
    }

    override fun getPixieChance(itemStack: ItemStack): Float {
        return 0.05f
    }

    override fun getAttackDamage(yoyo: ItemStack): Double {
        return YoyosConfig.botaniaYoyos.elementiumYoyo.damage.get()
    }

    override fun getWeight(yoyo: ItemStack): Double {
        return YoyosConfig.botaniaYoyos.elementiumYoyo.weight.get()
    }

    override fun getLength(yoyo: ItemStack): Double {
        return YoyosConfig.botaniaYoyos.elementiumYoyo.length.get()
    }

    override fun getDuration(yoyo: ItemStack): Int {
        return YoyosConfig.botaniaYoyos.elementiumYoyo.duration.get()
    }
}