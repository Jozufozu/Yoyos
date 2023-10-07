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

import com.jozufozu.yoyos.common.api.IYoyo
import com.jozufozu.yoyos.common.init.ModEntityTypes
import com.jozufozu.yoyos.common.init.ModItems
import com.jozufozu.yoyos.network.CCollectedDropsPacket
import com.jozufozu.yoyos.network.YoyoNetwork
import com.mojang.math.Vector3d
import com.mojang.math.Vector3f
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraftforge.entity.IEntityAdditionalSpawnData
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.network.NetworkHooks
import net.minecraftforge.registries.DataSerializerEntry
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.HashMap
import kotlin.math.*

open class YoyoEntity(type: EntityType<*>, level: Level) : Entity(type, level), IEntityAdditionalSpawnData {
    var collectedDrops = ArrayList<ItemStack>()
    var numCollectedDrops = 0
    var needCollectedSync: Boolean = false

    lateinit var thrower: Player
        protected set
    val hasThrower: Boolean get() = this::thrower.isInitialized

    protected var yoyoStackLastTick: ItemStack = ItemStack.EMPTY

    lateinit var yoyo: IYoyo
        protected set
    val hasYoyo: Boolean get() = this::yoyo.isInitialized

    protected var attackCool: Int = 0
    protected var attackInterval: Int = 0
    protected var shouldResetCool: Boolean = false

    protected var canCancelRetract = true
    protected var retractionTimeout = 0

    protected var lastSlot = -1

    protected var shouldGetStats = true

    protected var doesBlockInteraction = true

    var yoyoStack: ItemStack
        get() = this.entityData.get(YOYO_STACK)
        set(stack) = this.entityData.set(YOYO_STACK, stack)

    var hand: InteractionHand
        get() = InteractionHand.values()[this.entityData.get(HAND).toInt()]
        set(hand) = this.entityData.set(HAND, hand.ordinal.toByte())

    var isRetracting: Boolean
        get() = this.entityData.get(RETRACTING)
        set(retracting) {
            if (canCancelRetract || !isRetracting) {
                this.entityData.set(RETRACTING, retracting)
            }
        }

    var maxTime: Int
        get() = this.entityData.get(MAX_TIME)
        set(duration) = this.entityData.set(MAX_TIME, duration)

    var remainingTime: Int
        get() = this.entityData.get(REMAINING_TIME)
        set(duration) = this.entityData.set(REMAINING_TIME, duration)

    var weight: Float
        get() = this.entityData.get(WEIGHT)
        set(weight) = this.entityData.set(WEIGHT, weight)

    var currentLength: Float
        get() = this.entityData.get(CURRENT_LENGTH)
        set(length) = this.entityData.set(CURRENT_LENGTH, length)

    var maxLength: Float
        get() = this.entityData.get(MAX_LENGTH)
        set(length) = this.entityData.set(MAX_LENGTH, length)

    var maxCollectedDrops: Int
        get() = this.entityData.get(MAX_COLLECTED_DROPS)
        set(numCollectedDrops) = this.entityData.set(MAX_COLLECTED_DROPS, numCollectedDrops)

    val isCollecting: Boolean
        get() = maxCollectedDrops > 0

    val throwerEyeHeight: Float get() = thrower.getStandingEyeHeight(thrower.pose, thrower.getDimensions(thrower.pose))

    init {
        noCulling = true
        isNoGravity = true

        this.entityData.define(YOYO_STACK, ItemStack.EMPTY)
        this.entityData.define(HAND, InteractionHand.MAIN_HAND.ordinal.toByte())
        this.entityData.define(RETRACTING, false)
        this.entityData.define(MAX_TIME, -1)
        this.entityData.define(REMAINING_TIME, -1)
        this.entityData.define(WEIGHT, 1.0f)
        this.entityData.define(CURRENT_LENGTH, 1.0f)
        this.entityData.define(MAX_LENGTH, 1.0f)
        this.entityData.define(MAX_COLLECTED_DROPS, 0)
    }

    constructor(level: Level) : this(ModEntityTypes.YOYO, level)

    constructor(level: Level, player: Player, hand: InteractionHand) : this(ModEntityTypes.YOYO, level, player, hand)

    constructor(type: EntityType<*>, level: Level, player: Player, hand: InteractionHand) : this(type, level) {
        thrower = player

        CASTERS[player.uuid] = this

        this.hand = hand

        val handPos = getPlayerHandPos(1f)
        setPos(handPos.x, handPos.y, handPos.z)

        if (!level.noCollision(this)) setPos(player.x, player.y + throwerEyeHeight, player.z)
    }

