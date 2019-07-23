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

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.jozufozu.yoyos.Yoyos
import com.jozufozu.yoyos.common.api.BlockInteraction
import com.jozufozu.yoyos.common.api.EntityInteraction
import com.jozufozu.yoyos.common.api.IYoyo
import com.jozufozu.yoyos.common.api.YoyoFactory
import com.jozufozu.yoyos.common.init.ModEnchantments
import com.jozufozu.yoyos.common.init.ModItems
import com.jozufozu.yoyos.common.init.ModSounds
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.EnchantmentType
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.item.IItemTier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.TieredItem
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.util.*

open class ItemYoyo(name: String, material: IItemTier, properties: Properties, protected var yoyoFactory: YoyoFactory) : TieredItem(material, properties.maxStackSize(1).group(ModItems.YOYOS_TAB)), IYoyo {
    constructor(name: String, material: IItemTier, properties: Properties): this(name, material, properties, ::YoyoEntity)
    constructor(name: String, material: IItemTier): this(name, material, Properties(), ::YoyoEntity)

    private val attackDamage: Float = 3.0f + material.attackDamage
    protected var renderOrientation = RenderOrientation.Vertical
    protected val entityInteractions = ArrayList<EntityInteraction>()
    protected val blockInteractions = ArrayList<BlockInteraction>()

    init {
        this.registryName = ResourceLocation(Yoyos.MODID, name)
    }

    fun addBlockInteraction(vararg blockInteraction: BlockInteraction): ItemYoyo {
        Collections.addAll(blockInteractions, *blockInteraction)
        return this
    }

    fun addEntityInteraction(vararg entityInteraction: EntityInteraction): ItemYoyo {
        Collections.addAll(entityInteractions, *entityInteraction)
        return this
    }

    fun setRenderOrientation(renderOrientation: RenderOrientation): ItemYoyo {
        this.renderOrientation = renderOrientation
        return this
    }

    override fun canApplyAtEnchantingTable(stack: ItemStack, enchantment: Enchantment): Boolean {
        if (enchantment === Enchantments.SWEEPING) return false
        return if (enchantment === Enchantments.FORTUNE && interactsWithBlocks(stack)) true else enchantment === ModEnchantments.COLLECTING || enchantment.type === EnchantmentType.ALL || enchantment.type === EnchantmentType.WEAPON

    }

    override fun hasEffect(stack: ItemStack): Boolean {
        return this === ModItems.CREATIVE_YOYO || super.hasEffect(stack)
    }

    override fun onBlockDestroyed(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, entity: LivingEntity): Boolean {
        if (!world.isRemote && state.getBlockHardness(world, pos) != 0.0f) {
            stack.damageItem(1, entity, { livingEntity -> livingEntity.sendBreakAnimation(EquipmentSlotType.MAINHAND) })
        }

        return true
    }

