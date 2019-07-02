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
import com.jozufozu.yoyos.network.ReelStateC2SPacket;
import com.jozufozu.yoyos.network.YoyoRetractingC2SPacket;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class NetworkHandlers implements ClientModInitializer
{
    private static int lastReel;

    @Override
    public void onInitializeClient()
    {
        WorldTickCallback.EVENT.register(NetworkHandlers::onTickWorldTick);
        UseItemCallback.EVENT.register(NetworkHandlers::onPlayerInteractRightClickItem);
    }

    public static void onTickWorldTick(World event)
    {
        int reel = 0;

        MinecraftClient mc = MinecraftClient.getInstance();
        YoyoEntity yoyo = YoyoEntity.CASTERS.get(mc.player);

        if (yoyo instanceof StickyYoyoEntity)
        {
            if (mc.options.keyJump.isPressed()) reel += 1;
            if (mc.options.keySneak.isPressed()) reel -= 1;

            int abs = Math.abs(reel);
            if (abs > 1)
                reel /= abs;

            if (reel != lastReel)
            {
                ClientSidePacketRegistry.INSTANCE.sendToServer(new ReelStateC2SPacket(reel));
            }
        }

        lastReel = reel;
    }

    public static ActionResult onPlayerInteractRightClickItem(PlayerEntity player, World world, Hand hand)
    {
        if (player == MinecraftClient.getInstance().player)
        {
            YoyoEntity yoyo = YoyoEntity.CASTERS.get(player);
            if (yoyo != null)
            {
                ClientSidePacketRegistry.INSTANCE.sendToServer(new YoyoRetractingC2SPacket(!yoyo.isRetracting()));
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }
}
