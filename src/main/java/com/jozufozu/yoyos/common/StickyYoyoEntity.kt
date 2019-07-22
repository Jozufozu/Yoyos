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

package com.jozufozu.yoyos.common

import com.jozufozu.yoyos.Yoyos
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class StickyYoyoEntity : YoyoEntity {

    private var stuck = false
    private var stuckSince = 0

    private var reelDirection: Int = 0

    constructor(type: EntityType<*>, world: World) : super(type, world) {}

    constructor(world: World) : super(Yoyos.EntityTypes.STICKY_YOYO, world) {}

    constructor(world: World, player: PlayerEntity, hand: Hand) : super(Yoyos.EntityTypes.STICKY_YOYO, world, player, hand) {}

    fun setReelDirection(reelDirection: Int) {
        this.reelDirection = reelDirection
    }

    override fun tick() {
        if (!world.isRemote) {
            setFlag(6, isGlowing)
        }

        baseTick()

        if (hasThrower && !thrower.isAlive) {
            yoyo = checkAndGetYoyoObject()

            if (yoyo == null) return

            val dx = thrower.posX - posX
            val dy = thrower.posY + thrower.getEyeHeight(thrower.pose) - (posY + height * 0.5)
            val dz = thrower.posZ - posZ
            val distanceSqr = dx * dx + dy * dy + dz * dz

            if (reelDirection < 0 && currentLength > 0.1 && distanceSqr < currentLength * currentLength + 8)
                currentLength -= 0.5f

            if (reelDirection > 0 && currentLength < maxLength) {
                if (distanceSqr < currentLength * currentLength + 2)
                    currentLength += 0.1f
                else
                    currentLength = MathHelper.sqrt(distanceSqr)
            }

            if (!isRetracting && !world.checkBlockCollision(boundingBox.grow(0.1))) {
                motion = Vec3d.ZERO

                if (!stuck) {
                    stuckSince = ticksExisted
                    currentLength = MathHelper.sqrt(distanceSqr)
                    world.playSound(null, posX, posY, posZ, Yoyos.Sounds.YOYO_STICK, SoundCategory.PLAYERS, 0.7f, 3.0f)
                    yoyo!!.damageItem(yoyoStack, hand, 1, thrower)
                }
                stuck = true

                handlePlayerPulling()
            } else {
                if (isRetracting && stuck) remove()

                if (maxTime >= 0 && decrementRemainingTime() < 0) forceRetract()

                updateMotion()

                moveAndCollide()

                if (!world.isRemote && doesBlockInteraction)
                    worldInteraction()

                stuck = false
            }

            if (isCollecting)
                updateCapturedDrops()

            resetOrIncrementAttackCooldown()
        } else
            remove()
    }

    override fun getRotation(age: Int, partialTicks: Float): Float {
        return if (stuck) super.getRotation(stuckSince, 0f) else super.getRotation(age - stuckSince, partialTicks)
    }
}