    override fun readAdditional(compound: CompoundTag) {
        collectedDrops.clear()
        val list = compound.getList("collectedDrops", 10)

        for (i in 0..list.size) {
            val nbt = list.getCompound(i)
            nbt.putByte("Count", 1.toByte())
            val stack = ItemStack.of(nbt)
            stack.count = nbt.getInt("count")
            collectedDrops.add(stack)
        }
    }

    override fun writeAdditional(compound: CompoundNBT) {
        val collected = ListNBT()

        for (itemStack in collectedDrops) {
            val stackTag = CompoundNBT()
            val itemId = itemStack.item.registryName
            stackTag.putString("id", itemId.toString())
            stackTag.putInt("count", itemStack.count)
            if (itemStack.hasTag()) itemStack.tag?.let {
                stackTag.put("tag", it)
            }
            collected.add(stackTag)
        }

        compound.put("collectedDrops", collected)
    }

    override fun getAddEntityPacket(): Packet<*> {
        NetworkHooks.getEntitySpawningPacket(this)
    }

    fun forceRetract() {
        isRetracting = true
        canCancelRetract = false
    }

    @JvmOverloads
    fun decrementRemainingTime(amount: Int = 1): Int {
        val out = remainingTime - amount
        remainingTime = out
        return out
    }

    fun doesBlockInteraction(): Boolean {
        return doesBlockInteraction
    }

    fun setInteractsWithBlocks(blockInteraction: Boolean) {
        doesBlockInteraction = blockInteraction
    }

    fun canAttack(): Boolean {
        return attackCool >= attackInterval
    }

    fun resetAttackCooldown() {
        shouldResetCool = true
    }

    fun getPlayerHandPos(partialTicks: Float): Vector3f {
        if (!hasThrower) return position()

        var yaw = thrower.rotationYaw
        var pitch = thrower.rotationPitch

        var posX = thrower.posX
        var posY = thrower.posY
        var posZ = thrower.posZ

        if (partialTicks != 1f) {
            yaw = interpolateValue(thrower.prevRotationYaw.toDouble(), yaw.toDouble(), partialTicks.toDouble()).toFloat()
            pitch = interpolateValue(thrower.prevRotationPitch.toDouble(), pitch.toDouble(), partialTicks.toDouble()).toFloat()

            posX = interpolateValue(thrower.prevPosX, posX, partialTicks.toDouble())
            posY = interpolateValue(thrower.prevPosY, posY, partialTicks.toDouble())
            posZ = interpolateValue(thrower.prevPosZ, posZ, partialTicks.toDouble())
        }

        val throwerLookOffsetX = cos(yaw * 0.01745329238474369)
        val throwerLookOffsetZ = sin(yaw * 0.01745329238474369)
        val throwerLookOffsetY = sin(pitch * 0.01745329238474369)
        val throwerLookWidth = cos(pitch * 0.01745329238474369)

        val side = if (thrower.primaryHand == HandSide.RIGHT == (hand == Hand.MAIN_HAND)) 1f else -1f

        return Vector3d(posX - throwerLookOffsetX * side.toDouble() * 0.4 - throwerLookOffsetZ * 0.5 * throwerLookWidth, posY + throwerEyeHeight - throwerLookOffsetY * 0.5 - 0.25, posZ - throwerLookOffsetZ * side.toDouble() * 0.4 + throwerLookOffsetX * 0.5 * throwerLookWidth)
    }

    open fun getRotation(age: Int, partialTicks: Float): Float {
        val maxTime = maxTime
        val ageInTicks: Float

        if (maxTime < 0)
            ageInTicks = age + partialTicks
        else
            ageInTicks = maxTime - remainingTime + partialTicks

        var multiplier = 35f

        if (maxTime >= 0) multiplier *= 2 - ageInTicks / maxTime.toFloat()

        return ageInTicks * multiplier
    }


