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

package com.jozufozu.yoyos.tinkers.modifiers;

import com.jozufozu.yoyos.tinkers.TinkersYoyos;
import com.jozufozu.yoyos.tinkers.materials.YoyoNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.Tags;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;

/**
 * Makes yoyos lighter
 * Pufferfish or something
 */
public class ModFloating extends ToolModifier
{
    
    public ModFloating(int max)
    {
        super("floating", 0x00FFD9);
        
        addAspects(new ModifierAspect.LevelAspect(this, max), new ModifierAspect.DataAspect(this), ModifierAspect.freeModifier);
    }
    
    @Override
    protected boolean canApplyCustom(ItemStack stack) throws TinkerGuiException
    {
        if (stack.getItem() != TinkersYoyos.YOYO)
            throw new TinkerGuiException(Util.translateFormatted("gui.error.not_a_yoyo", Util.translate("modifier.floating.name")));
        
        YoyoNBT toolData = new YoyoNBT(TagUtil.getTagSafe(stack.getTagCompound(), Tags.TOOL_DATA));
        if (toolData.weight <= 0.1)
            throw new TinkerGuiException(Util.translateFormatted("gui.error.too_light"));

        return true;
    }
    
    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag)
    {
        ModifierNBT.IntegerNBT data = ModifierNBT.readInteger(modifierTag);
        
        YoyoNBT toolData = new YoyoNBT(TagUtil.getTagSafe(rootCompound, Tags.TOOL_DATA));
        
        for (int i = data.level; i > 0; i--)
        {
            toolData.weight -= 0.5F / i;
            toolData.weight -= 0.2F;
        }
        
        toolData.weight = Math.max(toolData.weight, 0.01F);
        
        TagUtil.setToolTag(rootCompound, toolData.get());
    }
    
    @Override
    public String getTooltip(NBTTagCompound modifierTag, boolean detailed)
    {
        return getLeveledTooltip(modifierTag, detailed);
    }
}
