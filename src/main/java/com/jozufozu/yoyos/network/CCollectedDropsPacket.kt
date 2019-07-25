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
import net.minecraft.client.Minecraft
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.*
import java.util.function.Supplier

class CCollectedDropsPacket {
    private val yoyoID: Int
    private val drops: Array<ItemStack>

    constructor(yoyo: YoyoEntity) {
        yoyoID = yoyo.entityId
        val drops = yoyo.collectedDrops

        this.drops = drops.toTypedArray()
    }

    constructor(buf: PacketBuffer) {
        yoyoID = buf.readInt()
        val length = buf.readInt()

        drops = Array(length) { readBigItemStack(buf) }
    }

    fun encode(buf: PacketBuffer) {
        buf.writeInt(yoyoID)
        buf.writeInt(drops.size)

        for (drop in drops)
            writeBigItemStack(buf, drop)
    }

    fun onMessage(ctx: Supplier<NetworkEvent.Context>) {
        ctx.get().enqueueWork {
            val maybeYoyo = Minecraft.getInstance().world.getEntityByID(yoyoID)

            if (maybeYoyo is YoyoEntity) {
                maybeYoyo.collectedDrops.clear()
                Collections.addAll(maybeYoyo.collectedDrops, *drops)
            }
        }

        ctx.get().packetHandled = true
    }

    private fun writeBigItemStack(buf: PacketBuffer, stack: ItemStack) {
        if (stack.isEmpty) {
            buf.writeBoolean(false)
        } else {
            buf.writeBoolean(true)
            val item = stack.item
            buf.writeVarInt(Item.getIdFromItem(item))
            buf.writeVarInt(stack.count)
            var compoundnbt: CompoundNBT? = null

            if (item.isDamageable || item.shouldSyncTag()) {
                compoundnbt = stack.shareTag
            }

            buf.writeCompoundTag(compoundnbt)
        }
    }

    private fun readBigItemStack(buf: PacketBuffer): ItemStack {
        return if (!buf.readBoolean()) {
            ItemStack.EMPTY
        } else {
            val id = buf.readVarInt()
            val count = buf.readVarInt()
            val itemstack = ItemStack(Item.getItemById(id), count)
            itemstack.readShareTag(buf.readCompoundTag())
            itemstack
        }
    }
}
