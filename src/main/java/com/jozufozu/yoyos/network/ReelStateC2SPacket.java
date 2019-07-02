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
import com.jozufozu.yoyos.common.StickyYoyoEntity;
import com.jozufozu.yoyos.common.YoyoEntity;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.io.IOException;

public class ReelStateC2SPacket implements Packet<ServerPlayPacketListener>
{
    /**
     * 0 - no movement
     * -1 - reel in
     * 1 - reel out
     */
    private int direction;

    public ReelStateC2SPacket() { }

    public ReelStateC2SPacket(int dir)
    {
        direction = dir;
    }

    public static void init()
    {
        ClientSidePacketRegistry.INSTANCE.register(new Identifier(Yoyos.MODID, "reel_state"), (packetContext, packetByteBuf) -> {
            packetContext.getTaskQueue().execute(() -> {
                ReelStateC2SPacket message = new ReelStateC2SPacket();

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
                    ((StickyYoyoEntity) maybeYoyo).setReelDirection(message.direction);

                    ReelStateS2CPacket packet = new ReelStateS2CPacket(maybeYoyo, message);
                    PlayerStream.watching(maybeYoyo).forEach((playerEntity) -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerEntity, packet));
                }
            });
        });
    }

    @Override
    public void read(PacketByteBuf buf) throws IOException
    {
        direction = buf.readByte();
    }

    @Override
    public void write(PacketByteBuf buf) throws IOException
    {
        buf.writeByte(direction);
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener)
    {

    }

    public static class ReelStateS2CPacket implements Packet<ClientPlayPacketListener>
    {
        private int direction;
        private int yoyoID;

        public ReelStateS2CPacket() { }

        public ReelStateS2CPacket(YoyoEntity yoyo, ReelStateC2SPacket message)
        {
            yoyoID = yoyo.getEntityId();
            direction = message.direction;
        }

        public static void init()
        {
            ServerSidePacketRegistry.INSTANCE.register(new Identifier(Yoyos.MODID, "reel_state_reply"), (packetContext, packetByteBuf) -> {
                packetContext.getTaskQueue().execute(() -> {
                    ReelStateS2CPacket message = new ReelStateS2CPacket();

                    try
                    {
                        message.read(packetByteBuf);
                    } catch (IOException e)
                    {
                        Yoyos.LOG.error("Error receiving retraction reply packet:", e);
                        return;
                    }

                    Entity maybeYoyo = packetContext.getPlayer().world.getEntityById(message.yoyoID);

                    if (maybeYoyo instanceof StickyYoyoEntity)
                    {
                        ((StickyYoyoEntity) maybeYoyo).setReelDirection(message.direction);
                    }
                });
            });
        }

        @Override
        public void read(PacketByteBuf buf) throws IOException
        {
            yoyoID = buf.readInt();
            direction = buf.readByte();
        }

        @Override
        public void write(PacketByteBuf buf) throws IOException
        {
            buf.writeInt(yoyoID);
            buf.writeByte(direction);
        }

        @Override
        public void apply(ClientPlayPacketListener clientPlayPacketListener)
        {

        }
    }
}

