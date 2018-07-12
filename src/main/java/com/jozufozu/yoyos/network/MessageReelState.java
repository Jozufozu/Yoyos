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

import com.jozufozu.yoyos.common.EntityStickyYoyo;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class MessageReelState implements IMessage
{
    /**
     * 0 - no movement
     * -1 - reel in
     * 1 - reel out
     */
    private int direction;

    private int dim;
    private int yoyoID;

    public MessageReelState() { }

    public MessageReelState(EntityStickyYoyo yoyo, int dir)
    {
        dim = yoyo.world.provider.getDimension();
        yoyoID = yoyo.getEntityId();
        direction = dir;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(dim);
        buf.writeInt(yoyoID);
        buf.writeByte(direction);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        dim = buf.readInt();
        yoyoID = buf.readInt();
        direction = buf.readByte();
    }

    public static class MessageReelStateReply implements IMessage
    {
        private int direction;
        private int yoyoID;

        public MessageReelStateReply() { }

        public MessageReelStateReply(MessageReelState message)
        {
            yoyoID = message.yoyoID;
            direction = message.direction;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeInt(yoyoID);
            buf.writeByte(direction);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            yoyoID = buf.readInt();
            direction = buf.readByte();
        }

        public static class Handler implements IMessageHandler<MessageReelStateReply, IMessage>
        {
            @Override
            @Nullable
            public IMessage onMessage(MessageReelStateReply message, MessageContext ctx)
            {
                Minecraft minecraft = Minecraft.getMinecraft();
                minecraft.addScheduledTask(() -> {
                    Entity maybeYoyo = minecraft.world.getEntityByID(message.yoyoID);

                    if (maybeYoyo instanceof EntityStickyYoyo)
                    {
                        ((EntityStickyYoyo) maybeYoyo).setReelDirection(message.direction);
                    }
                });
                return null;
            }
        }
    }

    public static class Handler implements IMessageHandler<MessageReelState, IMessage>
    {
        @Override
        @Nullable
        public IMessage onMessage(MessageReelState message, MessageContext ctx)
        {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            server.addScheduledTask(() -> {
                WorldServer world = server.getWorld(message.dim);

                Entity maybeYoyo = world.getEntityByID(message.yoyoID);

                if (maybeYoyo instanceof EntityStickyYoyo)
                {
                    ((EntityStickyYoyo) maybeYoyo).setReelDirection(message.direction);

                    // We reply to all, so other clients know that the reeling is happening
                    YoyoNetwork.INSTANCE.sendToAllTracking(new MessageReelStateReply(message), maybeYoyo);
                }
            });
            return null;
        }
    }
}

