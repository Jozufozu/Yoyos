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
import com.jozufozu.yoyos.common.api.RenderOrientation
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.Vector3f
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.util.*
import kotlin.math.atan2
import kotlin.math.sin

@OnlyIn(Dist.CLIENT)
class YoyoRenderer(renderManager: EntityRendererManager) : EntityRenderer<YoyoEntity>(renderManager) {
    private val itemRenderer: ItemRenderer = Minecraft.getInstance().itemRenderer
    private val random = Random()

    override fun render(yoyoEntity: YoyoEntity, yaw: Float, partialTicks: Float, matrixStack: MatrixStack, buffer: IRenderTypeBuffer, packedLight: Int) {
        val mcProfiler = Minecraft.getInstance().profiler
        mcProfiler.startSection("renderYoyo")

        matrixStack.push()
        matrixStack.translate(0.0, yoyoEntity.height / 2.0, 0.0)
        matrixStack.scale(.5f, .5f, .5f)

        val pointTo = yoyoEntity.getPlayerHandPos(partialTicks).subtract(yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ).normalize()

        val yaw = atan2(pointTo.x, pointTo.z) * -180 / Math.PI

        matrixStack.push()

        if (yoyoEntity.hasYoyo && yoyoEntity.yoyo.getRenderOrientation(yoyoEntity.yoyoStack) === RenderOrientation.Horizontal) {
            matrixStack.rotate(Vector3f.XP.rotationDegrees(-90f)) // be flat, like a lawnmower
        } else {
            matrixStack.rotate(Vector3f.YP.rotationDegrees(270f - yaw.toFloat())) // face away from player
        }

        matrixStack.rotate(Vector3f.ZP.rotationDegrees(yoyoEntity.getRotation(yoyoEntity.ticksExisted, partialTicks))) // spin around

        val ibakedmodel = itemRenderer.getItemModelWithOverrides(yoyoEntity.yoyoStack, yoyoEntity.world, null)
        itemRenderer.renderItem(yoyoEntity.yoyoStack, TransformType.GROUND, false, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, ibakedmodel)

        matrixStack.pop()

        if (yoyoEntity.isCollecting && yoyoEntity.collectedDrops.isNotEmpty()) {
            renderCollectedItems(yoyoEntity, matrixStack, buffer, partialTicks, packedLight)
        }

        matrixStack.pop()

        //renderChord(yoyoEntity, partialTicks)

        super.render(yoyoEntity, yaw.toFloat(), partialTicks, matrixStack, buffer, packedLight)

        mcProfiler.endSection()
    }

    fun renderCollectedItems(entity: YoyoEntity, matrixStack: MatrixStack, buffer: IRenderTypeBuffer, partialTicks: Float, packedLight: Int) {

        for ((i, drop) in entity.collectedDrops.withIndex()) {
            var count = drop.count
            val max = drop.maxStackSize

            while (count > 0) {
                doRenderItem(i, entity, drop, matrixStack, buffer, partialTicks, packedLight)
                count -= max
            }
        }
    }

    private fun getModelCount(stack: ItemStack): Int = when {
        stack.count > 48 -> 5
        stack.count > 32 -> 4
        stack.count > 16 -> 3
        stack.count > 1 -> 2
        else -> 1
    }

