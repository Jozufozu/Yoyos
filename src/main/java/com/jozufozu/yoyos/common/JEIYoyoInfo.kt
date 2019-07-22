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

package com.jozufozu.yoyos.common

import com.jozufozu.yoyos.Yoyos
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.enchantment.EnchantmentData
import net.minecraft.item.EnchantedBookItem
import net.minecraft.util.ResourceLocation

@JeiPlugin
class JEIYoyoInfo : IModPlugin {

    override fun getPluginUid(): ResourceLocation {
        return ID
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        registration.addIngredientInfo(EnchantedBookItem.getEnchantedItemStack(EnchantmentData(Yoyos.Enchantments.COLLECTING, 1)), VanillaTypes.ITEM, "enchantment.yoyos.collecting.desc")
    }

    companion object {
        private val ID = ResourceLocation(Yoyos.MODID, "yoyos")
    }
}
