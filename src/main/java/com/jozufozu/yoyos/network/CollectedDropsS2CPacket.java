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

import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.common.YoyoEntity;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;
import java.util.Collections;

public class CollectedDropsS2CPacket implements Packet<ClientPlayPacketListener>
{
    private int yoyoID;
    private ItemStack[] drops;
    
    public CollectedDropsS2CPacket() {}
    
    public CollectedDropsS2CPacket(YoyoEntity yoyo)
    {
        yoyoID = yoyo.getEntityId();
        drops = new ItemStack[yoyo.collectedDrops.size()];
        for (int i = 0; i < yoyo.collectedDrops.size(); i++)
            drops[i] = yoyo.collectedDrops.get(i);
    }

    public static void init()
    {
        ServerSidePacketRegistry.INSTANCE.register(new Identifier(Yoyos.MODID, "collected_drops"), (packetContext, packetByteBuf) -> {
            packetContext.getTaskQueue().method_20493(() -> {
                CollectedDropsS2CPacket message = new CollectedDropsS2CPacket();

                try
                {
                    message.read(packetByteBuf);
                }
                catch (Exception e)
                {
                    Yoyos.LOG.error("Error receiving collected drops packet:", e);
                    return;
                }

                MinecraftClient mc = MinecraftClient.getInstance();

                Entity maybeYoYo = mc.world.getEntityById(message.yoyoID);

                if (maybeYoYo instanceof YoyoEntity)
                {
                    YoyoEntity yoyo = (YoyoEntity) maybeYoYo;

                    // We trust that the drops are condensed
                    yoyo.collectedDrops.clear();
                    Collections.addAll(yoyo.collectedDrops, message.drops);
                }
            });
        });
    }

    @Override
    public void read(PacketByteBuf buf) throws IOException
    {
        yoyoID = buf.readInt();
        int length = buf.readInt();

        drops = new ItemStack[length];
        for (int i = 0; i < length; i++)
            drops[i] = readBigItemStack(buf);
    }

    @Override
    public void write(PacketByteBuf buf) throws IOException
    {
        buf.writeInt(yoyoID);
        buf.writeInt(drops.length);

        for (ItemStack drop : drops)
            writeBigItemStack(buf, drop);
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener)
    {

    }

    private void writeBigItemStack(PacketByteBuf buf, ItemStack stack)
    {
        if (stack.isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);

            Item item = stack.getItem();
            buf.writeVarInt(Item.getRawId(item));

            buf.writeVarInt(stack.getCount());
            CompoundTag tag = null;
            if (item.isDamageable() || item.shouldSyncTagToClient()) {
                tag = stack.getTag();
            }

            buf.writeCompoundTag(tag);
        }
    }

    private ItemStack readBigItemStack(PacketByteBuf buf)
    {
        if (!buf.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            int id = buf.readVarInt();
            int size = buf.readVarInt();
            ItemStack stack = new ItemStack(Item.byRawId(id), size);
            stack.setTag(buf.readCompoundTag());
            return stack;
        }
    }
}
