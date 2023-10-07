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

package com.jozufozu.yoyos.network

import com.jozufozu.yoyos.common.YoyoEntity
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

class SYoyoRetractingPacket {
    private var retracting: Boolean = false

    constructor(retracting: Boolean) {
        this.retracting = retracting
    }

    constructor(buf: FriendlyByteBuf) {
        retracting = buf.readBoolean()
    }

    fun encode(buf: FriendlyByteBuf) {
        buf.writeBoolean(retracting)
    }

    fun onMessage(ctx: Supplier<NetworkEvent.Context>) {
        ctx.get().enqueueWork {
            val player = ctx.get().sender ?: return@enqueueWork

            val maybeYoyo = YoyoEntity.CASTERS[player.uuid]

            if (maybeYoyo != null) maybeYoyo.isRetracting = retracting
        }

        ctx.get().packetHandled = true
    }
}
