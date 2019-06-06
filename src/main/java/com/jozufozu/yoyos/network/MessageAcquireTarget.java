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
import com.jozufozu.yoyos.compat.EntityChaserYoyo;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

public class MessageAcquireTarget implements IMessage
{
    public MessageAcquireTarget() { }

    @Override
    public void fromBytes(ByteBuf buf) { }

    @Override
    public void toBytes(ByteBuf buf) { }

    public static class MessageTargetUpdate implements IMessage
    {
        private int yoyoID;
        private int targetID;

        public MessageTargetUpdate() { }

        public MessageTargetUpdate(EntityChaserYoyo yoyo, @Nullable Entity entity)
        {
            yoyoID = yoyo.getEntityId();
            if (entity == null)
                targetID = -1;
            else
            targetID = entity.getEntityId();
        }

        public MessageTargetUpdate(EntityChaserYoyo yoyo)
        {
            yoyoID = yoyo.getEntityId();
            targetID = -1;
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeInt(yoyoID);
            buf.writeInt(targetID);
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            yoyoID = buf.readInt();
            targetID = buf.readInt();
        }

        public static class Handler implements IMessageHandler<MessageTargetUpdate, IMessage>
        {
            @Override
            @Nullable
            public IMessage onMessage(MessageTargetUpdate message, MessageContext ctx)
            {
                Minecraft mc = Minecraft.getMinecraft();
                mc.addScheduledTask(() -> {
                    Entity maybeYoyo = mc.world.getEntityByID(message.yoyoID);

                    if (maybeYoyo instanceof EntityChaserYoyo)
                    {
                        if (message.targetID == -1)
                            ((EntityChaserYoyo) maybeYoyo).forgetTargetEntity();
                        else
                            ((EntityChaserYoyo) maybeYoyo).setTargetEntity(mc.world.getEntityByID(message.targetID));
                    }
                });
                return null;
            }
        }
    }

    public static class Handler implements IMessageHandler<MessageAcquireTarget, IMessage>
    {
        @Override
        @Nullable
        public IMessage onMessage(MessageAcquireTarget message, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.mcServer.addScheduledTask(() -> {
                EntityYoyo maybeYoyo = EntityYoyo.CASTERS.get(player);

                if (maybeYoyo instanceof EntityChaserYoyo)
                {
                    ((EntityChaserYoyo) maybeYoyo).acquireTargetEntity();
                }
            });
            return null;
        }
    }
}
