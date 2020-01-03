package com.jozufozu.yoyos.compat.botania

import com.jozufozu.yoyos.common.YoyoEntity
import com.jozufozu.yoyos.common.init.ModEntityTypes
import com.jozufozu.yoyos.network.CUpdateTargetPacket
import com.jozufozu.yoyos.network.YoyoNetwork
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.item.ItemEntity
import net.minecraft.entity.passive.AmbientEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.RayTraceContext
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.network.PacketDistributor
import vazkii.botania.api.mana.ManaItemHandler


class ChaserYoyoEntity(entityType: EntityType<*>, world: World) : YoyoEntity(entityType, world) {
    constructor(world: World): this(ChaserYoyoEntity.type, world)
    constructor(world: World, player: PlayerEntity, hand: Hand) : this(ModEntityTypes.YOYO, world, player, hand)
    constructor(type: EntityType<*>, world: World, player: PlayerEntity, hand: Hand) : this(type, world) {
        thrower = player

        CASTERS[player.uniqueID] = this

        this.hand = hand

        val handPos = getPlayerHandPos(1f)
        setPosition(handPos.x, handPos.y, handPos.z)

        if (!world.areCollisionShapesEmpty(this)) setPosition(player.posX, player.posY + throwerEyeHeight, player.posZ)
    }

    var target: Entity? = null
        set(value) {
            field = value
            if (!world.isRemote) {
                chasingTicks = 0
                YoyoNetwork.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with { world.getChunkAt(position) }, CUpdateTargetPacket(this, field))
            }
        }

    var chasingTicks: Int = 0

    override fun tick() {
        super.tick()
        //if (target != null) particles()
        if (!world.isRemote) {
            if (target != null) {
                if (chasingTicks++ > 20) target = null
            } else if (chasingTicks > 0) {
                chasingTicks = 0
            }
        }
    }

    override fun interactWithEntity(entity: Entity) {
        if (attackCool >= attackInterval && entity === target) target = null
        super.interactWithEntity(entity)
    }

    override fun getTarget(): Vec3d {
        if (!isRetracting)
            target?.takeIf { it.isAlive }?.let { return Vec3d(it.posX, it.posY + it.height * 0.5, it.posZ) }

        return super.getTarget()
    }

    override fun handlePlayerPulling() {
        if (target == null) super.handlePlayerPulling()
    }

    fun acquireTargetEntity() {
        if (isRetracting || !ManaItemHandler.requestManaExact(yoyoStack, thrower, 200, false)) return

        val searchBox = AxisAlignedBB(thrower.posX - maxLength, thrower.posY - maxLength, thrower.posZ - maxLength, thrower.posX + maxLength, thrower.posY + maxLength, thrower.posZ + maxLength)
        val chaseable = world.getEntitiesInAABBexcluding(this, searchBox)
            { entity: Entity? -> isCollecting && entity is ItemEntity || entity is LivingEntity }

        var bestTarget: Entity? = null
        var maxCloseness = 0.0
        for (testEntity in chaseable) {
            if (!testEntity.isAlive || testEntity === thrower) continue
            if (testEntity is LivingEntity && testEntity.deathTime > 0) continue

            val testVec = Vec3d(testEntity.posX - thrower.posX, testEntity.posY - thrower.posY + testEntity.height * 0.5, testEntity.posZ - thrower.posZ)
            if (testVec.lengthSquared() > maxLength * maxLength) continue  // It's farther away than we can reach

            val closeness = testVec.normalize().dotProduct(thrower.lookVec) * when (testEntity) {
                is AnimalEntity -> 0.5
                is ItemEntity -> 0.7
                !is AmbientEntity -> 2.0 // If it's not an animal or an item, it's probably a monster
                else -> 1.0
            }

            if (closeness > maxCloseness) {
                if (canEntityBeSeen(testEntity) && thrower.canEntityBeSeen(testEntity)) {
                    maxCloseness = closeness
                    bestTarget = testEntity
                }
            }
        }

        if (bestTarget !== target) {
            target = bestTarget
            ManaItemHandler.requestManaExact(yoyoStack, thrower, 200, true)
            world.playSound(null, thrower.posX, thrower.posY + thrower.eyeHeight, thrower.posZ, BotaniaSounds.chase, SoundCategory.PLAYERS, 0.4f, 1.2f + world.rand.nextFloat() * 0.6f)
        }
    }

