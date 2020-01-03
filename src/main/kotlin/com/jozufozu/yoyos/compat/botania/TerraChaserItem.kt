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
package com.jozufozu.yoyos.compat.botania

import com.jozufozu.yoyos.Yoyos
import com.jozufozu.yoyos.common.YoyoEntity
import com.jozufozu.yoyos.common.YoyosConfig
import com.jozufozu.yoyos.network.SAcquireTargetPacket
import com.jozufozu.yoyos.network.YoyoNetwork
import net.minecraft.entity.LivingEntity
import net.minecraft.item.IItemTier
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty
import net.minecraftforge.eventbus.api.SubscribeEvent
import vazkii.botania.common.core.helper.ItemNBTHelper

class TerraChaserItem(name: String, material: IItemTier, manaPerDamage: Int, properties: Properties) : ManaYoyoItem(name, material, manaPerDamage, properties, ::ChaserYoyoEntity) {
    init {
        addPropertyOverride(ResourceLocation(Yoyos.MODID, "lined")) { itemStack: ItemStack, world: World?, entityLivingBase: LivingEntity? -> if (isLined(itemStack)) 1.0f else 0.0f }
        MinecraftForge.EVENT_BUS.register(this)

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    fun leftClick(evt: LeftClickEmpty) {
        val yoyoEntity = YoyoEntity.CASTERS[evt.player.uniqueID]
        if (yoyoEntity != null && yoyoEntity.yoyo === this) {
            YoyoNetwork.CHANNEL.sendToServer(SAcquireTargetPacket())
        }
    }

    override fun getMaxCollectedDrops(yoyo: ItemStack): Int {
        var out = super.getMaxCollectedDrops(yoyo)
        if (isLined(yoyo)) out += 64
        return out
    }

    override fun getAttackDamage(yoyo: ItemStack): Double {
        return YoyosConfig.botaniaYoyos.terrasteelYoyo.damage.get()
    }

    override fun getWeight(yoyo: ItemStack): Double {
        return YoyosConfig.botaniaYoyos.terrasteelYoyo.weight.get()
    }

    override fun getLength(yoyo: ItemStack): Double {
        return YoyosConfig.botaniaYoyos.terrasteelYoyo.length.get()
    }

    override fun getDuration(yoyo: ItemStack): Int {
        return YoyosConfig.botaniaYoyos.terrasteelYoyo.duration.get()
    }

    companion object {
        fun isLined(stack: ItemStack): Boolean {
            return ItemNBTHelper.getBoolean(stack, "lined", false)
        }

        fun setLined(stack: ItemStack?) {
            ItemNBTHelper.setBoolean(stack, "lined", true)
        }
    }
}