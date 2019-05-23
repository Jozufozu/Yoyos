/*
 * Copyright (c) 2019 Jozsef Augusztiny
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

package com.jozufozu.yoyos.tinkers.modifiers;

import com.jozufozu.yoyos.tinkers.TinkersYoyos;
import com.jozufozu.yoyos.tinkers.materials.YoyoNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.IToolMod;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.Tags;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;

/**
 * Allows for yoyos to cut down grass and flowers and such
 */
public class ModFarming extends ToolModifier
{
    public ModFarming()
    {
        super("farming", 0x27b29a);
        
        addAspects(new ModifierAspect.DataAspect(this), new ModifierAspect.SingleAspect(this), ModifierAspect.freeModifier);
    }

    @Override
    public boolean canApplyTogether(IToolMod otherModifier)
    {
        return otherModifier != TinkersYoyos.GARDENING;
    }

    @Override
    protected boolean canApplyCustom(ItemStack stack) throws TinkerGuiException
    {
        if (stack.getItem() != TinkersYoyos.YOYO)
            throw new TinkerGuiException(Util.translateFormatted("gui.error.not_a_yoyo", Util.translate("modifier.gardening.name")));
        
        return true;
    }
    
    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag)
    {
        YoyoNBT toolData = new YoyoNBT(TagUtil.getTagSafe(rootCompound, Tags.TOOL_DATA));
        toolData.attack += 0.5;
        toolData.weight += 0.5;
    
        TagUtil.setToolTag(rootCompound, toolData.get());
    }
}