    override fun tick() {
        this.lastTickPosX = this.posX
        this.lastTickPosY = this.posY
        this.lastTickPosZ = this.posZ
        super.tick()

        if (hasThrower && thrower.isAlive) {
            yoyo = checkAndGetYoyoObject() ?: return

            if (maxTime >= 0 && decrementRemainingTime() < 0) forceRetract()

            updateMotion()
            moveAndCollide()

            yoyo.onUpdate(yoyoStack, this)

            if (!level.isRemote && doesBlockInteraction()) levelInteraction()

            if (isCollecting) updateCapturedDrops()

            if (YoyosConfig.general.yoyoSwing.get()) handlePlayerPulling()

            resetOrIncrementAttackCooldown()
        } else
            remove()
    }

    protected fun checkAndGetYoyoObject(): IYoyo? {
        val hand = hand
        val yoyoStack = thrower.getHeldItem(hand)
        this.yoyoStack = yoyoStack

        val currentSlot = if (hand == Hand.MAIN_HAND) thrower.inventory.currentItem else -2

        val otherHand = thrower.getHeldItem(if (hand == Hand.MAIN_HAND) Hand.OFF_HAND else Hand.MAIN_HAND)

        if (thrower.uniqueID !in CASTERS || yoyoStack.item !is IYoyo || ticksExisted > 1 && (lastSlot != -1 && lastSlot != currentSlot || otherHand == yoyoStackLastTick)) {
            remove()
            return null
        }

        yoyoStackLastTick = yoyoStack

        if (yoyoStack.maxDamage < yoyoStack.damage && yoyoStack.item !== ModItems.CREATIVE_YOYO) {
            remove()
            return null
        }

        if (!level.isRemote && CASTERS[thrower.uniqueID] !== this) {
            CASTERS[thrower.uniqueID] = this
        }

        val yoyo = yoyoStack.item as IYoyo

        if (!level.isRemote && shouldGetStats) {
            maxCollectedDrops = yoyo.getMaxCollectedDrops(yoyoStack)
            attackInterval = yoyo.getAttackInterval(yoyoStack)
            val duration = yoyo.getDuration(yoyoStack)
            maxTime = duration
            remainingTime = duration

            val maxLength = yoyo.getLength(yoyoStack).toFloat()
            currentLength = maxLength
            this.maxLength = maxLength
            weight = yoyo.getWeight(yoyoStack).toFloat()

            setInteractsWithBlocks(yoyo.interactsWithBlocks(yoyoStack))

            shouldGetStats = false
        }

        lastSlot = currentSlot

        return yoyo
    }

    protected fun updateMotion() {
        var motion = getTarget().subtract(posX, posY + height / 2, posZ).scale(0.15 + 0.85 * 1.1.pow(-(weight * weight).toDouble()))

        if (inWater) {
            motion = motion.scale(yoyo.getWaterMovementModifier(yoyoStack).toDouble())
        }

        this.motion = motion

        onGround = true
    }

    fun moveAndCollide() {
        var yoyoBoundingBox = boundingBox
        val targetBoundingBox = yoyoBoundingBox.offset(motion)
        if (noClip) {
            val pos = targetBoundingBox.center
            setPosition(pos.x, targetBoundingBox.minY, pos.z)
            return
        }

        val union = targetBoundingBox.union(yoyoBoundingBox)

        val colliderBoxes = level.getCollisionShapes(null, union).flatMap { it.toBoundingBoxList().stream() }.collect(Collectors.toList())

        val entities = level.getEntitiesWithinAABBExcludingEntity(this, union)

        val steps = 25

        val dx = motion.x / steps
        val dy = motion.y / steps
        val dz = motion.z / steps

        for (step in 0 until steps) {

            var dx = dx
            var dy = dy
            var dz = dz

            for (box in colliderBoxes) {
                dx = calculateXOffset(box, yoyoBoundingBox, dx)
                dy = calculateYOffset(box, yoyoBoundingBox, dy)
                dz = calculateZOffset(box, yoyoBoundingBox, dz)
            }

            yoyoBoundingBox = yoyoBoundingBox.offset(dx, dy, dz)

            for (box in colliderBoxes) {
                if (box.intersects(yoyoBoundingBox)) {
                    dx = calculateXOffset(box, yoyoBoundingBox, dx)
                    dy = calculateYOffset(box, yoyoBoundingBox, dy)
                    dz = calculateZOffset(box, yoyoBoundingBox, dz)

                    yoyoBoundingBox = yoyoBoundingBox.offset(-dx, -dy, -dz)
                }
            }

            if (!level.isRemote) {
                val iterator = entities.listIterator()

                while (iterator.hasNext()) {
                    val entity = iterator.next()

                    if (entity === thrower) {
                        iterator.remove()
                        continue
                    }

                    if (entity.boundingBox.intersects(yoyoBoundingBox)) {
                        interactWithEntity(entity)

                        iterator.remove()
                    }
                }
            }
        }

        val pos = yoyoBoundingBox.center
        setPosition(pos.x, yoyoBoundingBox.minY, pos.z)
    }

