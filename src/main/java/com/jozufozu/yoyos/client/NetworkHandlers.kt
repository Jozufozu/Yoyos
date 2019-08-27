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

package com.jozufozu.yoyos.client

import com.jozufozu.yoyos.common.StickyYoyoEntity
import com.jozufozu.yoyos.common.YoyoEntity
import com.jozufozu.yoyos.network.SReelDirectionPacket
import com.jozufozu.yoyos.network.YoyoNetwork
import net.minecraft.client.Minecraft
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.event.TickEvent
import kotlin.math.abs

@OnlyIn(Dist.CLIENT)
object NetworkHandlers {
    private var lastReel: Int = 0

    @JvmStatic fun onTickWorldTick(event: TickEvent.WorldTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        var reel = 0

        val mc = Minecraft.getInstance()
        val yoyo = YoyoEntity.CASTERS[mc.player?.uniqueID]

        if (yoyo is StickyYoyoEntity) {
            if (mc.gameSettings.keyBindJump.isKeyDown) reel += 1
            if (mc.gameSettings.keyBindSneak.isKeyDown) reel -= 1

            val abs = abs(reel)
            if (abs > 1)
                reel /= abs

            if (reel != lastReel) {
                YoyoNetwork.CHANNEL.sendToServer(SReelDirectionPacket(reel.toByte()))
            }
        }

        lastReel = reel
    }
}
