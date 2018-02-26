package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.network.MessageRetractYoYo;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentKeybind;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemStickyYoyo extends ItemYoyo
{
    
    public ItemStickyYoyo()
    {
        super("sticky_yoyo", ToolMaterial.DIAMOND);
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        if (!worldIn.isRemote)
        {
            EntityYoyo entityYoyo = EntityYoyo.CASTERS.get(playerIn);
            
            if (entityYoyo != null && entityYoyo.isEntityAlive())
            {
                entityYoyo.setRetracting(!entityYoyo.isRetracting());
                YoyoNetwork.INSTANCE.sendToAll(new MessageRetractYoYo(entityYoyo));
                playerIn.swingArm(hand);
            }
            else if (itemStackIn.getItemDamage() < itemStackIn.getMaxDamage())
            {
                worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
                
                worldIn.spawnEntity(new EntityStickyYoyo(worldIn, playerIn));
                
                playerIn.swingArm(hand);
            }
        }
        
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
    }
    
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add("");
        tooltip.add(I18n.format("yoyos.info.sticky.name"));
        tooltip.add(I18n.format("yoyos.info.sticky.retraction.name", new TextComponentKeybind("key.sneak").getUnformattedText()));
    }
}
