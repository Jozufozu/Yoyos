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
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Matrix4f
import com.mojang.math.Vector3f
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.decoration.HangingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.LightLayer
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OnlyIn(Dist.CLIENT)
class YoyoRenderer(context: EntityRendererProvider.Context) : EntityRenderer<YoyoEntity>(context) {
    private val itemRenderer: ItemRenderer = Minecraft.getInstance().itemRenderer
    private val random = Random()

    override fun render(
        yoyoEntity: YoyoEntity,
        yaw: Float,
        partialTicks: Float,
        matrixStack: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int
    ) {
        val mcProfiler = Minecraft.getInstance().profiler
        mcProfiler.push("renderYoyo")

        matrixStack.pushPose()
        val x = Mth.lerp(partialTicks.toDouble(), yoyoEntity.xOld, yoyoEntity.x).toFloat()
        val y = Mth.lerp(partialTicks.toDouble(), yoyoEntity.yOld, yoyoEntity.y).toFloat()
        val z = Mth.lerp(partialTicks.toDouble(), yoyoEntity.zOld, yoyoEntity.z).toFloat()

        matrixStack.translate(0.0, yoyoEntity.bbHeight * 0.5, 0.0)

        val pointTo = yoyoEntity.getPlayerHandPos(partialTicks)
        pointTo.add(-x, -y, -z)
        pointTo.normalize()

        val yaw = atan2(pointTo.x(), pointTo.z()) * -180 / Math.PI

        matrixStack.pushPose()

        if (yoyoEntity.hasYoyo && yoyoEntity.yoyo.getRenderOrientation(yoyoEntity.yoyoStack) === RenderOrientation.Horizontal) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90f)) // be flat, like a lawnmower
        } else {
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(270f - yaw.toFloat())) // face away from player
        }

        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(yoyoEntity.getRotation(yoyoEntity.tickCount, partialTicks))) // spin around
        matrixStack.translate(-yoyoEntity.bbWidth * 0.4, -yoyoEntity.bbHeight * 0.75, 0.0)

        val ibakedmodel = itemRenderer.getModel(yoyoEntity.yoyoStack, yoyoEntity.level, null, 42)
        itemRenderer.render(
            yoyoEntity.yoyoStack,
            ItemTransforms.TransformType.GROUND,
            false,
            matrixStack,
            buffer,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            ibakedmodel
        )

        matrixStack.popPose()

        if (yoyoEntity.isCollecting && yoyoEntity.collectedDrops.isNotEmpty()) {
            renderCollectedItems(yoyoEntity, matrixStack, buffer, partialTicks, packedLight)
        }

        matrixStack.popPose()

        renderCord(yoyoEntity, partialTicks, matrixStack, buffer, yoyoEntity.thrower)

        super.render(yoyoEntity, yaw.toFloat(), partialTicks, matrixStack, buffer, packedLight)

        mcProfiler.pop()
    }

    fun renderCollectedItems(
        entity: YoyoEntity,
        matrixStack: PoseStack,
        buffer: MultiBufferSource,
        partialTicks: Float,
        packedLight: Int
    ) {

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

    fun doRenderItem(
        i: Int,
        yoyo: YoyoEntity,
        itemStack: ItemStack,
        matrixStack: PoseStack,
        buffer: MultiBufferSource,
        partialTicks: Float,
        packedLight: Int
    ) {
        matrixStack.pushPose()

        val seed = ((Item.getId(itemStack.item) * 31 + i) * 31 + itemStack.count).toLong()

        this.random.setSeed(seed)

        val ibakedmodel = itemRenderer.getModel(itemStack, yoyo.level, null, 42)
        val gui3d = ibakedmodel.isGui3d
        val modelCount = getModelCount(itemStack)

        val bobHeight = sin((yoyo.tickCount + partialTicks) / 10.0 + random.nextDouble()) * 0.1 + 0.1
        val yScale = ibakedmodel.transforms.getTransform(ItemTransforms.TransformType.GROUND).scale.y()
        matrixStack.translate(0.0, bobHeight + 0.25f * yScale, 0.0)

        val rotation = (yoyo.tickCount + partialTicks) / 20.0f + random.nextFloat()
        matrixStack.mulPose(Vector3f.YP.rotation(rotation))

        if (!gui3d) {
            val f7 = -0.0f * (modelCount - 1).toFloat() * 0.5f
            val f8 = -0.0f * (modelCount - 1).toFloat() * 0.5f
            val f9 = -0.09375f * (modelCount - 1).toFloat() * 0.5f
            matrixStack.translate(f7.toDouble(), f8.toDouble(), f9.toDouble())
        }

        for (k in 0 until modelCount) {
            matrixStack.pushPose()
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
            itemRenderer.render(
                itemStack,
                ItemTransforms.TransformType.GROUND,
                false,
                matrixStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                ibakedmodel
            )
            matrixStack.popPose()
            if (!gui3d) {
                matrixStack.translate(0.0, 0.0, 0.09375)
            }
        }

        matrixStack.popPose()
    }

    override fun getTextureLocation(pEntity: YoyoEntity) = InventoryMenu.BLOCK_ATLAS

    private fun renderCord(
        yoyoEntity: YoyoEntity,
        partialTicks: Float,
        matrixStackIn: PoseStack,
        bufferIn: MultiBufferSource,
        thrower: Player
    ) {
        matrixStackIn.pushPose()
        val yaw = (Mth.lerp(
            partialTicks * 0.5f,
            thrower.rotationYaw,
            thrower.prevRotationYaw
        ) * (Math.PI.toFloat() / 180f)).toDouble()
        val pitch = (Mth.lerp(
            partialTicks * 0.5f,
            thrower.rotationPitch,
            thrower.prevRotationPitch
        ) * (Math.PI.toFloat() / 180f)).toDouble()

        val sinYaw: Double
        val cosYaw: Double
        val sinPitch: Double
        val cosPitch = cos(pitch)

        if (thrower is HangingEntity) {
            cosYaw = 0.0
            sinYaw = 0.0
            sinPitch = -1.0
        } else {
            cosYaw = cos(yaw)
            sinYaw = sin(yaw)
            sinPitch = sin(pitch)
        }


        y -= (1.6 - thrower.height) * 0.5

        val rightHand = thrower.mainArm == HumanoidArm.RIGHT == (yoyoEntity.hand == InteractionHand.MAIN_HAND)

        var handX = Mth.lerp(partialTicks.toDouble(), thrower.xOld, thrower.x)
        var handY = Mth.lerp(partialTicks.toDouble(), thrower.yOld, thrower.y)
        var handZ = Mth.lerp(partialTicks.toDouble(), thrower.zOld, thrower.z)

        if (isThirdPersonThrower(thrower)) {
            handY += 1.272

            var rotation = Mth.lerp(thrower.prevCameraYaw, thrower.cameraYaw, partialTicks).toDouble()

            if (rightHand)
                rotation += Math.PI

            val shoulderRadius = 0.347
            handX += cos(rotation) * shoulderRadius
            handZ += sin(rotation) * shoulderRadius

            var f = 1.0

            if (thrower.pose == Pose.FALL_FLYING) {
                f = thrower.deltaMovement.lengthSqr()
                f /= 0.2f
                f *= f * f
            }

            if (f < 1.0f) {
                f = 1.0
            }

            val limbSwing = thrower.swingTime.toFloat()
            val limbSwingAmount = thrower.getSwingProgress(partialTicks)

            var pitch = cos(limbSwing * 0.6662 + if (rightHand) 0.0 else Math.PI) * 2.0 * limbSwingAmount * 0.5 / f

            pitch *= 0.5 - Math.PI / 10.0
            if (thrower.isSneaking) {
                pitch += 0.4
                handY -= 0.4
            }

            pitch += (if (rightHand) -1.0 else 1.0) * sin((thrower.ticksExisted + partialTicks) * 0.067) * 0.05

            val roll = Math.PI
            val yaw = rotation + cos((thrower.tickCount + partialTicks) * 0.09) * 0.05 + 0.05

            handX += -1.0 * sin(roll) * cos(yaw) - cos(roll) * sin(pitch) * sin(yaw)
            handY += cos(pitch) * cos(roll)
            handZ += sin(roll) * sin(yaw) - cos(roll) * sin(pitch) * cos(yaw)
        } else {
            handY += 1.1

            val rotationYaw = Mth.lerp(partialTicks, thrower.xRot, thrower.xRotO).toDouble()
            val rotationPitch = Mth.lerp(partialTicks, thrower.yRot, thrower.yRotO).toDouble()

            val mirror = if (rightHand) -1.0 else 1.0
            val radius = 0.1

            val v = -sin(rotationPitch) * mirror * mirror * radius
            val angle = rotationYaw + Math.PI * 0.5
            handX += cos(rotationYaw) * mirror * radius + cos(angle) * v
            handY += sin(-rotationPitch) * radius
            handZ += sin(rotationYaw) * mirror * radius + sin(angle) * v
        }

        val xDiff = handX - x
        val yDiff = handY - y + yoyoEntity.height
        val zDiff = handZ - z

        var color = 0xDDDDDD
        if (yoyoEntity.hasYoyo) {
            color = yoyoEntity.yoyo.getCordColor(yoyoEntity.yoyoStack, yoyoEntity.ticksExisted + partialTicks)
        }

        val stringR = (color shr 16 and 255) / 255f
        val stringG = (color shr 8 and 255) / 255f
        val stringB = (color and 255) / 255f

        val diffX = (handX - yoyoX).toFloat()
        val diffY = (handY - yoyoY).toFloat()
        val diffZ = (handZ - yoyoZ).toFloat()

        val builder = bufferIn.getBuffer(RenderType.leash())
        val poseMatrix = matrixStackIn.last().pose()
        val distanceXZ = Mth.fastInvSqrt(diffX * diffX + diffZ * diffZ) * 0.025f / 2.0f
        val distanceZ = diffZ * distanceXZ
        val distanceX = diffX * distanceXZ
        val yoyoBlockLight = getBlockLightLevel(yoyoEntity, yoyoEntity.blockPosition())
        // This line copied from EntityRenderer::getBlockLightLevel because it's a protected method
        // and we're in a different generic specialization.
        val throwerBlockLight = if (thrower.isOnFire) 15 else thrower.level.getBrightness(
            LightLayer.BLOCK, BlockPos(
                thrower.getEyePosition(
                    partialTicks
                )
            )
        )

        val yoyoSkyLight: Int = yoyoEntity.level.getBrightness(
            LightLayer.SKY, BlockPos(
                yoyoEntity.getEyePosition(
                    partialTicks
                )
            )
        )
        val throwerSkyLight: Int = yoyoEntity.level.getBrightness(
            LightLayer.SKY, BlockPos(
                thrower.getEyePosition(
                    partialTicks
                )
            )
        )
        renderSide(
            builder,
            poseMatrix,
            diffX,
            diffY,
            diffZ,
            yoyoBlockLight,
            throwerBlockLight,
            yoyoSkyLight,
            throwerSkyLight,
            0.025f,
            0.025f,
            distanceZ,
            distanceX
        )
        renderSide(
            builder,
            poseMatrix,
            diffX,
            diffY,
            diffZ,
            yoyoBlockLight,
            throwerBlockLight,
            yoyoSkyLight,
            throwerSkyLight,
            0.025f,
            0.0f,
            distanceZ,
            distanceX
        )
        matrixStackIn.popPose()
    }

    private fun isThirdPersonThrower(thrower: Player) =
        !Minecraft.getInstance().options.cameraType.isFirstPerson || thrower.id != Minecraft.getInstance().player?.id

    fun renderSide(
        buffer: VertexConsumer,
        matrix: Matrix4f,
        diffX: Float,
        diffY: Float,
        diffZ: Float,
        yoyoBlockLight: Int,
        throwerBlockLight: Int,
        yoyoSkyLight: Int,
        throwerSkyLight: Int,
        yThing1: Float,
        yThing2: Float,
        xThing: Float,
        zThing: Float
    ) {
        for (segment in 0..23) {
            val fraction = segment.toFloat() / 23.0f
            val blockLight = Mth.lerp(fraction, yoyoBlockLight.toFloat(), throwerBlockLight.toFloat()).toInt()
            val skyLight = Mth.lerp(fraction, yoyoSkyLight.toFloat(), throwerSkyLight.toFloat()).toInt()
            val packedLight = LightTexture.pack(blockLight, skyLight)
            addVertexPair(
                buffer,
                matrix,
                packedLight,
                diffX,
                diffY,
                diffZ,
                yThing1,
                yThing2,
                24,
                segment,
                false,
                xThing,
                zThing
            )
            addVertexPair(
                buffer,
                matrix,
                packedLight,
                diffX,
                diffY,
                diffZ,
                yThing1,
                yThing2,
                24,
                segment + 1,
                true,
                xThing,
                zThing
            )
        }
    }

    fun addVertexPair(
        buffer: VertexConsumer,
        matrix: Matrix4f,
        packedLight: Int,
        diffX: Float,
        diffY: Float,
        diffZ: Float,
        yThing1: Float,
        yThing2: Float,
        numSegments: Int,
        segment: Int,
        orientation: Boolean,
        xThing: Float,
        zThing: Float
    ) {
        var r = 0.8f
        var g = 0.8f
        var b = 0.8f
        if (segment % 2 == 0) {
            r *= 0.7f
            g *= 0.7f
            b *= 0.7f
        }
        val fraction = segment.toFloat() / numSegments.toFloat()
        val x = diffX * fraction
        val y = diffY * (fraction * fraction + fraction) * 0.5f + (numSegments.toFloat() - segment.toFloat()) / (numSegments.toFloat())
        val z = diffZ * fraction
        if (!orientation) {
            buffer.pos(matrix, x + xThing, y + yThing1 - yThing2, z - zThing).color(r, g, b, 1.0f).lightmap(packedLight).endVertex()
        }
        buffer.pos(matrix, x - xThing, y + yThing2, z + zThing).color(r, g, b, 1.0f).lightmap(packedLight).endVertex()
        if (orientation) {
            buffer.pos(matrix, x + xThing, y + yThing1 - yThing2, z - zThing).color(r, g, b, 1.0f).lightmap(packedLight).endVertex()
        }
    }
}
