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

package com.jozufozu.yoyos.client;

import com.jozufozu.yoyos.common.StickyYoyoEntity;
import com.jozufozu.yoyos.common.YoyoEntity;
import com.jozufozu.yoyos.network.SReelDirectionPacket;
import com.jozufozu.yoyos.network.SYoyoRetractingPacket;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@OnlyIn(Dist.CLIENT)
public class NetworkHandlers
{
    private static int lastReel;

    public static void onTickWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) return;

        byte reel = 0;

        Minecraft mc = Minecraft.getInstance();
        YoyoEntity yoyo = YoyoEntity.CASTERS.get(mc.player);

        if (yoyo instanceof StickyYoyoEntity)
        {
            if (mc.gameSettings.keyBindJump.isKeyDown()) reel += 1;
            if (mc.gameSettings.keyBindSneak.isKeyDown()) reel -= 1;

            int abs = Math.abs(reel);
            if (abs > 1)
                reel /= abs;

            if (reel != lastReel)
            {
                YoyoNetwork.INSTANCE.sendToServer(new SReelDirectionPacket(reel));
            }
        }

        lastReel = reel;
    }

    public static void onPlayerInteractRightClickItem(PlayerInteractEvent.RightClickItem event)
    {
        PlayerEntity player = event.getEntityPlayer();

        YoyoEntity yoyo = YoyoEntity.CASTERS.get(player.getUniqueID());
        if (yoyo != null && player == Minecraft.getInstance().player)
        {
            YoyoNetwork.INSTANCE.sendToServer(new SYoyoRetractingPacket(!yoyo.isRetracting()));
            event.setCanceled(true);
        }
    }
}
