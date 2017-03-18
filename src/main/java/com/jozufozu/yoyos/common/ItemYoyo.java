package com.jozufozu.yoyos.common;

import com.google.common.collect.ImmutableSet;
import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.network.MessageRetractYoYo;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class ItemYoyo extends ItemSword implements IYoyo {

    private final ToolMaterial material;
    private final boolean gardening;

    public ItemYoyo(String name, ToolMaterial material) {
        this(name, material, false);
    }

    public ItemYoyo(String name, ToolMaterial material, boolean gardening) {
        super(material);
        this.material = material;
        this.gardening = gardening;

        this.setUnlocalizedName(name);
        this.setRegistryName(Yoyos.MODID, name);

        if (!gardening)
            this.setCreativeTab(CreativeTabs.COMBAT);
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        return ImmutableSet.of("yoyo");
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
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("info.weight.name", getWeight(stack)));
        tooltip.add(I18n.format("info.length.name", getLength(stack)));
        tooltip.add(I18n.format("info.duration.name", ((float) getDuration(stack)) / 20F));
    }

    @Override
    public float getWeight(ItemStack yoyo) {
        if (this.gardening)
            return 2.5F;

        switch (this.material) {
            case WOOD:
                return 2.2F;
            case STONE:
                return 4.0F;
            case IRON:
                return 5.0F;
            case DIAMOND:
                return 1.7F;
            case GOLD:
                return 5.5F;
            default:
                return 1.0F;
        }
    }

    @Override
    public float getLength(ItemStack yoyo) {
        return 5.0F + this.material.getEfficiencyOnProperMaterial() / 2;
    }

    @Override
    public int getDuration(ItemStack yoyo) {
        return ((int) (50 * this.material.getEfficiencyOnProperMaterial()));
    }

    @Override
    public int getAttackSpeed(ItemStack yoyo) {
        return 10;
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
    public void attack(Entity targetEntity, ItemStack stack, EntityPlayer attacker, EntityYoyo yoyoEntity) {
        if (!net.minecraftforge.common.ForgeHooks.onPlayerAttackTarget(attacker, targetEntity)) return;
        if (targetEntity.canBeAttackedWithItem())
        {
            if (!targetEntity.hitByEntity(attacker))
            {
                float damage = (float)attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float attackModifier;

                if (targetEntity instanceof EntityLivingBase)
                {
                    attackModifier = EnchantmentHelper.getModifierForCreature(attacker.getHeldItemMainhand(), ((EntityLivingBase)targetEntity).getCreatureAttribute());
                }
                else
                {
                    attackModifier = EnchantmentHelper.getModifierForCreature(attacker.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
                }

                float attackStrength = attacker.getCooledAttackStrength(0.5F);
                damage = damage * (0.2F + attackStrength * attackStrength * 0.8F);
                attackModifier = attackModifier * attackStrength;
                attacker.resetCooldown();

                if (damage > 0.0F || attackModifier > 0.0F)
                {
                    boolean flag = attackStrength > 0.9F;
                    int knockbackModifier = 0;
                    knockbackModifier = knockbackModifier + EnchantmentHelper.getKnockbackModifier(attacker);

                    boolean critical = flag && attacker.fallDistance > 0.0F && !attacker.onGround && !attacker.isOnLadder() && !attacker.isInWater() && !attacker.isPotionActive(MobEffects.BLINDNESS) && !attacker.isRiding() && targetEntity instanceof EntityLivingBase;
                    critical = critical && !attacker.isSprinting();

                    if (critical)
                    {
                        damage *= 1.5F;
                    }

                    damage = damage + attackModifier;

                    float targetHealth = 0.0F;
                    boolean setEntityOnFire = false;
                    int fireAspect = EnchantmentHelper.getFireAspectModifier(attacker);

                    if (targetEntity instanceof EntityLivingBase)
                    {
                        targetHealth = ((EntityLivingBase)targetEntity).getHealth();

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

                    if (didDamage) {
                        if (knockbackModifier > 0) {
                            if (targetEntity instanceof EntityLivingBase) {
                                ((EntityLivingBase)targetEntity).knockBack(attacker, (float)knockbackModifier * 0.5F, (double) MathHelper.sin(attacker.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(attacker.rotationYaw * 0.017453292F)));
                            }
                            else {
                                targetEntity.addVelocity((double)(-MathHelper.sin(attacker.rotationYaw * 0.017453292F) * (float)knockbackModifier * 0.5F), 0.1D, (double)(MathHelper.cos(attacker.rotationYaw * 0.017453292F) * (float)knockbackModifier * 0.5F));
                            }
                        }

                        if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                            ((EntityPlayerMP)targetEntity).connection.sendPacket(new SPacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = motionX;
                            targetEntity.motionY = motionY;
                            targetEntity.motionZ = motionZ;
                        }

                        if (critical) {
                            attacker.worldObj.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, attacker.getSoundCategory(), 1.0F, 1.0F);
                            attacker.onCriticalHit(targetEntity);
                        }

                        if (!critical) {
                            if (flag) {
                                attacker.worldObj.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, attacker.getSoundCategory(), 1.0F, 1.0F);
                            }
                            else {
                                attacker.worldObj.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, attacker.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (attackModifier > 0.0F) {
                            attacker.onEnchantmentCritical(targetEntity);
                        }

                        if (damage >= 18.0F) {
                            attacker.addStat(AchievementList.OVERKILL);
                        }

                        attacker.setLastAttacker(targetEntity);

                        if (targetEntity instanceof EntityLivingBase) {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase)targetEntity, attacker);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(attacker, targetEntity);
                        ItemStack itemstack1 = attacker.getHeldItemMainhand();
                        Entity entity = targetEntity;

                        if (targetEntity instanceof EntityDragonPart) {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart)targetEntity).entityDragonObj;

                            if (ientitymultipart instanceof EntityLivingBase) {
                                entity = (EntityLivingBase)ientitymultipart;
                            }
                        }

                        if (itemstack1 != null && entity instanceof EntityLivingBase) {
                            itemstack1.hitEntity((EntityLivingBase)entity, attacker);

                            if (itemstack1.stackSize <= 0) {
                                attacker.setHeldItem(EnumHand.MAIN_HAND, null);
                                ForgeEventFactory.onPlayerDestroyItem(attacker, itemstack1, EnumHand.MAIN_HAND);
                            }
                        }

                        if (targetEntity instanceof EntityLivingBase) {
                            float f5 = targetHealth - ((EntityLivingBase)targetEntity).getHealth();
                            attacker.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));

                            if (fireAspect > 0) {
                                targetEntity.setFire(fireAspect * 4);
                            }

                            if (attacker.worldObj instanceof WorldServer && f5 > 2.0F) {
                                int k = (int)((double)f5 * 0.5D);
                                ((WorldServer)attacker.worldObj).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + (double)(targetEntity.height * 0.5F), targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D, new int[0]);
                            }
                        }

                        attacker.addExhaustion(0.3F);
                    }
                    else
                    {
                        attacker.worldObj.playSound(null, yoyoEntity.posX, yoyoEntity.posY, yoyoEntity.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, attacker.getSoundCategory(), 1.0F, 1.0F);

                        if (setEntityOnFire) {
                            targetEntity.extinguish();
                        }
                    }
                }
            }
        }
    }
}
