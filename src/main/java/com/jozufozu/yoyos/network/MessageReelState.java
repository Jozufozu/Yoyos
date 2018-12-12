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
import com.jozufozu.yoyos.common.EntityYoyo;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
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

    public MessageReelState() { }

    public MessageReelState(int dir)
    {
        direction = dir;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(direction);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        direction = buf.readByte();
    }

    public static class MessageReelStateReply implements IMessage
    {
        private int direction;
        private int yoyoID;

        public MessageReelStateReply() { }

        public MessageReelStateReply(EntityYoyo yoyo, MessageReelState message)
        {
            yoyoID = yoyo.getEntityId();
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
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.mcServer.addScheduledTask(() -> {
                EntityYoyo maybeYoyo = EntityYoyo.CASTERS.get(player);

                if (maybeYoyo instanceof EntityStickyYoyo)
                {
                    ((EntityStickyYoyo) maybeYoyo).setReelDirection(message.direction);

                    YoyoNetwork.INSTANCE.sendToAllTracking(new MessageReelStateReply(maybeYoyo, message), maybeYoyo);
                }
            });
            return null;
        }
    }
}

