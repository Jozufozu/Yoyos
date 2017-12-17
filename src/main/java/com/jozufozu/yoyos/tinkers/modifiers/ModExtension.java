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
 * Additional length
 */
public class ModExtension extends ToolModifier
{
    public ModExtension()
    {
        super("extension", 0x4FDCFF);
        
        addAspects(new ModifierAspect.MultiAspect(this, 10, 8, 1));
    }
    
    @Override
    protected boolean canApplyCustom(ItemStack stack) throws TinkerGuiException
    {
        if (stack.getItem() != TinkersYoyos.YOYO)
            throw new TinkerGuiException(Util.translateFormatted("gui.error.not_a_yoyo", Util.translate("modifier.extension.name")));
        return true;
    }
    
    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag)
    {
        ModifierNBT.IntegerNBT data = ModifierNBT.readInteger(modifierTag);
        
        YoyoNBT toolData = new YoyoNBT(TagUtil.getTagSafe(rootCompound, Tags.TOOL_DATA));
        toolData.chordLength += data.level * 2;
        
        TagUtil.setToolTag(rootCompound, toolData.get());
    }
    
    @Override
    public String getTooltip(NBTTagCompound modifierTag, boolean detailed)
    {
        return getLeveledTooltip(modifierTag, detailed);
    }
}
