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

import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.IItemTier
import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.KeybindTextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World

class ItemStickyYoyo(name: String, material: IItemTier, properties: Properties) : ItemYoyo(name, material, properties, ::StickyYoyoEntity) {
    constructor(name: String, material: IItemTier): this(name, material, Properties())

    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<ITextComponent>, flagIn: ITooltipFlag) {
        super.addInformation(stack, worldIn, tooltip, flagIn)
        addStickyInfo(tooltip)
    }

    companion object {
        fun addStickyInfo(tooltip: MutableList<ITextComponent>) {
            tooltip.add(StringTextComponent(""))
            tooltip.add(TranslationTextComponent("tooltip.yoyos.sticky"))
            tooltip.add(TranslationTextComponent("tooltip.yoyos.sticky.retraction", KeybindTextComponent("key.sneak")))
            tooltip.add(TranslationTextComponent("tooltip.yoyos.sticky.release", KeybindTextComponent("key.jump")))
        }
    }
}
