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

import com.jozufozu.yoyos.common.init.ModEntityTypes
import com.jozufozu.yoyos.common.init.ModSounds
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class StickyYoyoEntity : YoyoEntity {

    private var stuck = false
    private var stuckSince = 0

    var reelDirection: Byte
        get() = this.dataManager.get(REEL_DIRECTION)
        set(dir) = this.dataManager.set(REEL_DIRECTION, dir)

    constructor(type: EntityType<*>, world: World) : super(type, world)

    constructor(world: World) : super(ModEntityTypes.STICKY_YOYO, world)

    constructor(world: World, player: PlayerEntity, hand: Hand) : super(ModEntityTypes.STICKY_YOYO, world, player, hand)

    override fun registerData() {
        super.registerData()
        this.dataManager.register(REEL_DIRECTION, 0)
    }

    override fun tick() {
        if (!world.isRemote) {
            setFlag(6, isGlowing)
        }

        baseTick()
        this.lastTickPosX = this.posX
        this.lastTickPosY = this.posY
        this.lastTickPosZ = this.posZ

        if (hasThrower && thrower.isAlive) {
            yoyo = checkAndGetYoyoObject() ?: return

            val dx = thrower.posX - posX
            val dy = thrower.posY + throwerEyeHeight - (posY + height * 0.5)
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

            if (!isRetracting && world.checkBlockCollision(boundingBox.grow(0.1))) {
                motion = Vec3d.ZERO

                if (!stuck) {
                    stuckSince = ticksExisted
                    currentLength = MathHelper.sqrt(distanceSqr)
                    world.playSound(null, posX, posY, posZ, ModSounds.yoyoStick, SoundCategory.PLAYERS, 0.7f, 3.0f)
                    yoyo.damageItem(yoyoStack, hand, 1, thrower)
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

    companion object {
        private val REEL_DIRECTION = EntityDataManager.createKey(StickyYoyoEntity::class.java, DataSerializers.BYTE)
    }
}
