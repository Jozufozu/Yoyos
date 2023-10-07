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

import com.jozufozu.yoyos.common.YoyoEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.PlayerRenderer
import net.minecraft.util.Hand
import net.minecraft.util.HandSide
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.vector.Quaternion
import net.minecraft.util.math.vector.Vector3f
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.event.RenderHandEvent

@OnlyIn(Dist.CLIENT)
object YoyosClient {
    @JvmStatic fun onRenderHand(event: RenderHandEvent) {
        val mc = Minecraft.getInstance()

        val player = mc.player ?: return

        val yoyo = YoyoEntity.CASTERS[player.uniqueID]
        if (yoyo == null || yoyo.hand != event.hand) return

        if (player.isInvisible) return

        val matrixStack = event.matrixStack

        val handSide = if (event.hand == Hand.MAIN_HAND) player.primaryHand else player.primaryHand.opposite()

        val swingProgress = event.swingProgress

        matrixStack.push()
        val rightHand = handSide !== HandSide.LEFT
        val mirror = if (rightHand) 1.0f else -1.0f
        val f1 = MathHelper.sqrt(swingProgress)
        val f2 = -0.3f * MathHelper.sin(f1 * Math.PI.toFloat())
        val f3 = 0.4f * MathHelper.sin(f1 * (Math.PI.toFloat() * 2f))
        val f4 = -0.4f * MathHelper.sin(swingProgress * Math.PI.toFloat())
        matrixStack.translate((mirror * (f2 + 0.64000005f)).toDouble(), (f3 + -0.6f + event.equipProgress * -0.6f).toDouble(), (f4 + -0.71999997f).toDouble())
        matrixStack.rotate(Quaternion(Vector3f.YP, mirror * 45.0f, true))
        val f5 = MathHelper.sin(swingProgress * swingProgress * Math.PI.toFloat())
        val f6 = MathHelper.sin(f1 * Math.PI.toFloat())
        matrixStack.rotate(Quaternion(Vector3f.YP, mirror * f6 * 70.0f, true))
        matrixStack.rotate(Quaternion(Vector3f.ZP, mirror * f5 * -20.0f, true))
        mc.getTextureManager().bindTexture(player.locationSkin)
        matrixStack.translate((mirror * -1.0f).toDouble(), 3.6, 3.5)
        matrixStack.rotate(Quaternion(Vector3f.ZP, mirror * 120.0f, true))
        matrixStack.rotate(Quaternion(Vector3f.XP, 200f, true))
        matrixStack.rotate(Quaternion(Vector3f.YP, mirror * -135.0f, true))
        matrixStack.translate((mirror * 5.6f).toDouble(), 0.0, 0.0)

        val playerRenderer = mc.renderManager.getRenderer(player) as PlayerRenderer
        if (rightHand) {
            playerRenderer.renderRightArm(matrixStack, event.buffers, event.light, player)
        } else {
            playerRenderer.renderLeftArm(matrixStack, event.buffers, event.light, player)
        }

        matrixStack.pop()

        event.isCanceled = true
    }
}