    fun doRenderItem(i: Int, yoyo: YoyoEntity, itemStack: ItemStack, matrixStack: MatrixStack, buffer: IRenderTypeBuffer, partialTicks: Float, packedLight: Int) {
        matrixStack.push()

        val seed = ((Item.getIdFromItem(itemStack.item) * 31 + i) * 31 + itemStack.count).toLong()

        this.random.setSeed(seed)

        val ibakedmodel = itemRenderer.getItemModelWithOverrides(itemStack, yoyo.world, null)
        val gui3d = ibakedmodel.isGui3d
        val modelCount = getModelCount(itemStack)

        val bobHeight = sin((yoyo.ticksExisted + partialTicks) / 10.0 + random.nextDouble()) * 0.1 + 0.1
        val yScale = ibakedmodel.itemCameraTransforms.getTransform(TransformType.GROUND).scale.y
        matrixStack.translate(0.0, bobHeight + 0.25f * yScale, 0.0)

        val rotation = (yoyo.ticksExisted + partialTicks) / 20.0f + random.nextFloat()
        matrixStack.rotate(Vector3f.YP.rotation(rotation))

        if (!gui3d) {
            val f7 = -0.0f * (modelCount - 1).toFloat() * 0.5f
            val f8 = -0.0f * (modelCount - 1).toFloat() * 0.5f
            val f9 = -0.09375f * (modelCount - 1).toFloat() * 0.5f
            matrixStack.translate(f7.toDouble(), f8.toDouble(), f9.toDouble())
        }

        for (k in 0 until modelCount) {
            matrixStack.push()
            if (k > 0) {
                if (gui3d) {
                    val f11 = (random.nextDouble() * 2.0 - 1.0) * 0.15
                    val f13 = (random.nextDouble() * 2.0 - 1.0) * 0.15
                    val f10 = (random.nextDouble() * 2.0 - 1.0) * 0.15
                    matrixStack.translate(f11, f13, f10)
                } else {
                    val f12 = (random.nextDouble() * 2.0 - 1.0) * 0.15 * 0.5
                    val f14 = (random.nextDouble() * 2.0 - 1.0) * 0.15 * 0.5
                    matrixStack.translate(f12, f14, 0.0)
                }
            }
            itemRenderer.renderItem(itemStack, TransformType.GROUND, false, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, ibakedmodel)
            matrixStack.pop()
            if (!gui3d) {
                matrixStack.translate(0.0, 0.0, 0.09375)
            }
        }

        matrixStack.pop()
    }

