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
                val yoyoEntity = YoyoEntity.CASTERS[playerIn.uniqueID]

                if (yoyoEntity == null) {
                    yoyoFactory(worldIn, playerIn, hand).let {
                        worldIn.addEntity(it)
                        worldIn.playSound(null, it.posX, it.posY, it.posZ, ModSounds.YOYO_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (Item.random.nextFloat() * 0.4f + 0.8f))
                    }

                    playerIn.addExhaustion(0.05f)
                } else {
                    yoyoEntity.isRetracting = !yoyoEntity.isRetracting
                }
            }
        }

        return ActionResult(ActionResultType.SUCCESS, itemStack)
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

    override fun getAttributeModifiers(equipmentSlot: EquipmentSlotType, stack: ItemStack): Multimap<String, AttributeModifier> {
        val multimap = HashMultimap.create<String, AttributeModifier>()

        if (equipmentSlot === EquipmentSlotType.MAINHAND || equipmentSlot === EquipmentSlotType.OFFHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.name, AttributeModifier(Item.ATTACK_DAMAGE_MODIFIER, { "Weapon modifier" }, getAttackDamage(stack), AttributeModifier.Operation.ADDITION))
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.name, AttributeModifier(Item.ATTACK_SPEED_MODIFIER, { "Weapon modifier" }, -2.4000000953674316, AttributeModifier.Operation.ADDITION))
        }

        return multimap
    }

    fun getAttackDamage(yoyo: ItemStack): Double {
        return when {
            this === ModItems.SHEAR_YOYO -> YoyosConfig.vanillaYoyos.shearYoyo.damage.get()
            this === ModItems.STICKY_YOYO -> YoyosConfig.vanillaYoyos.stickyYoyo.damage.get()
            this === ModItems.HOE_YOYO -> YoyosConfig.vanillaYoyos.hoeYoyo.damage.get()
            this === ModItems.DIAMOND_YOYO -> YoyosConfig.vanillaYoyos.diamondYoyo.damage.get()
            this === ModItems.GOLD_YOYO -> YoyosConfig.vanillaYoyos.goldYoyo.damage.get()
            this === ModItems.IRON_YOYO -> YoyosConfig.vanillaYoyos.ironYoyo.damage.get()
            this === ModItems.STONE_YOYO -> YoyosConfig.vanillaYoyos.stoneYoyo.damage.get()
            this === ModItems.WOODEN_YOYO -> YoyosConfig.vanillaYoyos.woodenYoyo.damage.get()
            this === ModItems.CREATIVE_YOYO -> YoyosConfig.vanillaYoyos.creativeYoyo.damage.get()
            else -> attackDamage.toDouble()
        }
    }

    override fun getWeight(yoyo: ItemStack): Double {
        return when {
            this === ModItems.SHEAR_YOYO -> YoyosConfig.vanillaYoyos.shearYoyo.weight.get()
            this === ModItems.STICKY_YOYO -> YoyosConfig.vanillaYoyos.stickyYoyo.weight.get()
            this === ModItems.HOE_YOYO -> YoyosConfig.vanillaYoyos.hoeYoyo.weight.get()
            this === ModItems.DIAMOND_YOYO -> YoyosConfig.vanillaYoyos.diamondYoyo.weight.get()
            this === ModItems.GOLD_YOYO -> YoyosConfig.vanillaYoyos.goldYoyo.weight.get()
            this === ModItems.IRON_YOYO -> YoyosConfig.vanillaYoyos.ironYoyo.weight.get()
            this === ModItems.STONE_YOYO -> YoyosConfig.vanillaYoyos.stoneYoyo.weight.get()
            this === ModItems.WOODEN_YOYO -> YoyosConfig.vanillaYoyos.woodenYoyo.weight.get()
            this === ModItems.CREATIVE_YOYO -> YoyosConfig.vanillaYoyos.creativeYoyo.weight.get()
            else -> 1.0
        }

    }

    override fun getLength(yoyo: ItemStack): Double {
        return when {
            this === ModItems.SHEAR_YOYO -> YoyosConfig.vanillaYoyos.shearYoyo.length.get()
            this === ModItems.STICKY_YOYO -> YoyosConfig.vanillaYoyos.stickyYoyo.length.get()
            this === ModItems.HOE_YOYO -> YoyosConfig.vanillaYoyos.hoeYoyo.length.get()
            this === ModItems.DIAMOND_YOYO -> YoyosConfig.vanillaYoyos.diamondYoyo.length.get()
            this === ModItems.GOLD_YOYO -> YoyosConfig.vanillaYoyos.goldYoyo.length.get()
            this === ModItems.IRON_YOYO -> YoyosConfig.vanillaYoyos.ironYoyo.length.get()
            this === ModItems.STONE_YOYO -> YoyosConfig.vanillaYoyos.stoneYoyo.length.get()
            this === ModItems.WOODEN_YOYO -> YoyosConfig.vanillaYoyos.woodenYoyo.length.get()
            this === ModItems.CREATIVE_YOYO -> YoyosConfig.vanillaYoyos.creativeYoyo.length.get()
            else -> 1.0
        }

    }

    override fun getDuration(yoyo: ItemStack): Int {
        return when {
            this === ModItems.SHEAR_YOYO -> YoyosConfig.vanillaYoyos.shearYoyo.duration.get()
            this === ModItems.STICKY_YOYO -> YoyosConfig.vanillaYoyos.stickyYoyo.duration.get()
            this === ModItems.HOE_YOYO -> YoyosConfig.vanillaYoyos.hoeYoyo.duration.get()
            this === ModItems.DIAMOND_YOYO -> YoyosConfig.vanillaYoyos.diamondYoyo.duration.get()
            this === ModItems.GOLD_YOYO -> YoyosConfig.vanillaYoyos.goldYoyo.duration.get()
            this === ModItems.IRON_YOYO -> YoyosConfig.vanillaYoyos.ironYoyo.duration.get()
            this === ModItems.STONE_YOYO -> YoyosConfig.vanillaYoyos.stoneYoyo.duration.get()
            this === ModItems.WOODEN_YOYO -> YoyosConfig.vanillaYoyos.woodenYoyo.duration.get()
            this === ModItems.CREATIVE_YOYO -> YoyosConfig.vanillaYoyos.creativeYoyo.duration.get()
            else -> 10
        }

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

            return YoyosConfig.general.collectingBase.get() * mult
        }
    }
}
