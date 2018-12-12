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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class MessageYoyoRetracting implements IMessage
{
    private boolean retracting;
    
    public MessageYoyoRetracting() {}
    
    public MessageYoyoRetracting(boolean retracting)
    {
        this.retracting = retracting;
    }
    
    @Override
    public void fromBytes(ByteBuf buf)
    {
        retracting = buf.readBoolean();
    }
    
    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeBoolean(retracting);
    }

    public static class MessageYoyoRetractingReply implements IMessage
    {
        private int yoyoID;

        private boolean retracting;

        public MessageYoyoRetractingReply() { }

        public MessageYoyoRetractingReply(EntityYoyo yoyo)
        {
            this.yoyoID = yoyo.getEntityId();
            this.retracting = yoyo.isRetracting();
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeInt(yoyoID);
            buf.writeBoolean(retracting);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            yoyoID = buf.readInt();
            retracting = buf.readBoolean();
        }

        public static class Handler implements IMessageHandler<MessageYoyoRetractingReply, IMessage>
        {
            @Override
            @Nullable
            public IMessage onMessage(MessageYoyoRetractingReply message, MessageContext ctx)
            {
                Minecraft mc = Minecraft.getMinecraft();
                mc.addScheduledTask(() -> {
                    Entity maybeYoYo = mc.world.getEntityByID(message.yoyoID);

                    if (maybeYoYo instanceof EntityYoyo)
                    {
                        ((EntityYoyo) maybeYoYo).setRetracting(message.retracting);
                    }
                });
                return null;
            }
        }
    }
    
    public static class Handler implements IMessageHandler<MessageYoyoRetracting, IMessage>
    {
        @Override
        @Nullable
        public IMessage onMessage(MessageYoyoRetracting message, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.mcServer.addScheduledTask(() -> {
                EntityYoyo maybeYoyo = EntityYoyo.CASTERS.get(player);

                if (maybeYoyo != null)
                {
                    maybeYoyo.setRetracting(message.retracting);

                    YoyoNetwork.INSTANCE.sendToAllTracking(new MessageYoyoRetractingReply(maybeYoyo), maybeYoyo);
                }
            });
            return null;
        }
    }
}
