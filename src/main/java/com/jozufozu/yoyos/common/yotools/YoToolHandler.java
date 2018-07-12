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

package com.jozufozu.yoyos.common.yotools;

import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.common.EntityArbitraryYoyo;
import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.network.MessageRetractYoYo;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// I'm disabling this util I can figure out if it makes sense at all
// @Mod.EventBusSubscriber(modid = Yoyos.MODID)
public class YoToolHandler
{
    @SubscribeEvent
    public static void playerClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        if (YoToolData.hasData(event.getItemStack()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void playerClickItem(PlayerInteractEvent.RightClickItem event)
    {
        World world = event.getWorld();

        if (world.isRemote) return;

        EntityPlayer player = event.getEntityPlayer();
        EnumHand hand = event.getHand();
        ItemStack heldItem = player.getHeldItem(hand);

        if (!(heldItem.getItem() instanceof ItemBlock) && YoToolData.hasData(heldItem))
        {
            EntityYoyo entityYoyo = EntityYoyo.CASTERS.get(player);

            if (entityYoyo != null && entityYoyo.isEntityAlive())
            {
                entityYoyo.setRetracting(!entityYoyo.isRetracting());
                YoyoNetwork.INSTANCE.sendToAll(new MessageRetractYoYo(entityYoyo));
            }
            else if (!heldItem.isItemStackDamageable() || heldItem.getItemDamage() <= heldItem.getMaxDamage())
            {
                EntityYoyo yoyo = new EntityArbitraryYoyo(world, player);
                world.spawnEntity(yoyo);

                world.playSound(null, yoyo.posX, yoyo.posY, yoyo.posZ, Yoyos.YOYO_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F);// / (itemRand.nextFloat() * 0.4F + 0.8F));
            }

            event.setCancellationResult(EnumActionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
