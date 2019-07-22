package com.jozufozu.yoyos.common

import net.minecraft.block.*
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.CreatureAttribute
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.ItemEntity
import net.minecraft.entity.passive.SheepEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.HoeItem
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.item.Items
import net.minecraft.network.play.server.SEntityVelocityPacket
import net.minecraft.particles.ParticleTypes
import net.minecraft.potion.Effects
import net.minecraft.stats.Stats
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameRules
import net.minecraft.world.ServerWorld
import net.minecraft.world.World
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameters
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.IPlantable
import net.minecraftforge.common.IShearable
import net.minecraftforge.event.ForgeEventFactory
import kotlin.math.roundToInt

object Interactions {
    private val SHEARS: ItemStack by lazy { ItemStack(Items.SHEARS) }

    fun collectItem(yoyo: ItemStack, player: PlayerEntity, hand: Hand, yoyoEntity: YoyoEntity, targetEntity: Entity): Boolean {
        if (targetEntity is ItemEntity && yoyoEntity.isCollecting) {
            yoyoEntity.collectDrop(targetEntity)
            return true
        }
        return false
    }

    fun shearEntity(yoyo: ItemStack, player: PlayerEntity, hand: Hand, yoyoEntity: YoyoEntity, targetEntity: Entity): Boolean {
        if (targetEntity is SheepEntity) {
            val world = targetEntity.world
            val shearable = targetEntity as IShearable
            val pos = BlockPos(targetEntity.posX, targetEntity.posY, targetEntity.posZ)

            if (shearable.isShearable(yoyo, world, pos)) {
                yoyoEntity.yoyo!!.damageItem(yoyo, hand, 1, player)
                val stacks = shearable.onSheared(yoyo, world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, yoyo))

                for (stack in stacks) {
                    yoyoEntity.createItemDropOrCollect(stack, pos)
                }

                yoyoEntity.decrementRemainingTime(10)
            }

            return true
        }