    override fun getEntityTexture(yoyoEntity: YoyoEntity): ResourceLocation {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE
    }


//    fun renderChord(entity: YoyoEntity, partialTicks: Float) {
//        var y = y
//        val thrower = entity.thrower as? PlayerEntity ?: return
//
//        y -= (1.6 - thrower.height) * 0.5
//        val tessellator = Tessellator.getInstance()
//        val bufferBuilder = tessellator.buffer
//
//        val rightHand = thrower.primaryHand == HandSide.RIGHT == (entity.hand == Hand.MAIN_HAND)
//
//        val handPos: Vec3d
//
//        if (Minecraft.getInstance().gameSettings.thirdPersonView != 0 || thrower.entityId != Minecraft.getInstance().player.entityId) {
//            var posX = interpolateValue(thrower.prevPosX, thrower.posX, partialTicks.toDouble())
//            var posY = interpolateValue(thrower.prevPosY, thrower.posY, partialTicks.toDouble()) + 1.272
//            var posZ = interpolateValue(thrower.prevPosZ, thrower.posZ, partialTicks.toDouble())
//            var bodyRotation = interpolateValue(thrower.prevCameraYaw, thrower.cameraYaw, partialTicks).toDouble()
//
//            bodyRotation = Math.toRadians(bodyRotation)
//
//            var rotation = bodyRotation
//
//            if (rightHand)
//                rotation += Math.PI
//
//            val shoulderRadius = 0.347
//            posX += cos(rotation) * shoulderRadius
//            posZ += sin(rotation) * shoulderRadius
//
//            var f = 1.0
//
//            if (thrower.pose == Pose.FALL_FLYING) {
//                f = thrower.motion.lengthSquared()
//                f = f / 0.2f
//                f = f * f * f
//            }
//
//            if (f < 1.0f) {
//                f = 1.0
//            }
//
//            val limbSwing = thrower.swingProgressInt.toFloat()
//            val limbSwingAmount = thrower.getSwingProgress(partialTicks)
//
//            var pitch = cos(limbSwing * 0.6662 + if (rightHand) 0.0 else Math.PI) * 2.0 * limbSwingAmount * 0.5 / f
//
//            pitch *= 0.5 - Math.PI / 10.0
//            if (thrower.isSneaking) {
//                pitch += 0.4
//                posY -= 0.4
//            }
//
//            pitch += (if (rightHand) -1.0 else 1.0) * sin((thrower.ticksExisted + partialTicks) * 0.067) * 0.05
//
//            val roll = Math.PI
//            val yaw = bodyRotation + cos((thrower.ticksExisted + partialTicks) * 0.09) * 0.05 + 0.05
//
//            posX += -1.0 * sin(roll) * cos(yaw) - cos(roll) * sin(pitch) * sin(yaw)
//            posY += cos(pitch) * cos(roll)
//            posZ += sin(roll) * sin(yaw) - cos(roll) * sin(pitch) * cos(yaw)
//
//            handPos = Vec3d(posX, posY, posZ)
//        } else {
//            var posX = interpolateValue(thrower.prevPosX, thrower.posX, partialTicks.toDouble())
//            var posY = interpolateValue(thrower.prevPosY, thrower.posY, partialTicks.toDouble()) + 1.1
//            var posZ = interpolateValue(thrower.prevPosZ, thrower.posZ, partialTicks.toDouble())
//
//            val rotationYaw = Math.toRadians(interpolateValue(thrower.prevRotationYaw, thrower.rotationYaw, partialTicks).toDouble())
//            val rotationPitch = Math.toRadians(interpolateValue(thrower.prevRotationPitch, thrower.rotationPitch, partialTicks).toDouble())
//
//            val mirror = (if (rightHand) -1 else 1).toDouble()
//            val radius = 0.1
//
//            val v = -sin(rotationPitch) * mirror * mirror * radius
//            val angle = rotationYaw + Math.PI * 0.5
//            posX += cos(rotationYaw) * mirror * radius + cos(angle) * v
//            posY += sin(-rotationPitch) * radius
//            posZ += sin(rotationYaw) * mirror * radius + sin(angle) * v
//
//            handPos = Vec3d(posX, posY, posZ)
//        }
//
//        val yoyoPosX = interpolateValue(entity.prevPosX, entity.posX, partialTicks.toDouble())
//        val yoyoPosY = interpolateValue(entity.prevPosY, entity.posY, partialTicks.toDouble()) - entity.height
//        val yoyoPosZ = interpolateValue(entity.prevPosZ, entity.posZ, partialTicks.toDouble())
//
//        val xDiff = handPos.x - yoyoPosX
//        val yDiff = handPos.y - yoyoPosY
//        val zDiff = handPos.z - yoyoPosZ
//
//        var color = 0xDDDDDD
//        if (entity.hasYoyo) {
//            color = entity.yoyo.getCordColor(entity.yoyoStack, entity.ticksExisted + partialTicks)
//        }
//
//        val stringR = (color shr 16 and 255) / 255f
//        val stringG = (color shr 8 and 255) / 255f
//        val stringB = (color and 255) / 255f
//
//        for (i in 0..1) {
//            bufferBuilder.begin(5, DefaultVertexFormats.POSITION_COLOR)
//            for (j in 0..24) {
//                var r = stringR
//                var g = stringG
//                var b = stringB
//
//                if (j % 2 == 0) {
//                    r *= 0.7f
//                    g *= 0.7f
//                    b *= 0.7f
//                }
//
//                val segment = j / 24.0
//                val zag = 0.0125 * (i % 2 * 2 - 1)
//                val x1 = x + xDiff * segment
//                val y1 = y + yDiff * (segment * segment + segment) * 0.5
//                val z1 = z + zDiff * segment
//
//                bufferBuilder.pos(x1 - 0.0125, y1, z1 + zag).color(r, g, b, 1.0f).endVertex()
//                bufferBuilder.pos(x1 + 0.0125, y1, z1 - zag).color(r, g, b, 1.0f).endVertex()
//            }
//            tessellator.draw()
//        }
//    }

    private fun interpolateValue(start: Double, end: Double, pct: Double): Double {
        return start + (end - start) * pct
    }

    private fun interpolateValue(start: Float, end: Float, pct: Float): Float {
        return start + (end - start) * pct
    }
}
