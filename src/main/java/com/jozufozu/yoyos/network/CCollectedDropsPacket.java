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

import com.jozufozu.yoyos.common.YoyoEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;

public class CCollectedDropsPacket
{
    private int yoyoID;
    private ItemStack[] drops = new ItemStack[]{};

    public CCollectedDropsPacket(YoyoEntity yoyo)
    {
        yoyoID = yoyo.getEntityId();
        ArrayList<ItemStack> drops = yoyo.getCollectedDrops();

        this.drops = drops.toArray(this.drops);
    }

    public CCollectedDropsPacket(PacketBuffer buf)
    {
        yoyoID = buf.readInt();
        int length = buf.readInt();

        drops = new ItemStack[length];
        for (int i = 0; i < length; i++)
            drops[i] = readBigItemStack(buf);
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeInt(yoyoID);
        buf.writeInt(drops.length);

        for (ItemStack drop : drops)
            writeBigItemStack(buf, drop);
    }

    public void onMessage(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
                              {
                                  Minecraft mc = Minecraft.getInstance();

                                  Entity maybeYoyo = mc.world.getEntityByID(yoyoID);

                                  if (maybeYoyo instanceof YoyoEntity)
                                  {
                                      YoyoEntity yoyo = (YoyoEntity) maybeYoyo;

                                      // We trust that the drops are condensed
                                      yoyo.getCollectedDrops().clear();
                                      Collections.addAll(yoyo.getCollectedDrops(), drops);
                                  }
                              });

        ctx.get().setPacketHandled(true);
    }

    private static void writeBigItemStack(PacketBuffer buf, ItemStack stack)
    {
        if (stack.isEmpty())
        {
            buf.writeBoolean(false);
        }
        else
        {
            buf.writeBoolean(true);
            Item item = stack.getItem();
            buf.writeVarInt(Item.getIdFromItem(item));
            buf.writeVarInt(stack.getCount());
            CompoundNBT compoundnbt = null;

            if (item.isDamageable() || item.shouldSyncTag())
            {
                compoundnbt = stack.getShareTag();
            }

            buf.writeCompoundTag(compoundnbt);
        }
    }

    private static ItemStack readBigItemStack(PacketBuffer buf)
    {
        if (!buf.readBoolean())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            int id = buf.readVarInt();
            int count = buf.readVarInt();
            ItemStack itemstack = new ItemStack(Item.getItemById(id), count);
            itemstack.readShareTag(buf.readCompoundTag());
            return itemstack;
        }
    }
}
