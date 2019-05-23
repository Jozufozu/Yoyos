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

package com.jozufozu.yoyos.common;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.jozufozu.yoyos.Yoyos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ItemYoyo extends Item implements IYoyo
{
    //TODO: Remove duration when a block is broken

    private final float attackDamage;
    protected final ToolMaterial material;
    private RenderOrientation renderOrientation = RenderOrientation.Vertical;
    private ArrayList<IEntityInteraction> entityInteractions;
    private ArrayList<IBlockInteraction> blockInteractions;

    public ItemYoyo(String name, ToolMaterial material)
    {
        this(name, material, Lists.newArrayList(ItemYoyo::collectItem, ItemYoyo::attackEntity), new ArrayList<>());
    }
    
    public ItemYoyo(String name, ToolMaterial material, @Nonnull ArrayList<IEntityInteraction> entityInteractions, @Nonnull ArrayList<IBlockInteraction> blockInteractions)
    {
        this.entityInteractions = entityInteractions;
        this.blockInteractions = blockInteractions;

        this.material = material;
        this.maxStackSize = 1;
        this.setMaxDamage(material.getMaxUses());
        this.setCreativeTab(CreativeTabs.COMBAT);
        this.attackDamage = 3.0F + material.getAttackDamage();

        this.setUnlocalizedName(String.format("%s.%s", Yoyos.MODID, name));
        this.setRegistryName(Yoyos.MODID, name);

        this.addPropertyOverride(new ResourceLocation(Yoyos.MODID, "thrown"), (stack, worldIn, entityIn) -> entityIn instanceof EntityBat ? 1 : 0);

        this.setCreativeTab(CreativeTabs.COMBAT);
    }

    public ItemYoyo addBlockInteraction(IBlockInteraction... blockInteraction)
    {
        Collections.addAll(blockInteractions, blockInteraction);
        return this;
    }

    public ItemYoyo addEntityInteraction(IEntityInteraction... entityInteraction)
    {
        Collections.addAll(entityInteractions, entityInteraction);
        return this;
    }

    public RenderOrientation getRenderOrientation()
    {
        return renderOrientation;
    }

    public ItemYoyo setRenderOrientation(RenderOrientation renderOrientation)
    {
        this.renderOrientation = renderOrientation;
        return this;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack)
    {
        return ImmutableSet.of("yoyo");
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
    {
        return enchantment == Yoyos.COLLECTING || enchantment.type == EnumEnchantmentType.ALL || enchantment.type == EnumEnchantmentType.WEAPON;
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return super.hasEffect(stack) || this == Yoyos.CREATIVE_YOYO;
    }
    
    public int getItemEnchantability()
    {
        return this.material.getEnchantability();
    }
    
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair)
    {
        ItemStack mat = this.material.getRepairItemStack();
        return OreDictionary.itemMatches(mat, repair, false) || super.getIsRepairable(toRepair, repair);
    }

    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving)
    {
        if ((double)state.getBlockHardness(worldIn, pos) != 0.0D)
        {
            this.damageItem(stack, 2, entityLiving);
        }

        return true;
    }

    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
    {
        this.damageItem(stack, 1, attacker);
        return true;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack itemStack = playerIn.getHeldItem(hand);
        if (!worldIn.isRemote && !EntityYoyo.CASTERS.containsKey(playerIn))
        {
            if (itemStack.getItemDamage() <= itemStack.getMaxDamage() || this == Yoyos.CREATIVE_YOYO)
            {
                EntityYoyo yoyo = new EntityYoyo(worldIn, playerIn);
                worldIn.spawnEntity(yoyo);

                worldIn.playSound(null, yoyo.posX, yoyo.posY, yoyo.posZ, Yoyos.YOYO_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

                playerIn.addExhaustion(0.05F);
            }
        }
        
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
    }
    
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(I18n.format("yoyos.info.weight.name", getWeight(stack)));
        tooltip.add(I18n.format("yoyos.info.length.name", getLength(stack)));
    
        int duration = getDuration(stack);
        Object arg = (duration < 0 ? I18n.format("stat.yoyo.infinite.name") : ((float) duration) / 20F);
        tooltip.add(I18n.format("yoyos.info.duration.name", arg));
        
        if (stack.isItemEnchanted())
            tooltip.add("");
    }
    
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack)
    {
        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
    
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND || equipmentSlot == EntityEquipmentSlot.OFFHAND)
        {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getAttackDamage(stack), 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
        }
    
        return multimap;
    }

    @Override
    public float getAttackDamage(ItemStack yoyo)
    {
        if (this == Yoyos.SHEAR_YOYO) return ModConfig.vanillaYoyos.shearYoyo.damage;
        if (this == Yoyos.STICKY_YOYO) return ModConfig.vanillaYoyos.stickyYoyo.damage;
        if (this == Yoyos.HOE_YOYO) return ModConfig.vanillaYoyos.hoeYoyo.damage;
        if (this == Yoyos.DIAMOND_YOYO) return ModConfig.vanillaYoyos.diamondYoyo.damage;
        if (this == Yoyos.GOLD_YOYO) return ModConfig.vanillaYoyos.goldYoyo.damage;
        if (this == Yoyos.IRON_YOYO) return ModConfig.vanillaYoyos.ironYoyo.damage;
        if (this == Yoyos.STONE_YOYO) return ModConfig.vanillaYoyos.stoneYoyo.damage;
        if (this == Yoyos.WOODEN_YOYO) return ModConfig.vanillaYoyos.woodenYoyo.damage;
        if (this == Yoyos.CREATIVE_YOYO) return ModConfig.vanillaYoyos.creativeYoyo.damage;

        return attackDamage;
    }

    @Override
    public float getWeight(ItemStack yoyo)
    {
        if (this == Yoyos.SHEAR_YOYO) return ModConfig.vanillaYoyos.shearYoyo.weight;
        if (this == Yoyos.STICKY_YOYO) return ModConfig.vanillaYoyos.stickyYoyo.weight;
        if (this == Yoyos.HOE_YOYO) return ModConfig.vanillaYoyos.hoeYoyo.weight;
        if (this == Yoyos.DIAMOND_YOYO) return ModConfig.vanillaYoyos.diamondYoyo.weight;
        if (this == Yoyos.GOLD_YOYO) return ModConfig.vanillaYoyos.goldYoyo.weight;
        if (this == Yoyos.IRON_YOYO) return ModConfig.vanillaYoyos.ironYoyo.weight;
        if (this == Yoyos.STONE_YOYO) return ModConfig.vanillaYoyos.stoneYoyo.weight;
        if (this == Yoyos.WOODEN_YOYO) return ModConfig.vanillaYoyos.woodenYoyo.weight;
        if (this == Yoyos.CREATIVE_YOYO) return ModConfig.vanillaYoyos.creativeYoyo.weight;
        
        return 1.0f;
    }
    
    @Override
    public float getLength(ItemStack yoyo)
    {
        if (this == Yoyos.SHEAR_YOYO) return ModConfig.vanillaYoyos.shearYoyo.length;
        if (this == Yoyos.STICKY_YOYO) return ModConfig.vanillaYoyos.stickyYoyo.length;
        if (this == Yoyos.HOE_YOYO) return ModConfig.vanillaYoyos.hoeYoyo.length;
        if (this == Yoyos.DIAMOND_YOYO) return ModConfig.vanillaYoyos.diamondYoyo.length;
        if (this == Yoyos.GOLD_YOYO) return ModConfig.vanillaYoyos.goldYoyo.length;
        if (this == Yoyos.IRON_YOYO) return ModConfig.vanillaYoyos.ironYoyo.length;
        if (this == Yoyos.STONE_YOYO) return ModConfig.vanillaYoyos.stoneYoyo.length;
        if (this == Yoyos.WOODEN_YOYO) return ModConfig.vanillaYoyos.woodenYoyo.length;
        if (this == Yoyos.CREATIVE_YOYO) return ModConfig.vanillaYoyos.creativeYoyo.length;
    
        return 1.0f;
    }
    
    @Override
    public int getDuration(ItemStack yoyo)
    {
        if (this == Yoyos.SHEAR_YOYO) return ModConfig.vanillaYoyos.shearYoyo.duration;
        if (this == Yoyos.STICKY_YOYO) return ModConfig.vanillaYoyos.stickyYoyo.duration;
        if (this == Yoyos.HOE_YOYO) return ModConfig.vanillaYoyos.hoeYoyo.duration;
        if (this == Yoyos.DIAMOND_YOYO) return ModConfig.vanillaYoyos.diamondYoyo.duration;
        if (this == Yoyos.GOLD_YOYO) return ModConfig.vanillaYoyos.goldYoyo.duration;
        if (this == Yoyos.IRON_YOYO) return ModConfig.vanillaYoyos.ironYoyo.duration;
        if (this == Yoyos.STONE_YOYO) return ModConfig.vanillaYoyos.stoneYoyo.duration;
        if (this == Yoyos.WOODEN_YOYO) return ModConfig.vanillaYoyos.woodenYoyo.duration;
        if (this == Yoyos.CREATIVE_YOYO) return ModConfig.vanillaYoyos.creativeYoyo.duration;
    
        return 10;
    }
    
    @Override
    public int getAttackSpeed(ItemStack yoyo)
    {
        return 10;
    }
    
    @Override
    public int collecting(ItemStack yoyo)
    {
        if (this == Yoyos.CREATIVE_YOYO) return Integer.MAX_VALUE / 2;
        return EnchantmentHelper.getEnchantmentLevel(Yoyos.COLLECTING, yoyo);
    }
    
    @Override
    public void damageItem(ItemStack yoyo, int amount, EntityLivingBase player)
    {
        yoyo.damageItem(amount, player);
    }

    @Override
    public void entityInteraction(ItemStack yoyo, EntityPlayer player, EnumHand hand, EntityYoyo yoyoEntity, Entity targetEntity)
    {
        if (targetEntity.world.isRemote) return;
        for (IEntityInteraction entityInteraction : entityInteractions)
        {
            if (entityInteraction.entityInteraction(yoyo, player, hand, yoyoEntity, targetEntity)) return;
        }
    }

    @Override
    public boolean interactsWithBlocks(ItemStack yoyo)
    {
        return blockInteractions != null && !blockInteractions.isEmpty();
    }

    @Override
    public void blockInteraction(ItemStack yoyo, EntityPlayer player, World world, BlockPos pos, IBlockState state, Block block, EntityYoyo yoyoEntity)
    {
        if (world.isRemote) return;
        for (IBlockInteraction blockInteraction : blockInteractions)
        {
            if (blockInteraction.blockInteraction(yoyo, player, pos, state, block, yoyoEntity)) return;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public RenderOrientation getRenderOrientation(ItemStack yoyo)
    {
        return renderOrientation;
    }

    public static boolean collectItem(ItemStack yoyo, EntityPlayer player, EnumHand hand, EntityYoyo yoyoEntity, Entity targetEntity) {
        if (targetEntity instanceof EntityItem && yoyoEntity.isCollecting()) {
            yoyoEntity.collectDrop(((EntityItem) targetEntity));
            return true;
        }
        return false;
    }

    public static boolean shearEntity(ItemStack yoyo, EntityPlayer player, EnumHand hand, EntityYoyo yoyoEntity, Entity targetEntity) {
        if (targetEntity instanceof IShearable)
        {
            World world = targetEntity.world;
            IShearable shearable = (IShearable) targetEntity;
            BlockPos pos = new BlockPos(targetEntity.posX, targetEntity.posY, targetEntity.posZ);

            if (shearable.isShearable(yoyo, world, pos))
            {
                yoyoEntity.yoyo.damageItem(yoyo, 1, player);
                List<ItemStack> stacks = shearable.onSheared(yoyo, world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, yoyo));

                for (ItemStack stack : stacks)
                {
                    yoyoEntity.createItemDropOrCollect(stack, pos);
                }
            }

            return true;
        }

        return false;
    }

    public static boolean till(ItemStack yoyo, EntityPlayer player, BlockPos pos, IBlockState state, Block block, EntityYoyo yoyoEntity)
    {
        World worldIn = yoyoEntity.world;

        if (pos.getY() + 0.9 < yoyoEntity.posY && worldIn.isAirBlock(pos.up()))
        {
            int hook = ForgeEventFactory.onHoeUse(yoyo, player, worldIn, pos);
            if (hook != 0) return true;

            if (block == Blocks.GRASS || block == Blocks.GRASS_PATH)
            {
                setBlock(yoyoEntity.yoyo, yoyo, player, worldIn, pos, Blocks.FARMLAND.getDefaultState());
                return true;
            }

            if (block == Blocks.DIRT)
            {
                switch (state.getValue(BlockDirt.VARIANT))
                {
                case DIRT:
                    setBlock(yoyoEntity.yoyo, yoyo, player, worldIn, pos, Blocks.FARMLAND.getDefaultState());
                    return true;
                case COARSE_DIRT:
                    setBlock(yoyoEntity.yoyo, yoyo, player, worldIn, pos, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT));
                    return true;
                }
            }
        }

        return false;
    }

    private static void setBlock(IYoyo yoyo, ItemStack yoyoStack, EntityPlayer player, World worldIn, BlockPos pos, IBlockState state)
    {
        worldIn.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);

        if (!worldIn.isRemote)
        {
            worldIn.setBlockState(pos, state, 11);
            yoyo.damageItem(yoyoStack, 1, player);
        }
    }

    public static boolean garden(ItemStack yoyo, EntityPlayer player, BlockPos pos, IBlockState state, Block block, EntityYoyo yoyoEntity)
    {
        World world = yoyoEntity.world;
        if (block instanceof IShearable)
        {
            IShearable shearable = (IShearable) block;
            if (shearable.isShearable(yoyo, world, pos))
            {
                List<ItemStack> stacks = shearable.onSheared(yoyo, world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, yoyo));

                if (yoyoEntity.world.setBlockToAir(pos))
                {
                    block.breakBlock(world, pos, state);

                    yoyoEntity.yoyo.damageItem(yoyo, 1, player);

                    for (ItemStack stack : stacks)
                    {
                        yoyoEntity.createItemDropOrCollect(stack, pos);
                    }

                    world.playSound(null, pos, block.getSoundType(state, world, pos, yoyoEntity).getBreakSound(), SoundCategory.BLOCKS, 1, 1);
                    world.playEvent(2001, pos.toImmutable(), Block.getStateId(state));
                    return true;
                }
            }
        }

        if (block instanceof BlockBush)
        {
            doBlockBreaking(yoyo, player, world, pos, state, block, yoyoEntity);
            return true;
        }

        return false;
    }

    public static boolean farm(ItemStack yoyo, EntityPlayer player, BlockPos pos, IBlockState state, Block block, EntityYoyo yoyoEntity)
    {
        if (block instanceof BlockCrops)
        {
            if (state.getValue(BlockCrops.AGE) == 7)
            {
                NonNullList<ItemStack> drops = doHarvesting(yoyo, player, yoyoEntity.world, pos, state, block, yoyoEntity);

                if (drops == null) return true;

                yoyoEntity.yoyo.damageItem(yoyo, 1, player);

                boolean foundSeed = false;

                for (ItemStack stack : drops)
                {
                    if (stack.isEmpty()) continue;

                    if (!foundSeed && stack.getItem() instanceof IPlantable)
                    {
                        stack.shrink(1);
                        foundSeed = true;
                    }

                    yoyoEntity.createItemDropOrCollect(stack, pos);
                }

                if (foundSeed) yoyoEntity.world.setBlockState(pos, state.withProperty(BlockCrops.AGE, 0));

                return true;
            }
        }

        return false;
    }

    public static void doBlockBreaking(ItemStack yoyo, EntityPlayer player, World world, BlockPos pos, IBlockState state, Block block, EntityYoyo yoyoEntity)
    {
        NonNullList<ItemStack> itemStacks = doHarvesting(yoyo, player, world, pos, state, block, yoyoEntity);

        if (itemStacks == null) return;

        yoyoEntity.yoyo.damageItem(yoyo, 1, player);

        for (ItemStack stack : itemStacks)
        {
            yoyoEntity.createItemDropOrCollect(stack, pos);
        }
    }

    /**
     * NOT responsible for damaging the tool
     * @param yoyo
     * @param player
     * @param world
     * @param pos
     * @param state
     * @param block
     * @param yoyoEntity
     * @return null iff the block could not be broken
     */
    @Nullable
    private static NonNullList<ItemStack> doHarvesting(ItemStack yoyo, EntityPlayer player, World world, BlockPos pos, IBlockState state, Block block, EntityYoyo yoyoEntity)
    {
        if (!yoyoEntity.world.setBlockToAir(pos)) return null;

        block.breakBlock(world, pos, state);

        world.playSound(null, pos, block.getSoundType(state, world, pos, yoyoEntity).getBreakSound(), SoundCategory.BLOCKS, 1, 1);
        world.playEvent(2001, pos.toImmutable(), Block.getStateId(state));

        int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, yoyo);

        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(drops, world, pos, state, fortune);
        ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, fortune, 1.0F, false, player);

        return drops;
    }

    public static boolean attackEntity(ItemStack yoyo, EntityPlayer player, EnumHand hand, EntityYoyo yoyoEntity, Entity targetEntity)
    {
        if (!ForgeHooks.onPlayerAttackTarget(player, targetEntity))
            return false;

        if (targetEntity.canBeAttackedWithItem())
        {
            if (!targetEntity.hitByEntity(player))
            {
                float damage = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float attackModifier;

                if (targetEntity instanceof EntityLivingBase)
                    attackModifier = EnchantmentHelper.getModifierForCreature(yoyo, ((EntityLivingBase) targetEntity).getCreatureAttribute());
                else
                    attackModifier = EnchantmentHelper.getModifierForCreature(yoyo, EnumCreatureAttribute.UNDEFINED);

                if (damage > 0.0F || attackModifier > 0.0F)
                {
                    int knockbackModifier = 0;
                    knockbackModifier = knockbackModifier + EnchantmentHelper.getKnockbackModifier(player);

                    boolean critical = player.fallDistance > 0.0F && !player.onGround && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(MobEffects.BLINDNESS) && !player.isRiding() && targetEntity instanceof EntityLivingBase;
                    critical = critical && !player.isSprinting();

                    if (critical)
                        damage *= 1.5F;

                    damage = damage + attackModifier;

                    float targetHealth = 0.0F;
                    boolean setEntityOnFire = false;
                    int fireAspect = EnchantmentHelper.getFireAspectModifier(player);

                    if (targetEntity instanceof EntityLivingBase)
                    {
                        targetHealth = ((EntityLivingBase) targetEntity).getHealth();

                        if (fireAspect > 0 && !targetEntity.isBurning())
                        {
                            setEntityOnFire = true;
                            targetEntity.setFire(1);
                        }
                    }

                    double motionX = targetEntity.motionX;
                    double motionY = targetEntity.motionY;
                    double motionZ = targetEntity.motionZ;
                    boolean didDamage = targetEntity.attackEntityFrom(new EntityDamageSourceIndirect("yoyo", yoyoEntity, player), damage);

                    if (didDamage)
                    {
                        if (knockbackModifier > 0)
                        {
                            if (targetEntity instanceof EntityLivingBase)
                                ((EntityLivingBase) targetEntity).knockBack(player, (float) knockbackModifier * 0.5F, (double) MathHelper.sin(player.rotationYaw * 0.017453292F), (double) (-MathHelper.cos(player.rotationYaw * 0.017453292F)));
                            else
                                targetEntity.addVelocity((double) (-MathHelper.sin(player.rotationYaw * 0.017453292F) * (float) knockbackModifier * 0.5F), 0.1D, (double) (MathHelper.cos(player.rotationYaw * 0.017453292F) * (float) knockbackModifier * 0.5F));
                        }

                        if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged)
                        {
                            ((EntityPlayerMP) targetEntity).connection.sendPacket(new SPacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = motionX;
                            targetEntity.motionY = motionY;
                            targetEntity.motionZ = motionZ;
                        }

                        if (critical)
                        {
                            player.world.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                            player.onCriticalHit(targetEntity);
                        }

                        if (!critical)
                        {
                            player.world.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                        }

                        if (attackModifier > 0.0F)
                            player.onEnchantmentCritical(targetEntity);

                        player.setLastAttackedEntity(targetEntity);

                        if (targetEntity instanceof EntityLivingBase)
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, player);

                        EnchantmentHelper.applyArthropodEnchantments(player, targetEntity);

                        if (yoyo != ItemStack.EMPTY && targetEntity instanceof EntityLivingBase)
                        {
                            yoyo.hitEntity((EntityLivingBase) targetEntity, player);

                            if (yoyo.getCount() <= 0)
                            {
                                player.setHeldItem(hand, ItemStack.EMPTY);
                                ForgeEventFactory.onPlayerDestroyItem(player, yoyo, hand);
                            }
                        }

                        if (targetEntity instanceof EntityLivingBase)
                        {
                            float f5 = targetHealth - ((EntityLivingBase) targetEntity).getHealth();
                            player.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));

                            if (fireAspect > 0)
                                targetEntity.setFire(fireAspect * 4);

                            if (player.world instanceof WorldServer && f5 > 2.0F)
                            {
                                int k = (int) ((double) f5 * 0.5D);
                                ((WorldServer) player.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + (double) (targetEntity.height * 0.5F), targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        player.addExhaustion(0.3F);
                    }
                    else
                    {
                        player.world.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);

                        if (setEntityOnFire)
                            targetEntity.extinguish();
                    }
                }
            }
        }

        return false;
    }
}
