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
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Pose
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.HandSide
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.Vec3d
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OnlyIn(Dist.CLIENT)
class YoyoRenderer(renderManager: EntityRendererManager) : EntityRenderer<YoyoEntity>(renderManager) {
    private val itemRenderer: ItemRenderer = Minecraft.getInstance().itemRenderer
    private val random = Random()

    override fun shouldRender(yoyoEntity: YoyoEntity, camera: ICamera, x: Double, y: Double, z: Double): Boolean {
        return true
    }

    override fun doRender(yoyoEntity: YoyoEntity, x: Double, y: Double, z: Double, entityYaw: Float, partialTicks: Float) {
        val mcProfiler = Minecraft.getInstance().profiler
        mcProfiler.startSection("renderYoyo")

        GlStateManager.pushMatrix()
        GlStateManager.translated(x, y + yoyoEntity.height / 2, z)
        GlStateManager.scaled(.5, .5, .5)

        val pointTo = yoyoEntity.getPlayerHandPos(partialTicks).subtract(yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ).normalize()

        val yaw = atan2(pointTo.x, pointTo.z) * -180 / Math.PI

        GlStateManager.pushMatrix()

        if (yoyoEntity.hasYoyo && yoyoEntity.yoyo.getRenderOrientation(yoyoEntity.yoyoStack) === RenderOrientation.Horizontal) {
            GlStateManager.rotated(-90.0, 1.0, 0.0, 0.0)         //be flat, like a lawnmower
        } else {
            GlStateManager.rotated(270 - yaw, 0.0, 1.0, 0.0)   //face away from player
        }

        GlStateManager.rotated(yoyoEntity.getRotation(yoyoEntity.ticksExisted, partialTicks).toDouble(), 0.0, 0.0, 1.0)    //spin around

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial()
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(yoyoEntity))
        }

        itemRenderer.renderItem(yoyoEntity.yoyoStack, TransformType.NONE)

        GlStateManager.popMatrix()

        if (yoyoEntity.isCollecting && !yoyoEntity.collectedDrops.isEmpty()) {
            renderCollectedItems(yoyoEntity, partialTicks)
        }

        GlStateManager.popMatrix()

        renderChord(yoyoEntity, x, y, z, partialTicks)

        if (this.renderOutlines) {
            GlStateManager.tearDownSolidRenderingTextureCombine()
            GlStateManager.disableColorMaterial()
        }

        super.doRender(yoyoEntity, x, y, z, entityYaw, partialTicks)

        mcProfiler.endSection()
    }

    fun renderCollectedItems(entity: YoyoEntity, partialTicks: Float) {
        var boundTexture = false
        if (this.bindEntityTexture(entity)) {
            this.renderManager.textureManager.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false)
            boundTexture = true
        }

        GlStateManager.enableRescaleNormal()
        GlStateManager.alphaFunc(516, 0.1f)
        GlStateManager.enableBlend()
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)

        for ((i, drop) in entity.collectedDrops.withIndex()) {
            var count = drop.count
            val max = drop.maxStackSize

            while (count > 0) {
                doRenderItem(i, entity, drop, partialTicks)
                count -= max
            }
        }

        GlStateManager.disableRescaleNormal()
        GlStateManager.disableBlend()
        this.bindEntityTexture(entity)

        if (boundTexture) {
            this.renderManager.textureManager.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap()
        }
    }

    private fun transformModelCount(yoyo: YoyoEntity, itemStack: ItemStack, partialTicks: Float, model: IBakedModel): Int {
        val gui3d = model.isGui3d
        val count = this.getModelCount(itemStack)

        val bob = sin((random.nextDouble() + yoyo.ticksExisted.toDouble() + partialTicks.toDouble()) / 10.0 + random.nextDouble() * Math.PI * 2.0) * 0.1 + 0.1

        val scale = model.itemCameraTransforms.getTransform(TransformType.GROUND).scale.y.toDouble()
        GlStateManager.translated(0.0, bob + 0.25 * scale, 0.0)

        if (gui3d || this.renderManager.options != null) {
            val angle = ((random.nextDouble() + yoyo.ticksExisted.toDouble() + partialTicks.toDouble()) / 20.0 + random.nextDouble() * Math.PI * 2.0) * (180.0 / Math.PI)
            GlStateManager.rotated(angle.toFloat().toDouble(), 0.0, 1.0, 0.0)
        }

        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f)
        return count
    }

    private fun getModelCount(stack: ItemStack): Int = when {
        stack.count > 48 -> 5
        stack.count > 32 -> 4
        stack.count > 16 -> 3
        stack.count > 1 -> 2
        else -> 1
    }

    fun doRenderItem(i: Int, yoyo: YoyoEntity, itemStack: ItemStack, partialTicks: Float) {
        GlStateManager.pushMatrix()

        val seed = ((Item.getIdFromItem(itemStack.item) * 31 + i) * 31 + itemStack.count).toLong()

        this.random.setSeed(seed)

        val bakedModel = this.itemRenderer.getModelWithOverrides(itemStack)
        val modelCount = this.transformModelCount(yoyo, itemStack, partialTicks, bakedModel)
        val gui3d = bakedModel.isGui3d

        if (!gui3d) {
            val f3 = -0.0f * (modelCount - 1).toFloat() * 0.5f
            val f4 = -0.0f * (modelCount - 1).toFloat() * 0.5f
            val f5 = -0.09375f * (modelCount - 1).toFloat() * 0.5f
            GlStateManager.translated(f3.toDouble(), f4.toDouble(), f5.toDouble())
        }

        for (k in 0 until modelCount) {
            if (gui3d) {
                GlStateManager.pushMatrix()

                if (k > 0) {
                    val f7 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f
                    val f9 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f
                    val f6 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f
                    GlStateManager.translated(f7.toDouble(), f9.toDouble(), f6.toDouble())
                }

                this.itemRenderer.renderItem(itemStack, TransformType.GROUND)
                GlStateManager.popMatrix()
            } else {
                GlStateManager.pushMatrix()

                if (k > 0) {
                    val f8 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f
                    val f10 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f
                    GlStateManager.translated(f8.toDouble(), f10.toDouble(), 0.0)
                }

                this.itemRenderer.renderItem(itemStack, TransformType.GROUND)
                GlStateManager.popMatrix()
                GlStateManager.translated(0.0, 0.0, 0.09375)
            }
        }

        GlStateManager.popMatrix()
    }

    override fun getEntityTexture(yoyoEntity: YoyoEntity): ResourceLocation {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE
    }

    companion object {

        fun renderChord(entity: YoyoEntity, x: Double, y: Double, z: Double, partialTicks: Float) {
            var y = y
            val thrower = entity.thrower as? PlayerEntity ?: return

            y -= (1.6 - thrower.height) * 0.5
            val tessellator = Tessellator.getInstance()
            val bufferBuilder = tessellator.buffer

            val rightHand = thrower.primaryHand == HandSide.RIGHT == (entity.hand == Hand.MAIN_HAND)

            val handPos: Vec3d

            if (Minecraft.getInstance().gameSettings.thirdPersonView != 0 || thrower.entityId != Minecraft.getInstance().player.entityId) {
                var posX = interpolateValue(thrower.prevPosX, thrower.posX, partialTicks.toDouble())
                var posY = interpolateValue(thrower.prevPosY, thrower.posY, partialTicks.toDouble()) + 1.272
                var posZ = interpolateValue(thrower.prevPosZ, thrower.posZ, partialTicks.toDouble())
                var bodyRotation = interpolateValue(thrower.prevCameraYaw, thrower.cameraYaw, partialTicks).toDouble()

                bodyRotation = Math.toRadians(bodyRotation)

                var rotation = bodyRotation

                if (rightHand)
                    rotation += Math.PI

                val shoulderRadius = 0.347
                posX += cos(rotation) * shoulderRadius
                posZ += sin(rotation) * shoulderRadius

                var f = 1.0

                if (thrower.pose == Pose.FALL_FLYING) {
                    f = thrower.motion.lengthSquared()
                    f = f / 0.2f
                    f = f * f * f
                }

                if (f < 1.0f) {
                    f = 1.0
                }

                val limbSwing = thrower.swingProgressInt.toFloat()
                val limbSwingAmount = thrower.getSwingProgress(partialTicks)

                var pitch = cos(limbSwing * 0.6662 + if (rightHand) 0.0 else Math.PI) * 2.0 * limbSwingAmount * 0.5 / f

                pitch *= 0.5 - Math.PI / 10.0
                if (thrower.isSneaking) {
                    pitch += 0.4
                    posY -= 0.4
                }

                pitch += (if (rightHand) -1.0 else 1.0) * sin((thrower.ticksExisted + partialTicks) * 0.067) * 0.05

                val roll = Math.PI
                val yaw = bodyRotation + cos((thrower.ticksExisted + partialTicks) * 0.09) * 0.05 + 0.05

                posX += -1.0 * sin(roll) * cos(yaw) - cos(roll) * sin(pitch) * sin(yaw)
                posY += cos(pitch) * cos(roll)
                posZ += sin(roll) * sin(yaw) - cos(roll) * sin(pitch) * cos(yaw)

                handPos = Vec3d(posX, posY, posZ)
            } else {
                var posX = interpolateValue(thrower.prevPosX, thrower.posX, partialTicks.toDouble())
                var posY = interpolateValue(thrower.prevPosY, thrower.posY, partialTicks.toDouble()) + 1.1
                var posZ = interpolateValue(thrower.prevPosZ, thrower.posZ, partialTicks.toDouble())

                val rotationYaw = Math.toRadians(interpolateValue(thrower.prevRotationYaw, thrower.rotationYaw, partialTicks).toDouble())
                val rotationPitch = Math.toRadians(interpolateValue(thrower.prevRotationPitch, thrower.rotationPitch, partialTicks).toDouble())

                val mirror = (if (rightHand) -1 else 1).toDouble()
                val radius = 0.1

                val v = -sin(rotationPitch) * mirror * mirror * radius
                val angle = rotationYaw + Math.PI * 0.5
                posX += cos(rotationYaw) * mirror * radius + cos(angle) * v
                posY += sin(-rotationPitch) * radius
                posZ += sin(rotationYaw) * mirror * radius + sin(angle) * v

                handPos = Vec3d(posX, posY, posZ)
            }

            val yoyoPosX = interpolateValue(entity.prevPosX, entity.posX, partialTicks.toDouble())
            val yoyoPosY = interpolateValue(entity.prevPosY, entity.posY, partialTicks.toDouble()) - entity.height
            val yoyoPosZ = interpolateValue(entity.prevPosZ, entity.posZ, partialTicks.toDouble())

            val xDiff = handPos.x - yoyoPosX
            val yDiff = handPos.y - yoyoPosY
            val zDiff = handPos.z - yoyoPosZ

            GlStateManager.disableTexture()
            GlStateManager.disableLighting()
            GlStateManager.disableCull()

            var color = 0xDDDDDD
            if (entity.hasYoyo) {
                color = entity.yoyo.getCordColor(entity.yoyoStack, entity.ticksExisted + partialTicks)
            }

            val stringR = (color shr 16 and 255) / 255f
            val stringG = (color shr 8 and 255) / 255f
            val stringB = (color and 255) / 255f

            for (i in 0..1) {
                bufferBuilder.begin(5, DefaultVertexFormats.POSITION_COLOR)
                for (j in 0..24) {
                    var r = stringR
                    var g = stringG
                    var b = stringB

                    if (j % 2 == 0) {
                        r *= 0.7f
                        g *= 0.7f
                        b *= 0.7f
                    }

                    val segment = j / 24.0
                    val zag = 0.0125 * (i % 2 * 2 - 1)
                    val x1 = x + xDiff * segment
                    val y1 = y + yDiff * (segment * segment + segment) * 0.5
                    val z1 = z + zDiff * segment

                    bufferBuilder.pos(x1 - 0.0125, y1, z1 + zag).color(r, g, b, 1.0f).endVertex()
                    bufferBuilder.pos(x1 + 0.0125, y1, z1 - zag).color(r, g, b, 1.0f).endVertex()
                }
                tessellator.draw()
            }

            GlStateManager.enableLighting()
            GlStateManager.enableTexture()
            GlStateManager.enableCull()
        }

        private fun interpolateValue(start: Double, end: Double, pct: Double): Double {
            return start + (end - start) * pct
        }

        private fun interpolateValue(start: Float, end: Float, pct: Float): Float {
            return start + (end - start) * pct
        }
    }
}