    override fun onItemRightClick(worldIn: World, playerIn: PlayerEntity, hand: Hand): ActionResult<ItemStack> {
        val itemStack = playerIn.getHeldItem(hand)
        if (!worldIn.isRemote) {
            if (itemStack.damage <= itemStack.maxDamage || this === ModItems.CREATIVE_YOYO) {
                if (playerIn.uniqueID !in YoyoEntity.CASTERS) {
                    val yoyo = yoyoFactory(worldIn, playerIn, hand)

                    worldIn.addEntity(yoyo)
                    worldIn.playSound(null, yoyo.posX, yoyo.posY, yoyo.posZ, ModSounds.YOYO_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (Item.random.nextFloat() * 0.4f + 0.8f))

                    playerIn.addExhaustion(0.05f)
                }
            }
        }

        return ActionResult(ActionResultType.SUCCESS, itemStack)
    }

    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<ITextComponent>, flagIn: ITooltipFlag) {
        super.addInformation(stack, worldIn, tooltip, flagIn)
        tooltip.add(TranslationTextComponent("yoyos.info.weight.name", getWeight(stack)))
        tooltip.add(TranslationTextComponent("yoyos.info.length.name", getLength(stack)))

        val duration = getDuration(stack)
        val arg = if (duration < 0) I18n.format("stat.yoyo.infinite.name") else duration.toFloat() / 20f
        tooltip.add(TranslationTextComponent("yoyos.info.duration.name", arg))

        if (stack.isEnchanted)
            tooltip.add(StringTextComponent(""))
    }

    override fun getAttributeModifiers(equipmentSlot: EquipmentSlotType, stack: ItemStack): Multimap<String, AttributeModifier> {
        val multimap = HashMultimap.create<String, AttributeModifier>()

        if (equipmentSlot === EquipmentSlotType.MAINHAND || equipmentSlot === EquipmentSlotType.OFFHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.name, AttributeModifier(Item.ATTACK_DAMAGE_MODIFIER, { "Weapon modifier" }, getAttackDamage(stack), AttributeModifier.Operation.ADDITION))
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.name, AttributeModifier(Item.ATTACK_SPEED_MODIFIER, { "Weapon modifier" }, -2.4000000953674316, AttributeModifier.Operation.ADDITION))
        }

        return multimap
    }

    fun getAttackDamage(yoyo: ItemStack): Double {
        if (this === ModItems.SHEAR_YOYO) return ModConfig.vanillaYoyos.shearYoyo.damage
        if (this === ModItems.STICKY_YOYO) return ModConfig.vanillaYoyos.stickyYoyo.damage
        if (this === ModItems.HOE_YOYO) return ModConfig.vanillaYoyos.hoeYoyo.damage
        if (this === ModItems.DIAMOND_YOYO) return ModConfig.vanillaYoyos.diamondYoyo.damage
        if (this === ModItems.GOLD_YOYO) return ModConfig.vanillaYoyos.goldYoyo.damage
        if (this === ModItems.IRON_YOYO) return ModConfig.vanillaYoyos.ironYoyo.damage
        if (this === ModItems.STONE_YOYO) return ModConfig.vanillaYoyos.stoneYoyo.damage
        if (this === ModItems.WOODEN_YOYO) return ModConfig.vanillaYoyos.woodenYoyo.damage
        return if (this === ModItems.CREATIVE_YOYO) ModConfig.vanillaYoyos.creativeYoyo.damage else attackDamage.toDouble()

    }

    override fun getWeight(yoyo: ItemStack): Float {
        if (this === ModItems.SHEAR_YOYO) return ModConfig.vanillaYoyos.shearYoyo.weight
        if (this === ModItems.STICKY_YOYO) return ModConfig.vanillaYoyos.stickyYoyo.weight
        if (this === ModItems.HOE_YOYO) return ModConfig.vanillaYoyos.hoeYoyo.weight
        if (this === ModItems.DIAMOND_YOYO) return ModConfig.vanillaYoyos.diamondYoyo.weight
        if (this === ModItems.GOLD_YOYO) return ModConfig.vanillaYoyos.goldYoyo.weight
        if (this === ModItems.IRON_YOYO) return ModConfig.vanillaYoyos.ironYoyo.weight
        if (this === ModItems.STONE_YOYO) return ModConfig.vanillaYoyos.stoneYoyo.weight
        if (this === ModItems.WOODEN_YOYO) return ModConfig.vanillaYoyos.woodenYoyo.weight
        return if (this === ModItems.CREATIVE_YOYO) ModConfig.vanillaYoyos.creativeYoyo.weight else 1.0f

    }

    override fun getLength(yoyo: ItemStack): Float {
        if (this === ModItems.SHEAR_YOYO) return ModConfig.vanillaYoyos.shearYoyo.length
        if (this === ModItems.STICKY_YOYO) return ModConfig.vanillaYoyos.stickyYoyo.length
        if (this === ModItems.HOE_YOYO) return ModConfig.vanillaYoyos.hoeYoyo.length
        if (this === ModItems.DIAMOND_YOYO) return ModConfig.vanillaYoyos.diamondYoyo.length
        if (this === ModItems.GOLD_YOYO) return ModConfig.vanillaYoyos.goldYoyo.length
        if (this === ModItems.IRON_YOYO) return ModConfig.vanillaYoyos.ironYoyo.length
        if (this === ModItems.STONE_YOYO) return ModConfig.vanillaYoyos.stoneYoyo.length
        if (this === ModItems.WOODEN_YOYO) return ModConfig.vanillaYoyos.woodenYoyo.length
        return if (this === ModItems.CREATIVE_YOYO) ModConfig.vanillaYoyos.creativeYoyo.length else 1.0f

    }

    override fun getDuration(yoyo: ItemStack): Int {
        if (this === ModItems.SHEAR_YOYO) return ModConfig.vanillaYoyos.shearYoyo.duration
        if (this === ModItems.STICKY_YOYO) return ModConfig.vanillaYoyos.stickyYoyo.duration
        if (this === ModItems.HOE_YOYO) return ModConfig.vanillaYoyos.hoeYoyo.duration
        if (this === ModItems.DIAMOND_YOYO) return ModConfig.vanillaYoyos.diamondYoyo.duration
        if (this === ModItems.GOLD_YOYO) return ModConfig.vanillaYoyos.goldYoyo.duration
        if (this === ModItems.IRON_YOYO) return ModConfig.vanillaYoyos.ironYoyo.duration
        if (this === ModItems.STONE_YOYO) return ModConfig.vanillaYoyos.stoneYoyo.duration
        if (this === ModItems.WOODEN_YOYO) return ModConfig.vanillaYoyos.woodenYoyo.duration
        return if (this === ModItems.CREATIVE_YOYO) ModConfig.vanillaYoyos.creativeYoyo.duration else 10

    }

    override fun getAttackInterval(yoyo: ItemStack): Int {
        return if (this === ModItems.CREATIVE_YOYO) 0 else 10
    }

    override fun getMaxCollectedDrops(yoyo: ItemStack): Int {
        return if (this === ModItems.CREATIVE_YOYO) Integer.MAX_VALUE else calculateMaxCollectedDrops(EnchantmentHelper.getEnchantmentLevel(ModEnchantments.COLLECTING, yoyo))
    }

    override fun <T : LivingEntity> damageItem(stack: ItemStack, hand: Hand, amount: Int, entity: T) {
        stack.damageItem(amount, entity, { item -> item.sendBreakAnimation(hand) })
    }

    override fun entityInteraction(yoyo: ItemStack, player: PlayerEntity, hand: Hand, yoyoEntity: YoyoEntity, targetEntity: Entity) {
        if (targetEntity.world.isRemote) return
        for (entityInteraction in entityInteractions) {
            if (entityInteraction(yoyo, player, hand, yoyoEntity, targetEntity)) return
        }
    }

    override fun interactsWithBlocks(yoyo: ItemStack) = blockInteractions.isNotEmpty()

    override fun blockInteraction(yoyo: ItemStack, player: PlayerEntity, world: World, pos: BlockPos, state: BlockState, block: Block, yoyoEntity: YoyoEntity) {
        if (world.isRemote) return
        for (blockInteraction in blockInteractions) {
            if (blockInteraction(yoyo, player, pos, state, block, yoyoEntity)) return
        }
    }

    @OnlyIn(Dist.CLIENT)
    override fun getRenderOrientation(yoyo: ItemStack): RenderOrientation {
        return renderOrientation
    }

    companion object {
        fun calculateMaxCollectedDrops(level: Int): Int {
            if (level == 0) return 0

            var mult = 1

            for (i in 0 until level - 1) mult *= 2

            return ModConfig.collectingBase * mult
        }
    }
}