        return false
    }

    fun till(yoyo: ItemStack, player: PlayerEntity, pos: BlockPos, state: BlockState, block: Block, yoyoEntity: YoyoEntity): Boolean {
        val world = yoyoEntity.world

        if (pos.y + 0.9 < yoyoEntity.posY) {
            state.getCollisionShape(world, pos).rayTrace(yoyoEntity.positionVector, Vec3d(pos).add(0.5, 0.5, 0.5), pos)?.let {
                val hook = ForgeEventFactory.onHoeUse(ItemUseContext(player, yoyoEntity.hand, it))
                if (hook != 0) return hook > 0
            }

            if (world.isAirBlock(pos.up())) {
                val tillState = HoeItem.HOE_LOOKUP[block]
                if (tillState != null) {
                    world.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 1.0f)
                    if (!world.isRemote) {
                        world.setBlockState(pos, tillState, 11)

                        yoyoEntity.yoyo!!.damageItem(yoyo, yoyoEntity.hand, 1, player)
                    }

                    return true
                }
            }
        }

        return false
    }

    fun garden(yoyo: ItemStack, player: PlayerEntity, pos: BlockPos, state: BlockState, block: Block, yoyoEntity: YoyoEntity): Boolean {
        val world = yoyoEntity.world
        if (block is IShearable) {
            val shearable = block as IShearable
            if (shearable.isShearable(yoyo, world, pos)) {
                val stacks = Block.getDrops(state, world as ServerWorld, pos, world.getTileEntity(pos), player, SHEARS)

                if (!world.gameRules.getBoolean(GameRules.DO_TILE_DROPS)) stacks.clear()

                if (yoyoEntity.world.removeBlock(pos, false)) {
                    block.onPlayerDestroy(world, pos, state)

                    yoyoEntity.yoyo!!.damageItem(yoyo, yoyoEntity.hand, 1, player)
                    yoyoEntity.decrementRemainingTime(10)

                    for (stack in stacks) {
                        yoyoEntity.createItemDropOrCollect(stack, pos)
                    }

                    world.playSound(null, pos, block.getSoundType(state, world, pos, yoyoEntity).breakSound, SoundCategory.BLOCKS, 1f, 1f)
                    world.playEvent(2001, pos.toImmutable(), Block.getStateId(state))
                    return true
                }
            }
        }

        if (block is BushBlock || block is SugarCaneBlock || block is KelpBlock || block is KelpTopBlock) {
            doBlockBreaking(yoyo, player, world, pos, state, block, yoyoEntity)
            return true
        }

        return false
    }

    fun farm(yoyo: ItemStack, player: PlayerEntity, pos: BlockPos, state: BlockState, block: Block, yoyoEntity: YoyoEntity): Boolean {
        if (block is CropsBlock) {
            if (block.isMaxAge(state)) {
                val drops = doHarvesting(yoyo, player, yoyoEntity.world, pos, state, block, yoyoEntity)
                        ?: return true

                if (!yoyoEntity.world.gameRules.getBoolean(GameRules.DO_TILE_DROPS)) drops.clear()

                yoyoEntity.yoyo!!.damageItem(yoyo, yoyoEntity.hand, 1, player)

                var foundSeed = false

                for (stack in drops) {
                    if (stack.isEmpty) continue

                    if (ModConfig.replant && !foundSeed && stack.item is IPlantable) {
                        stack.shrink(1)
                        foundSeed = true
                    }

                    yoyoEntity.createItemDropOrCollect(stack, pos)
                }

                if (ModConfig.replant) {
                    if (!foundSeed && !yoyoEntity.collectedDrops.isEmpty()) {
                        for (stack in yoyoEntity.collectedDrops) {
                            if (stack.isEmpty) continue

                            if (stack.item is IPlantable) {
                                stack.shrink(1)
                                yoyoEntity.needCollectedSync = true // refill/reshuffle the stacks
                                foundSeed = true
                                break
                            }
                        }
                    }

                    if (foundSeed) yoyoEntity.world.setBlockState(pos, block.withAge(0))
                }

                return true
            }
        }

        return false
    }

    fun doBlockBreaking(yoyo: ItemStack, player: PlayerEntity, world: World, pos: BlockPos, state: BlockState, block: Block, yoyoEntity: YoyoEntity) {
        val itemStacks = doHarvesting(yoyo, player, world, pos, state, block, yoyoEntity) ?: return

        yoyoEntity.yoyo!!.damageItem(yoyo, yoyoEntity.hand, 1, player)

        if (!yoyoEntity.world.gameRules.getBoolean(GameRules.DO_TILE_DROPS)) return

        for (stack in itemStacks) {
            yoyoEntity.createItemDropOrCollect(stack, pos)
        }
    }

    /**
     * NOT responsible for damaging the tool
     * @return null iff the block could not be broken
     */
    private fun doHarvesting(yoyo: ItemStack, player: PlayerEntity, world: World, pos: BlockPos, state: BlockState, block: Block, yoyoEntity: YoyoEntity): NonNullList<ItemStack>? {
        if (!yoyoEntity.world.removeBlock(pos, false)) return null

        block.onPlayerDestroy(world, pos, state)

        world.playSound(null, pos, block.getSoundType(state, world, pos, yoyoEntity).breakSound, SoundCategory.BLOCKS, 1f, 1f)
        world.playEvent(2001, pos.toImmutable(), Block.getStateId(state))

        val drops: NonNullList<ItemStack> = with(LootContext.Builder(world as ServerWorld)) {
            withParameter(LootParameters.THIS_ENTITY, player)
            withParameter(LootParameters.TOOL, yoyo)
            withParameter(LootParameters.POSITION, pos)
            withLuck(player.luck)
            NonNullList.from(ItemStack.EMPTY, *state.getDrops(this).toTypedArray())
        }
        ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, yoyo), 1.0f, false, player)

        yoyoEntity.decrementRemainingTime(10)

        return drops
    }

    fun attackEntity(yoyo: ItemStack, player: PlayerEntity, hand: Hand, yoyoEntity: YoyoEntity, targetEntity: Entity): Boolean {
        if (!yoyoEntity.canAttack() || !targetEntity.isAlive) return false
        if (!ForgeHooks.onPlayerAttackTarget(player, targetEntity)) return false

        val uuid = targetEntity.uniqueID
        if (uuid == player.leftShoulderEntity.getUniqueId("UUID") || uuid == player.rightShoulderEntity.getUniqueId("UUID"))
            return false

        if (targetEntity.canBeAttackedWithItem()) {
            if (!targetEntity.hitByEntity(player)) {
                yoyoEntity.resetAttackCooldown()
                yoyoEntity.decrementRemainingTime(10)
                yoyoEntity.yoyo!!.damageItem(yoyo, hand, 1, player)

                var damage = player.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).value.toFloat()

                val attackModifier: Float = if (targetEntity is LivingEntity)
                    EnchantmentHelper.getModifierForCreature(yoyo, targetEntity.creatureAttribute)
                else
                    EnchantmentHelper.getModifierForCreature(yoyo, CreatureAttribute.UNDEFINED)

                if (damage > 0.0f || attackModifier > 0.0f) {
                    var knockbackModifier = 0
                    knockbackModifier += EnchantmentHelper.getKnockbackModifier(player)

                    val critical = (player.fallDistance > 0.0f && !player.onGround && !player.isOnLadder && !player.isInWater && !player.isPotionActive(Effects.BLINDNESS) && player.ridingEntity !== null && targetEntity is LivingEntity) && !player.isSprinting

                    if (critical)
                        damage *= 1.5f

                    damage += attackModifier

                    var targetHealth = 0.0f
                    var setEntityOnFire = false
                    val fireAspect = EnchantmentHelper.getFireAspectModifier(player)

                    if (targetEntity is LivingEntity) {
                        targetHealth = targetEntity.health

                        if (fireAspect > 0 && !targetEntity.isBurning()) {
                            setEntityOnFire = true
                            targetEntity.setFire(1)
                        }
                    }

                    val motion = targetEntity.motion
                    val didDamage = targetEntity.attackEntityFrom(IndirectEntityDamageSource("yoyo", yoyoEntity, player), damage)

                    if (didDamage) {
                        if (knockbackModifier > 0) {
                            if (targetEntity is LivingEntity)
                                targetEntity.knockBack(player, knockbackModifier.toFloat() * 0.5f, MathHelper.sin(player.rotationYaw * 0.017453292f).toDouble(), (-MathHelper.cos(player.rotationYaw * 0.017453292f)).toDouble())
                            else
                                targetEntity.addVelocity((-MathHelper.sin(player.rotationYaw * 0.017453292f) * knockbackModifier.toFloat() * 0.5f).toDouble(), 0.1, (MathHelper.cos(player.rotationYaw * 0.017453292f) * knockbackModifier.toFloat() * 0.5f).toDouble())
                        }

                        if (targetEntity is ServerPlayerEntity && targetEntity.velocityChanged) {
                            targetEntity.connection.sendPacket(SEntityVelocityPacket(targetEntity))
                            targetEntity.velocityChanged = false
                            targetEntity.motion = motion
                        }

                        if (critical) {
                            player.world.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.soundCategory, 1.0f, 1.0f)
                            player.onCriticalHit(targetEntity)
                        }

                        if (!critical) {
                            player.world.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.soundCategory, 1.0f, 1.0f)
                        }

                        if (attackModifier > 0.0f)
                            player.onEnchantmentCritical(targetEntity)

                        player.setLastAttackedEntity(targetEntity)

                        if (targetEntity is LivingEntity)
                            EnchantmentHelper.applyThornEnchantments(targetEntity, player)

                        EnchantmentHelper.applyArthropodEnchantments(player, targetEntity)

                        if (yoyo != ItemStack.EMPTY && targetEntity is LivingEntity) {
                            yoyo.hitEntity(targetEntity, player)

                            if (yoyo.count <= 0) {
                                player.setHeldItem(hand, ItemStack.EMPTY)
                                ForgeEventFactory.onPlayerDestroyItem(player, yoyo, hand)
                            }
                        }

                        if (targetEntity is LivingEntity) {
                            val f5 = targetHealth - targetEntity.health
                            player.addStat(Stats.DAMAGE_DEALT, (f5 * 10.0f).roundToInt())

                            if (fireAspect > 0)
                                targetEntity.setFire(fireAspect * 4)

                            if (player.world is ServerWorld && f5 > 2.0f) {
                                val k = (f5.toDouble() * 0.5).toInt()
                                (player.world as ServerWorld).spawnParticle(ParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + (targetEntity.height * 0.5f).toDouble(), targetEntity.posZ, k, 0.1, 0.0, 0.1, 0.2)
                            }
                        }

                        player.addExhaustion(0.3f)
                    } else {
                        player.world.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.soundCategory, 1.0f, 1.0f)

                        if (setEntityOnFire)
                            targetEntity.extinguish()
                    }
                }
            }
        }

        return false
    }
}