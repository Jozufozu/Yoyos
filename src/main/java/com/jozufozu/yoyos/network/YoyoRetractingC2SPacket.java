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
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;

public class YoyoRetractingC2SPacket implements Packet<ServerPlayPacketListener>
{
    private boolean retracting;
    
    public YoyoRetractingC2SPacket() {}
    
    public YoyoRetractingC2SPacket(boolean retracting)
    {
        this.retracting = retracting;
    }

    public static void init()
    {
        ClientSidePacketRegistry.INSTANCE.register(new Identifier(Yoyos.MODID, "retracting"), (packetContext, packetByteBuf) -> {
            packetContext.getTaskQueue().execute(() -> {
                YoyoRetractingC2SPacket message = new YoyoRetractingC2SPacket();

                try
                {
                    message.read(packetByteBuf);
                } catch (IOException e)
                {
                    Yoyos.LOG.error("Error receiving retraction request packet:", e);
                    return;
                }

                YoyoEntity maybeYoyo = YoyoEntity.CASTERS.get(packetContext.getPlayer());

                if (maybeYoyo != null)
                {
                    maybeYoyo.setRetracting(message.retracting);

                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(packetContext.getPlayer(), new YoyoRetractingS2CPacket(maybeYoyo));
                }
            });
        });
    }

    @Override
    public void read(PacketByteBuf buf) throws IOException
    {
        retracting = buf.readBoolean();
    }

    @Override
    public void write(PacketByteBuf buf) throws IOException
    {
        buf.writeBoolean(retracting);
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener)
    {

    }

    public static class YoyoRetractingS2CPacket implements Packet
    {
        private int yoyoID;

        private boolean retracting;

        public YoyoRetractingS2CPacket() { }

        public YoyoRetractingS2CPacket(YoyoEntity yoyo)
        {
            this.yoyoID = yoyo.getEntityId();
            this.retracting = yoyo.isRetracting();
        }

        public static void init()
        {
            ServerSidePacketRegistry.INSTANCE.register(new Identifier(Yoyos.MODID, "retracting_reply"), (packetContext, packetByteBuf) -> {
                packetContext.getTaskQueue().execute(() -> {
                    YoyoRetractingS2CPacket message = new YoyoRetractingS2CPacket();

                    try
                    {
                        message.read(packetByteBuf);
                    } catch (IOException e)
                    {
                        Yoyos.LOG.error("Error receiving retraction reply packet:", e);
                        return;
                    }

                    Entity maybeYoyo = packetContext.getPlayer().world.getEntityById(message.yoyoID);

                    if (maybeYoyo instanceof YoyoEntity)
                    {
                        ((YoyoEntity) maybeYoyo).setRetracting(message.retracting);
                    }
                });
            });
        }

        @Override
        public void read(PacketByteBuf buf) throws IOException
        {
            yoyoID = buf.readInt();
            retracting = buf.readBoolean();
        }

        @Override
        public void write(PacketByteBuf buf) throws IOException
        {
            buf.writeInt(yoyoID);
            buf.writeBoolean(retracting);
        }

        @Override
        public void apply(PacketListener packetListener)
        {

        }
    }
}
