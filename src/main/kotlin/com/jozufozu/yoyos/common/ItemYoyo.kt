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
import com.jozufozu.yoyos.common.api.*
import com.jozufozu.yoyos.common.init.ModEnchantments
import com.jozufozu.yoyos.common.init.ModItems
import com.jozufozu.yoyos.common.init.ModSounds
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.EnchantmentType
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.attributes.Attribute
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.ai.attributes.Attributes
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
import java.util.function.Function

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
        return if (enchantment === Enchantments.SWEEPING) false
        else if (enchantment === Enchantments.FORTUNE && interactsWithBlocks(stack)) true
        else enchantment === ModEnchantments.COLLECTING || enchantment.type === EnchantmentType.BREAKABLE || enchantment.type === EnchantmentType.WEAPON
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
                val yoyoEntity = YoyoEntity.CASTERS[playerIn.uniqueID]

                if (yoyoEntity == null) {
                    yoyoFactory(worldIn, playerIn, hand).let {
                        worldIn.addEntity(it)
                        worldIn.playSound(null, it.posX, it.posY, it.posZ, ModSounds.yoyoThrow, SoundCategory.NEUTRAL, 0.5f, 0.4f / (Item.random.nextFloat() * 0.4f + 0.8f))
                    }

                    playerIn.addExhaustion(0.05f)
                } else {
                    yoyoEntity.isRetracting = !yoyoEntity.isRetracting
                }
            }
        }

        return ActionResult(ActionResultType.SUCCESS, itemStack)
    }

    override fun hitEntity(stack: ItemStack, target: LivingEntity, attacker: LivingEntity): Boolean {
        stack.damageItem(1, attacker) { entity -> entity.sendBreakAnimation(EquipmentSlotType.MAINHAND) }
        return true
    }

    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<ITextComponent>, flagIn: ITooltipFlag) {
        super.addInformation(stack, worldIn, tooltip, flagIn)
        tooltip.add(TranslationTextComponent("tooltip.yoyos.weight", getWeight(stack)))
        tooltip.add(TranslationTextComponent("tooltip.yoyos.length", getLength(stack)))

        val duration = getDuration(stack)
        if (duration < 0) tooltip.add(TranslationTextComponent("tooltip.yoyos.duration.infinite"))
        else tooltip.add(TranslationTextComponent("tooltip.yoyos.duration", duration.toFloat() / 20f))

        if (stack.isEnchanted)
            tooltip.add(StringTextComponent(""))
    }

    override fun getAttributeModifiers(equipmentSlot: EquipmentSlotType, stack: ItemStack): Multimap<Attribute, AttributeModifier> {
        val multimap = HashMultimap.create<Attribute, AttributeModifier>()

        if (equipmentSlot === EquipmentSlotType.MAINHAND || equipmentSlot === EquipmentSlotType.OFFHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, AttributeModifier(Item.ATTACK_DAMAGE_MODIFIER, { "Weapon modifier" }, getAttackDamage(stack), AttributeModifier.Operation.ADDITION))
            multimap.put(Attributes.ATTACK_SPEED, AttributeModifier(Item.ATTACK_SPEED_MODIFIER, { "Weapon modifier" }, -2.4000000953674316, AttributeModifier.Operation.ADDITION))
        }

        return multimap
    }

    open fun getAttackDamage(yoyo: ItemStack): Double = getConfiguredOrDefault(yoyo, attackDamage.toDouble()) { it.damage.get() }

    override fun getWeight(yoyo: ItemStack): Double = getConfiguredOrDefault(yoyo, 1.0) { it.weight.get() }

    override fun getLength(yoyo: ItemStack): Double = getConfiguredOrDefault(yoyo, 1.0) { it.length.get() }

    override fun getDuration(yoyo: ItemStack): Int = getConfiguredOrDefault(yoyo, 10) { it.duration.get() }

    private fun <V> getConfiguredOrDefault(yoyo: ItemStack, default: V, getter: Function<YoyosConfig.YoyoSettings, V>): V {
        return when {
            this === ModItems.SHEAR_YOYO -> getter.apply(YoyosConfig.vanillaYoyos.shearYoyo)
            this === ModItems.STICKY_YOYO -> getter.apply(YoyosConfig.vanillaYoyos.stickyYoyo)
            this === ModItems.HOE_YOYO -> getter.apply(YoyosConfig.vanillaYoyos.hoeYoyo)
            this === ModItems.DIAMOND_YOYO -> getter.apply(YoyosConfig.vanillaYoyos.diamondYoyo)
            this === ModItems.GOLD_YOYO -> getter.apply(YoyosConfig.vanillaYoyos.goldYoyo)
            this === ModItems.IRON_YOYO -> getter.apply(YoyosConfig.vanillaYoyos.ironYoyo)
            this === ModItems.STONE_YOYO -> getter.apply(YoyosConfig.vanillaYoyos.stoneYoyo)
            this === ModItems.WOODEN_YOYO -> getter.apply(YoyosConfig.vanillaYoyos.woodenYoyo)
            this === ModItems.CREATIVE_YOYO -> getter.apply(YoyosConfig.vanillaYoyos.creativeYoyo)
            else -> default
        }
    }

    override fun getAttackInterval(yoyo: ItemStack): Int {
        return if (this === ModItems.CREATIVE_YOYO) 0 else 10
    }

    override fun getMaxCollectedDrops(yoyo: ItemStack): Int {
        return if (this === ModItems.CREATIVE_YOYO) Integer.MAX_VALUE else calculateMaxCollectedDrops(EnchantmentHelper.getEnchantmentLevel(ModEnchantments.COLLECTING, yoyo))
    }

    override fun <T : LivingEntity> damageItem(yoyo: ItemStack, hand: Hand, amount: Int, entity: T) {
        yoyo.damageItem(amount, entity, { item -> item.sendBreakAnimation(hand) })
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

            return YoyosConfig.general.collectingBase.get() * mult
        }
    }
}
