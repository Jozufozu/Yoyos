package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.network.MessageRetractYoYo;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemStickyYoyo extends ItemYoyo {

    public ItemStickyYoyo() {
        super("sticky_yoyo", ToolMaterial.DIAMOND);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        if (!worldIn.isRemote) {
            EntityYoyo entityYoyo = EntityYoyo.CASTERS.get(playerIn);

            if (entityYoyo != null && entityYoyo.isEntityAlive()) {
                entityYoyo.setRetracting(!entityYoyo.isRetracting());
                YoyoNetwork.INSTANCE.sendToAll(new MessageRetractYoYo(entityYoyo, false));
                playerIn.swingArm(hand);
            }
            else if (itemStackIn.getItemDamage() < itemStackIn.getMaxDamage()) {
                worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

                worldIn.spawnEntityInWorld(new EntityStickyYoyo(worldIn, playerIn));

                playerIn.swingArm(hand);
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
    }
}
