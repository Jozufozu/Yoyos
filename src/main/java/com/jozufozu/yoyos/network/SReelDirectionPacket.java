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

import com.jozufozu.yoyos.common.StickyYoyoEntity;
import com.jozufozu.yoyos.common.YoyoEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SReelDirectionPacket
{
    /**
     * 0 - no movement
     * -1 - reel in
     * 1 - reel out
     */
    private byte direction;

    public SReelDirectionPacket(byte dir)
    {
        direction = dir;
    }

    public SReelDirectionPacket(PacketBuffer buf)
    {
        direction = buf.readByte();
    }

    public void encode(PacketBuffer buf)
    {
        buf.writeByte(direction);
    }

    public void onMessage(Supplier<NetworkEvent.Context> ctx)
    {
        NetworkEvent.Context context = ctx.get();

        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();

            if (player == null) return;

            YoyoEntity maybeYoyo = YoyoEntity.CASTERS.get(player);

            if (maybeYoyo instanceof StickyYoyoEntity) ((StickyYoyoEntity) maybeYoyo).setReelDirection(direction);
        });

        context.setPacketHandled(true);
    }
}