//    private fun particles() {
//        if (world.isRemote) {
//            val color = Color(2162464)
//            var r = color.red.toFloat() / 255.0f
//            var g = color.green.toFloat() / 255.0f
//            var b = color.blue.toFloat() / 255.0f
//            val osize = 0.4f
//            var size: Float
//            val monocle: Boolean = Botania.proxy.isClientPlayerWearingMonocle
//            if (monocle) {
//                Botania.proxy.(false)
//            }
//            if (ConfigHandler.CLIENT.subtlePowerSystem) {
//                Botania.proxy.lightningFX(posX, posY + height * 0.5, posZ, r, g, b, 0.1f * osize, (Math.random() - 0.5).toFloat() * 0.02f, (Math.random() - 0.5).toFloat() * 0.02f, (Math.random() - 0.5).toFloat() * 0.01f)
//            } else {
//                val or = r
//                val og = g
//                val ob = b
//                val luminance = 0.2126 * r.toDouble() + 0.7152 * g.toDouble() + 0.0722 * b.toDouble()
//                val savedPosX = posX
//                val savedPosY = posY
//                val savedPosZ = posZ
//                var currentPos: Vector3 = Vector3.fromEntity(this)
//                val oldPos = Vector3(prevPosX, prevPosY, prevPosZ)
//                var diffVec: Vector3 = oldPos.subtract(currentPos)
//                val diffVecNorm: Vector3 = diffVec.normalize()
//                val distance = 0.095
//                do {
//                    if (luminance < 0.1) {
//                        r = or + Math.random().toFloat() * 0.125f
//                        g = og + Math.random().toFloat() * 0.125f
//                        b = ob + Math.random().toFloat() * 0.125f
//                    }
//                    size = osize + (Math.random().toFloat() - 0.5f) * 0.065f + Math.sin(Random(entityUniqueID.mostSignificantBits).nextInt(9001) as Double).toFloat() * 0.4f
//                    Botania.proxy.wispFX(posX, posY + height * 0.5, posZ, r, g, b, 0.2f * size, -this.motionX as Float * 0.01f, -this.motionY as Float * 0.01f, -this.motionZ as Float * 0.01f)
//                    posX += diffVecNorm.x * distance
//                    posY += diffVecNorm.y * distance
//                    posZ += diffVecNorm.z * distance
//                    currentPos = Vector3.fromEntity(this)
//                    diffVec = oldPos.subtract(currentPos)
//                } while (!this.getEntityData().hasKey("orbit") && Math.abs(diffVec.mag()) > distance)
//                Botania.proxy.wispFX(posX, posY + height * 0.5, posZ, or, og, ob, 0.1f * size, (Math.random() - 0.5).toFloat() * 0.06f, (Math.random() - 0.5).toFloat() * 0.06f, (Math.random() - 0.5).toFloat() * 0.06f)
//                posX = savedPosX
//                posY = savedPosY
//                posZ = savedPosZ
//            }
//            if (monocle) {
//                Botania.proxy.setWispFXDepthTest(true)
//            }
//        }
//    }

    /**
     * returns true if the entity provided in the argument can be seen. (Raytrace)
     */
    fun canEntityBeSeen(entityIn: Entity): Boolean {
        val from = Vec3d(posX, posY + height * 0.5, posZ)
        val to = Vec3d(entityIn.posX, entityIn.posY + entityIn.eyeHeight.toDouble(), entityIn.posZ)
        val context = RayTraceContext(from, to, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this)
        return world.rayTraceBlocks(context).type == RayTraceResult.Type.MISS
    }

    companion object {
        val type: EntityType<*> = EntityType.Builder.create(::ChaserYoyoEntity, EntityClassification.MISC)
                .disableSummoning()
                .size(0.25f, 0.25f)
                .setTrackingRange(64)
                .setUpdateInterval(1)
                .setShouldReceiveVelocityUpdates(true)
                .setCustomClientFactory { _, world -> ChaserYoyoEntity(world) }
                .build("yoyo")
                .setRegistryName("yoyo")

        fun registerEntityTypes(event: RegistryEvent.Register<EntityType<*>>) {
            event.registry.register(type)
        }
    }
}