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
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraftforge.network.NetworkEvent
import java.util.*
import java.util.function.Supplier

class CCollectedDropsPacket {
    private val yoyoID: Int
    private val drops: Array<ItemStack>

    constructor(yoyo: YoyoEntity) {
        yoyoID = yoyo.id
        val drops = yoyo.collectedDrops

        this.drops = drops.toTypedArray()
    }

    constructor(buf: FriendlyByteBuf) {
        yoyoID = buf.readInt()
        val length = buf.readInt()

        drops = Array(length) { buf.readBigItemStack() }
    }

    fun encode(buf: FriendlyByteBuf) {
        buf.writeInt(yoyoID)
        buf.writeInt(drops.size)

        for (drop in drops)
            buf.writeBigItemStack(drop)
    }

    fun onMessage(ctx: Supplier<NetworkEvent.Context>) {
        ctx.get().enqueueWork {
            val maybeYoyo = Minecraft.getInstance().level?.getEntity(yoyoID)

            if (maybeYoyo is YoyoEntity) {
                maybeYoyo.collectedDrops.clear()
                Collections.addAll(maybeYoyo.collectedDrops, *drops)
            }
        }

        ctx.get().packetHandled = true
    }

}

private fun FriendlyByteBuf.readBigItemStack(): ItemStack {
    return if (!this.readBoolean()) {
        ItemStack.EMPTY
    } else {
        val id = this.readVarInt()
        val count = this.readVarInt()
        val itemstack = ItemStack(Item.byId(id), count)
        itemstack.readShareTag(this.readNbt())
        itemstack
    }
}

private fun FriendlyByteBuf.writeBigItemStack(stack: ItemStack) {
    if (stack.isEmpty) {
        this.writeBoolean(false)
    } else {
        this.writeBoolean(true)
        val item: Item = stack.item
        this.writeVarInt(Item.getId(item))
        // Copied from FriendlyByteBuf.writeItemStack, but uses VarInts instead of bytes for stack size
        this.writeVarInt(stack.count)
        var compoundtag: CompoundTag? = null
        if (item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt()) {
            compoundtag = stack.tag
        }
        this.writeNbt(compoundtag)
    }
}
