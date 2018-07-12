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

package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.network.MessageRetractYoYo;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
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
            }
            else if (itemStackIn.getItemDamage() < itemStackIn.getMaxDamage())
            {
                EntityStickyYoyo yoyo = new EntityStickyYoyo(worldIn, playerIn);
                worldIn.spawnEntity(yoyo);

                worldIn.playSound(null, yoyo.posX, yoyo.posY, yoyo.posZ, Yoyos.YOYO_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

                playerIn.addExhaustion(0.05F);
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
        tooltip.add(I18n.format("yoyos.info.sticky.release.name", new TextComponentKeybind("key.jump").getUnformattedText()));
    }
}
