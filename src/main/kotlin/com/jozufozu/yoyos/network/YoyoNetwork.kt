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

import com.jozufozu.yoyos.Yoyos
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.simple.SimpleChannel

object YoyoNetwork {
    const val PROTOCOL = "1.0"
    val CHANNEL: SimpleChannel = NetworkRegistry.ChannelBuilder.named(ResourceLocation(Yoyos.MODID, "yoyos"))
            .networkProtocolVersion { PROTOCOL }
            .clientAcceptedVersions { true }
            .serverAcceptedVersions { true }
            .simpleChannel()

    private var ID = 0
    private val nextID: Int get() = ID++

    fun initialize() {
        CHANNEL.registerMessage(nextID, SYoyoRetractingPacket::class.java, SYoyoRetractingPacket::encode, ::SYoyoRetractingPacket, SYoyoRetractingPacket::onMessage)
        CHANNEL.registerMessage(nextID, SReelDirectionPacket::class.java, SReelDirectionPacket::encode, ::SReelDirectionPacket, SReelDirectionPacket::onMessage)
        CHANNEL.registerMessage(nextID, CCollectedDropsPacket::class.java, CCollectedDropsPacket::encode, ::CCollectedDropsPacket, CCollectedDropsPacket::onMessage)
        //CHANNEL.registerMessage(nextID, SAcquireTargetPacket::class.java, SAcquireTargetPacket::encode, ::SAcquireTargetPacket, SAcquireTargetPacket::onMessage)
    }
}
