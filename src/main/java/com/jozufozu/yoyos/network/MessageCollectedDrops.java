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

package com.jozufozu.yoyos.network;

import com.jozufozu.yoyos.common.EntityYoyo;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.Collections;

public class MessageCollectedDrops implements IMessage
{
    private int yoyoID;
    private ItemStack[] drops;
    
    public MessageCollectedDrops() {}
    
    public MessageCollectedDrops(EntityYoyo yoyo)
    {
        yoyoID = yoyo.getEntityId();
        drops = new ItemStack[yoyo.collectedDrops.size()];
        for (int i = 0; i < yoyo.collectedDrops.size(); i++)
            drops[i] = yoyo.collectedDrops.get(i);
    }
    
    @Override
    public void fromBytes(ByteBuf buf)
    {
        yoyoID = buf.readInt();
        int length = buf.readInt();
    
        drops = new ItemStack[length];
        for (int i = 0; i < length; i++)
            drops[i] = readBigItemStack(buf);
    }
    
    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(yoyoID);
        buf.writeInt(drops.length);
    
        for (ItemStack drop : drops)
            writeBigItemStack(buf, drop);
    }

    private void writeBigItemStack(ByteBuf buf, ItemStack stack)
    {
        if (stack.isEmpty())
        {
            buf.writeShort(-1);
        }
        else
        {
            buf.writeShort(Item.getIdFromItem(stack.getItem()));
            buf.writeInt(stack.getCount());
            buf.writeShort(stack.getMetadata());
            NBTTagCompound nbttagcompound = null;

            if (stack.getItem().isDamageable() || stack.getItem().getShareTag())
            {
                nbttagcompound = stack.getItem().getNBTShareTag(stack);
            }

            ByteBufUtils.writeTag(buf, nbttagcompound);
        }
    }

    private ItemStack readBigItemStack(ByteBuf buf)
    {
        int i = buf.readShort();

        if (i < 0)
        {
            return ItemStack.EMPTY;
        }
        else
        {
            int j = buf.readInt();
            int k = buf.readShort();
            ItemStack itemstack = new ItemStack(Item.getItemById(i), j, k);
            itemstack.setTagCompound(ByteBufUtils.readTag(buf));
            return itemstack;
        }
    }
    
    public static class Handler implements IMessageHandler<MessageCollectedDrops, IMessage>
    {
        @Override
        @Nullable
        public IMessage onMessage(MessageCollectedDrops message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() ->
            {
                Minecraft mc = Minecraft.getMinecraft();
                
                Entity maybeYoYo = mc.world.getEntityByID(message.yoyoID);
                
                if (maybeYoYo instanceof EntityYoyo)
                {
                    EntityYoyo yoyo = (EntityYoyo) maybeYoYo;

                    // We trust that the drops are condensed
                    yoyo.collectedDrops.clear();
                    Collections.addAll(yoyo.collectedDrops, message.drops);
                }
            });
            return null;
        }
    }
}