    fun calculateXOffset(one: AxisAlignedBB, other: AxisAlignedBB, offsetX: Double): Double {
        var offsetX = offsetX
        if (other.maxY > one.minY && other.minY < one.maxY && other.maxZ > one.minZ && other.minZ < one.maxZ) {
            if (offsetX > 0.0 && other.maxX <= one.minX) {
                val d1 = one.minX - other.maxX

                if (d1 < offsetX) {
                    offsetX = d1
                }
            } else if (offsetX < 0.0 && other.minX >= one.maxX) {
                val d0 = one.maxX - other.minX

                if (d0 > offsetX) {
                    offsetX = d0
                }
            }

            return offsetX
        } else {
            return offsetX
        }
    }

    fun calculateYOffset(one: AxisAlignedBB, other: AxisAlignedBB, offsetY: Double): Double {
        var offsetY = offsetY
        if (other.maxX > one.minX && other.minX < one.maxX && other.maxZ > one.minZ && other.minZ < one.maxZ) {
            if (offsetY > 0.0 && other.maxY <= one.minY) {
                val d1 = one.minY - other.maxY

                if (d1 < offsetY) {
                    offsetY = d1
                }
            } else if (offsetY < 0.0 && other.minY >= one.maxY) {
                val d0 = one.maxY - other.minY

                if (d0 > offsetY) {
                    offsetY = d0
                }
            }

            return offsetY
        } else {
            return offsetY
        }
    }

    fun calculateZOffset(one: AxisAlignedBB, other: AxisAlignedBB, offsetZ: Double): Double {
        var offsetZ = offsetZ
        if (other.maxX > one.minX && other.minX < one.maxX && other.maxY > one.minY && other.minY < one.maxY) {
            if (offsetZ > 0.0 && other.maxZ <= one.minZ) {
                val d1 = one.minZ - other.maxZ

                if (d1 < offsetZ) {
                    offsetZ = d1
                }
            } else if (offsetZ < 0.0 && other.minZ >= one.maxZ) {
                val d0 = one.maxZ - other.minZ

                if (d0 > offsetZ) {
                    offsetZ = d0
                }
            }

            return offsetZ
        } else {
            return offsetZ
        }
    }

    open fun interactWithEntity(entity: Entity) {
        yoyo.entityInteraction(yoyoStack, thrower, hand, this, entity)
    }

