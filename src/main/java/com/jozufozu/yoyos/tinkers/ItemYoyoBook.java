package com.jozufozu.yoyos.tinkers;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import slimeknights.mantle.util.LocUtils;
import slimeknights.tconstruct.library.TinkerRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemYoyoBook extends Item {
    
    public ItemYoyoBook() {
        this.setCreativeTab(TinkerRegistry.tabGeneral);
        this.setMaxStackSize(1);
    }
    
    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemStack = playerIn.getHeldItem(handIn);
        if(worldIn.isRemote) {
            TinkersClientProxy.INSTANCE.openGui(itemStack);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }
    
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if(I18n.canTranslate(super.getUnlocalizedName(stack) + ".tooltip")) {
            tooltip.addAll(LocUtils.getTooltips(TextFormatting.GRAY.toString() + LocUtils.translateRecursive(super.getUnlocalizedName(stack) + ".tooltip")));
        }
    }
}
