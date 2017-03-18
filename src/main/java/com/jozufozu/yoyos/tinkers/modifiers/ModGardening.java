package com.jozufozu.yoyos.tinkers.modifiers;

import com.jozufozu.yoyos.tinkers.TinkersYoyos;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;

/**
 * Allows for yoyos to cut down grass and flowers and such
 */
public class ModGardening extends ToolModifier {

    public ModGardening() {
        super("gardening", 0x838996);

        addAspects(new ModifierAspect.DataAspect(this), new ModifierAspect.SingleAspect(this), ModifierAspect.freeModifier);
    }

    @Override
    protected boolean canApplyCustom(ItemStack stack) throws TinkerGuiException {
        if (stack.getItem() != TinkersYoyos.YOYO) {
            throw new TinkerGuiException(Util.translateFormatted("gui.error.not_a_yoyo", Util.translate("modifier.gardening.name")));
        }

        return true;
    }

    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {
        //And... we're done
    }
}
