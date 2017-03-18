package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.network.MessageRetractYoYo;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

public class ItemYoyo extends Item implements IYoyo {

    private final float attackDamage;
    private final Item.ToolMaterial material;
    private final boolean gardening;

    public ItemYoyo(String name, ToolMaterial material) {
        this(name, material, false);
    }

    public ItemYoyo(String name, ToolMaterial material, boolean gardening) {
        this.gardening = gardening;

        this.setUnlocalizedName(name);
        this.setRegistryName(Yoyos.MODID, name);

        this.material = material;
        this.maxStackSize = 1;
        this.setMaxDamage(material.getMaxUses());
        this.setCreativeTab(CreativeTabs.COMBAT);
        this.attackDamage = 3.0F + material.getDamageVsEntity();
    }

    /**
     * Return the enchantability factor of the item, most of the time is based on material.
     */
    public int getItemEnchantability()
    {
        return this.material.getEnchantability();
    }

    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        ItemStack mat = this.material.getRepairItemStack();
        return OreDictionary.itemMatches(mat, repair, false) || super.getIsRepairable(toRepair, repair);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        if (!worldIn.isRemote) {
            EntityYoyo entityYoyo = EntityYoyo.CASTERS.get(playerIn);

            if (entityYoyo != null && entityYoyo.isEntityAlive()) {
                entityYoyo.setRetracting(!entityYoyo.isRetracting());
                YoyoNetwork.INSTANCE.sendToAll(new MessageRetractYoYo(entityYoyo, false));
                playerIn.swingArm(hand);
            }
            else if (itemStackIn.getItemDamage() < itemStackIn.getMaxDamage()) {
                worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

                worldIn.spawnEntityInWorld(new EntityYoyo(worldIn, playerIn));

                playerIn.swingArm(hand);
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
    }

    @Override
    public float getWeight(ItemStack yoyo) {
        switch (this.material) {
            case WOOD:
                return 2.0F;
            case STONE:
                return 6.0F;
            case IRON:
                return 7.0F;
            case DIAMOND:
                return 1.2F;
            case GOLD:
                return 4.0F;
            default:
                return 1.0F;
        }
    }

    @Override
    public float getLength(ItemStack yoyo) {
        return 8.0F;
    }

    @Override
    public int getDuration(ItemStack yoyo) {
        return 100;
    }

    @Override
    public int getAttackSpeed(ItemStack yoyo) {
        return 15;
    }

    @Override
    public boolean gardening(ItemStack yoyo) {
        return gardening;
    }

    @Override
    public void damageItem(ItemStack yoyo, EntityLivingBase player) {
        yoyo.damageItem(1, player);
    }

    @Override
    public void attack(Entity targetEntity, ItemStack stack, EntityPlayer attacker) {
        if (!net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(attacker, targetEntity)) return;
        if (targetEntity.canBeAttackedWithItem())
        {
            if (!targetEntity.hitByEntity(attacker))
            {
                float damage = (float)attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                damage = this.attackDamage;
                float f1;

                if (targetEntity instanceof EntityLivingBase)
                {
                    f1 = EnchantmentHelper.getModifierForCreature(attacker.getHeldItemMainhand(), ((EntityLivingBase)targetEntity).getCreatureAttribute());
                }
                else
                {
                    f1 = EnchantmentHelper.getModifierForCreature(attacker.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
                }

                float f2 = attacker.getCooledAttackStrength(0.5F);
                damage = damage * (0.2F + f2 * f2 * 0.8F);
                f1 = f1 * f2;
                attacker.resetCooldown();

                if (damage > 0.0F || f1 > 0.0F)
                {
                    boolean flag = f2 > 0.9F;
                    boolean flag1 = false;
                    int i = 0;
                    i = i + EnchantmentHelper.getKnockbackModifier(attacker);

                    boolean critical = flag && attacker.fallDistance > 0.0F && !attacker.onGround && !attacker.isOnLadder() && !attacker.isInWater() && !attacker.isPotionActive(MobEffects.BLINDNESS) && !attacker.isRiding() && targetEntity instanceof EntityLivingBase;
                    critical = critical && !attacker.isSprinting();

                    if (critical)
                    {
                        damage *= 1.5F;
                    }

                    damage = damage + f1;
                    boolean flag3 = false;
                    double d0 = (double)(attacker.distanceWalkedModified - attacker.prevDistanceWalkedModified);

                    if (flag && !critical && attacker.onGround && d0 < (double)attacker.getAIMoveSpeed())
                    {
                        ItemStack itemstack = attacker.getHeldItem(EnumHand.MAIN_HAND);

                        if (itemstack != null && itemstack.getItem() instanceof ItemSword)
                        {
                            flag3 = true;
                        }
                    }

                    float f4 = 0.0F;
                    boolean setEntityOnFire = false;
                    int fireAspect = EnchantmentHelper.getFireAspectModifier(attacker);

                    if (targetEntity instanceof EntityLivingBase)
                    {
                        f4 = ((EntityLivingBase)targetEntity).getHealth();

                        if (fireAspect > 0 && !targetEntity.isBurning())
                        {
                            setEntityOnFire = true;
                            targetEntity.setFire(1);
                        }
                    }

                    double motionX = targetEntity.motionX;
                    double motionY = targetEntity.motionY;
                    double motionZ = targetEntity.motionZ;
                    boolean didDamage = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(attacker), damage);

                    if (didDamage)
                    {
                        if (i > 0)
                        {
                            if (targetEntity instanceof EntityLivingBase)
                            {
                                ((EntityLivingBase)targetEntity).knockBack(attacker, (float)i * 0.5F, (double) MathHelper.sin(attacker.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(attacker.rotationYaw * 0.017453292F)));
                            }
                            else
                            {
                                targetEntity.addVelocity((double)(-MathHelper.sin(attacker.rotationYaw * 0.017453292F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(attacker.rotationYaw * 0.017453292F) * (float)i * 0.5F));
                            }

                            attacker.motionX *= 0.6D;
                            attacker.motionZ *= 0.6D;
                            attacker.setSprinting(false);
                        }

                        if (flag3)
                        {
                            for (EntityLivingBase entitylivingbase : attacker.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, targetEntity.getEntityBoundingBox().expand(1.0D, 0.25D, 1.0D)))
                            {
                                if (entitylivingbase != attacker && entitylivingbase != targetEntity && !attacker.isOnSameTeam(entitylivingbase) && attacker.getDistanceSqToEntity(entitylivingbase) < 9.0D)
                                {
                                    entitylivingbase.knockBack(attacker, 0.4F, (double)MathHelper.sin(attacker.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(attacker.rotationYaw * 0.017453292F)));
                                    entitylivingbase.attackEntityFrom(DamageSource.causePlayerDamage(attacker), 1.0F);
                                }
                            }

                            attacker.worldObj.playSound((EntityPlayer)null, attacker.posX, attacker.posY, attacker.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, attacker.getSoundCategory(), 1.0F, 1.0F);
                            attacker.spawnSweepParticles();
                        }

                        if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged)
                        {
                            ((EntityPlayerMP)targetEntity).connection.sendPacket(new SPacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = motionX;
                            targetEntity.motionY = motionY;
                            targetEntity.motionZ = motionZ;
                        }

                        if (critical)
                        {
                            attacker.worldObj.playSound((EntityPlayer)null, attacker.posX, attacker.posY, attacker.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, attacker.getSoundCategory(), 1.0F, 1.0F);
                            attacker.onCriticalHit(targetEntity);
                        }

                        if (!critical && !flag3)
                        {
                            if (flag)
                            {
                                attacker.worldObj.playSound((EntityPlayer)null, attacker.posX, attacker.posY, attacker.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, attacker.getSoundCategory(), 1.0F, 1.0F);
                            }
                            else
                            {
                                attacker.worldObj.playSound((EntityPlayer)null, attacker.posX, attacker.posY, attacker.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, attacker.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (f1 > 0.0F)
                        {
                            attacker.onEnchantmentCritical(targetEntity);
                        }

                        if (!attacker.worldObj.isRemote && targetEntity instanceof EntityPlayer)
                        {
                            EntityPlayer entityplayer = (EntityPlayer)targetEntity;
                            ItemStack itemstack2 = attacker.getHeldItemMainhand();
                            ItemStack itemstack3 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : null;

                            if (itemstack2 != null && itemstack3 != null && itemstack2.getItem() instanceof ItemAxe && itemstack3.getItem() == Items.SHIELD)
                            {
                                float f3 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(attacker) * 0.05F;

                                if (flag1)
                                {
                                    f3 += 0.75F;
                                }

                                if (itemRand.nextFloat() < f3)
                                {
                                    entityplayer.getCooldownTracker().setCooldown(Items.SHIELD, 100);
                                    attacker.worldObj.setEntityState(entityplayer, (byte)30);
                                }
                            }
                        }

                        if (damage >= 18.0F)
                        {
                            attacker.addStat(AchievementList.OVERKILL);
                        }

                        attacker.setLastAttacker(targetEntity);

                        if (targetEntity instanceof EntityLivingBase)
                        {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase)targetEntity, attacker);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(attacker, targetEntity);
                        ItemStack itemstack1 = attacker.getHeldItemMainhand();
                        Entity entity = targetEntity;

                        if (targetEntity instanceof EntityDragonPart)
                        {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart)targetEntity).entityDragonObj;

                            if (ientitymultipart instanceof EntityLivingBase)
                            {
                                entity = (EntityLivingBase)ientitymultipart;
                            }
                        }

                        if (itemstack1 != null && entity instanceof EntityLivingBase)
                        {
                            itemstack1.hitEntity((EntityLivingBase)entity, attacker);

                            if (itemstack1.stackSize <= 0)
                            {
                                attacker.setHeldItem(EnumHand.MAIN_HAND, null);
                                net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(attacker, itemstack1, EnumHand.MAIN_HAND);
                            }
                        }

                        if (targetEntity instanceof EntityLivingBase)
                        {
                            float f5 = f4 - ((EntityLivingBase)targetEntity).getHealth();
                            attacker.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));

                            if (fireAspect > 0)
                            {
                                targetEntity.setFire(fireAspect * 4);
                            }

                            if (attacker.worldObj instanceof WorldServer && f5 > 2.0F)
                            {
                                int k = (int)((double)f5 * 0.5D);
                                ((WorldServer)attacker.worldObj).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + (double)(targetEntity.height * 0.5F), targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D, new int[0]);
                            }
                        }

                        attacker.addExhaustion(0.3F);
                    }
                    else
                    {
                        attacker.worldObj.playSound(null, attacker.posX, attacker.posY, attacker.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, attacker.getSoundCategory(), 1.0F, 1.0F);

                        if (setEntityOnFire)
                        {
                            targetEntity.extinguish();
                        }
                    }
                }
            }
        }
    }
}
