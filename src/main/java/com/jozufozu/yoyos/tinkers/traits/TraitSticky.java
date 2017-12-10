package com.jozufozu.yoyos.tinkers.traits;

import com.jozufozu.yoyos.tinkers.TinkersYoyos;
import slimeknights.tconstruct.library.modifiers.IToolMod;
import slimeknights.tconstruct.library.traits.AbstractTrait;

public class TraitSticky extends AbstractTrait
{
    public TraitSticky()
    {
        super("sticky", 0x63BD74);
    }
    
    @Override
    public boolean canApplyTogether(IToolMod otherModifier)
    {
        return otherModifier != TinkersYoyos.GLUEY;
    }
}