    protected fun levelInteraction() {
        val pos = position

        val entityBox = boundingBox.grow(0.1)

        BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1))
                .map { Pair(it.toImmutable(), level.getBlockState(it)) }
                .filter { !it.second.isAir(level, it.first) }
                .filter { it.second
                            .getShape(level, it.first)
                            .toBoundingBoxList()
                            .any { bb -> bb.offset(it.first).intersects(entityBox) }
                }
                .forEach { yoyo.blockInteraction(yoyoStack, thrower, level, it.first, it.second, it.second.block, this) }
    }

    protected fun updateCapturedDrops() {
        // If we're on the client, we trust the server
        if (!level.isRemote && collectedDrops.isNotEmpty() && needCollectedSync) {
            val iterator = collectedDrops.iterator()

            val existing = HashMap<Item, ItemStack>()

            // We don't have to respect the items' max size here
            while (iterator.hasNext()) {
                val collectedDrop = iterator.next()

                if (!collectedDrop.isEmpty) {
                    if (collectedDrop.hasTag()) continue

                    val item = collectedDrop.item

                    val master = existing[item]

                    if (master != null && collectedDrop.isItemEqual(master)) {
                        master.count += collectedDrop.count
                        iterator.remove()
                    } else {
                        existing[item] = collectedDrop
                    }
                }
            }

            YoyoNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with { level.getChunkAt(position) }, CCollectedDropsPacket(this))

            needCollectedSync = false
        }
    }

    fun createItemDropOrCollect(drop: ItemStack, pos: BlockPos) {
        var remaining = drop

        if (isCollecting) {
            remaining = collectDrop(drop)

            if (remaining == ItemStack.EMPTY) return
        }

        val itemEntity = ItemEntity(level, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, remaining)
        itemEntity.setDefaultPickupDelay()
        level.addEntity(itemEntity)
    }

    /**
     * @return the amount of stack left uncollected
     */
    fun collectDrop(stack: ItemStack): ItemStack {
        if (!isCollecting) return stack

        val maxTake = maxCollectedDrops - numCollectedDrops

        val take = stack.split(maxTake)
        collectedDrops.add(take)
        needCollectedSync = true
        numCollectedDrops += take.count

        return stack
    }

    fun collectDrop(drop: ItemEntity?) {
        if (drop == null) return

        val stack = drop.item
        val countBefore = stack.count
        collectDrop(stack)

        if (countBefore == stack.count) return

        drop.item = stack

        if (stack.isEmpty) {
            drop.setInfinitePickupDelay()
            drop.remove()
        }
        level.playSound(null, drop.posX, drop.posY, drop.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 1.0f, ((rand.nextFloat() - rand.nextFloat()) * 0.7f + 1.0f) * 2.0f)
    }

    protected open fun handlePlayerPulling() {
        val dx = posX - thrower.posX

        val eyeHeight = throwerEyeHeight.toDouble()

        var dy = posY + height * 0.5 - (thrower.posY + eyeHeight)

        if (dy < 0 && currentLength < eyeHeight) dy += eyeHeight * 1.2

        val dz = posZ - thrower.posZ
        val distanceSqr = dx * dx + dy * dy + dz * dz

        if (distanceSqr > currentLength * currentLength) {
            val stretch = sqrt(distanceSqr) - currentLength
            val scale = min(0.04 * stretch * stretch, 0.4)
            thrower.addVelocity(dx * scale, dy * scale, dz * scale)
            thrower.fallDistance = 0f
            if (isRetracting) remove()
        }
    }

    protected fun resetOrIncrementAttackCooldown() {
        if (shouldResetCool) {
            attackCool = 0
            shouldResetCool = false
        } else {
            attackCool++
        }
    }

    override fun remove() {
        super.remove()

        val hasThrower = hasThrower
        if (hasThrower) {
            CASTERS.remove(thrower.uniqueID)
        }

        if (collectedDrops.isEmpty()) return

        if (!level.isRemote) {
            if (hasThrower) {
                val inventory = thrower.inventory
                collectedDrops
                        .filterNot(ItemStack::isEmpty)
                        .forEach {
                            inventory.placeItemBackInInventory(level, it)
                            level.playSound(null, thrower.posX, thrower.posY, thrower.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 0.2f, ((rand.nextFloat() - rand.nextFloat()) * 0.7f + 1.0f) * 2.0f)
                        }
            } else { // the yoyo was loaded into the level with items still attached
                for (drop in collectedDrops) {
                    if (drop != null && !drop.isEmpty) {
                        while (drop.count > 0) {
                            val itemStack = drop.split(drop.maxStackSize)

                            val entityitem = ItemEntity(level, posX, posY + height, posZ, itemStack)
                            entityitem.setDefaultPickupDelay()
                            entityitem.setVelocity(0.0, 0.0, 0.0)

                            level.addEntity(entityitem)
                        }
                    }
                }
            }

        }
        collectedDrops.clear()
    }

    override fun getTeam() = thrower.team

    protected open fun getTarget(): Vector3d {
        if (isRetracting) {
            val handPos = getPlayerHandPos(1f)
            val dX = this.posX - handPos.x
            val dY = this.posY - handPos.y
            val dZ = this.posZ - handPos.z

            if (dX * dX + dY * dY + dZ * dZ < 0.8 || retractionTimeout++ >= MAX_RETRACT_TIME) remove()

            return handPos
        } else {
            val eyePos = thrower.let { Vector3d(it.posX, it.posY + it.getStandingEyeHeight(it.pose, it.getSize(it.pose)), it.posZ) }
            val lookVec = thrower.getLook(1.0f)

            val cordLength = currentLength.toDouble()

            var target = Vector3d(eyePos.x + lookVec.x * cordLength, eyePos.y + lookVec.y * cordLength, eyePos.z + lookVec.z * cordLength)
            retractionTimeout = 0
            val rayTraceResult = getTargetLook(eyePos, target)

            if (rayTraceResult != null) target = rayTraceResult.hitVec

            return target
        }
    }

    private fun getTargetLook(from: Vector3d, to: Vector3d): RayTraceResult? {
        val distance = from.distanceTo(to)
        var objectMouseOver: RayTraceResult? = level.rayTraceBlocks(RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, thrower))
        var flag = false
        var d1 = distance

        if (distance > 3.0) {
            flag = true
        }

        if (objectMouseOver != null) {
            d1 = objectMouseOver.hitVec.distanceTo(from)
        }

        val vec3d1 = thrower.getLook(1f)
        var pointedEntity: Entity? = null
        var vec3d3: Vector3d? = null
        val expanded = thrower.boundingBox.expand(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance).expand(1.0, 1.0, 1.0)

        val list = level.getEntitiesInAABBexcluding(null, expanded) { entity -> entity !is PlayerEntity || !entity.isSpectator() && entity.canBeCollidedWith() }

        var d2 = d1

        for (entity in list) {
            if (entity == this || entity == thrower) continue

            val box = entity.boundingBox.grow(entity.collisionBorderSize.toDouble())
            val raytraceresult = box.rayTrace(from, to)

            if (box.contains(from)) {
                if (d2 >= 0.0) {
                    pointedEntity = entity
                    vec3d3 = raytraceresult.orElseGet { from }
                    d2 = 0.0
                }
            } else if (raytraceresult.isPresent) {
                val d3 = from.distanceTo(raytraceresult.get())

                if (d3 < d2 || d2 == 0.0) {
                    if (entity.lowestRidingEntity === thrower.lowestRidingEntity && !thrower.canRiderInteract()) {
                        if (d2 == 0.0) {
                            pointedEntity = entity
                            vec3d3 = raytraceresult.get()
                        }
                    } else {
                        pointedEntity = entity
                        vec3d3 = raytraceresult.get()
                        d2 = d3
                    }
                }
            }
        }

        if (vec3d3 != null) {
            if (pointedEntity != null && flag) {
                pointedEntity = null
                objectMouseOver = BlockRayTraceResult.createMiss(vec3d3, Direction.UP, BlockPos(vec3d3))
            }

            if (pointedEntity != null && objectMouseOver == null) {
                objectMouseOver = EntityRayTraceResult(pointedEntity, vec3d3)
            }
        }

        return objectMouseOver
    }

    override fun readSpawnData(additionalData: FriendlyByteBuf) {
        (level.getEntity(additionalData.readInt()) as? Player)?.let {
            thrower = it
            CASTERS[it.uuid] = this
        }
    }

    override fun writeSpawnData(buffer: FriendlyByteBuf) {
        buffer.writeInt(thrower.id)
    }

    companion object {
        @JvmField val CASTERS: HashMap<UUID, YoyoEntity> = HashMap()

        private val YOYO_STACK = SynchedEntityData.defineId(YoyoEntity::class.java, EntityDataSerializers.ITEM_STACK)
        private val HAND = SynchedEntityData.defineId(YoyoEntity::class.java, EntityDataSerializers.BYTE)
        private val RETRACTING = SynchedEntityData.defineId(YoyoEntity::class.java, EntityDataSerializers.BOOLEAN)
        private val MAX_TIME = SynchedEntityData.defineId(YoyoEntity::class.java, EntityDataSerializers.INT)
        private val REMAINING_TIME = SynchedEntityData.defineId(YoyoEntity::class.java, EntityDataSerializers.INT)
        private val WEIGHT = SynchedEntityData.defineId(YoyoEntity::class.java, EntityDataSerializers.FLOAT)
        private val CURRENT_LENGTH = SynchedEntityData.defineId(YoyoEntity::class.java, EntityDataSerializers.FLOAT)
        private val MAX_LENGTH = SynchedEntityData.defineId(YoyoEntity::class.java, EntityDataSerializers.FLOAT)
        private val MAX_COLLECTED_DROPS = SynchedEntityData.defineId(YoyoEntity::class.java, EntityDataSerializers.INT)

        protected const val MAX_RETRACT_TIME = 40

        @JvmStatic fun onLivingDrops(event: LivingDropsEvent) {
            val source = event.source
            val killer = source.entity

            if (killer !is Player || killer.level.isClientSide) return

            val yoyo = CASTERS[killer.uuid]

            if (yoyo == null || !yoyo.isCollecting) return

            event.drops.forEach(yoyo::collectDrop)
            event.isCanceled = true
        }

        protected fun interpolateValue(start: Double, end: Double, pct: Double): Double {
            return start + (end - start) * pct
        }
    }
}
