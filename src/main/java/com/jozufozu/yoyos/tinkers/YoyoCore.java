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

package com.jozufozu.yoyos.tinkers;

import com.google.common.collect.Multimap;
import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.common.*;
import com.jozufozu.yoyos.tinkers.materials.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentKeybind;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.common.ClientProxy;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.library.utils.TooltipBuilder;
import slimeknights.tconstruct.tools.TinkerMaterials;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector3d;
import java.util.List;

public class YoyoCore extends TinkerToolCore implements IYoyo
{
    public YoyoCore()
    {
        super(new PartMaterialType(TinkersYoyos.YOYO_CORD, YoyoMaterialTypes.CORD),
              new PartMaterialType(TinkersYoyos.YOYO_BODY, YoyoMaterialTypes.BODY),
              new PartMaterialType(TinkersYoyos.YOYO_BODY, YoyoMaterialTypes.BODY),
              new PartMaterialType(TinkersYoyos.YOYO_AXLE, YoyoMaterialTypes.AXLE));
        
        addCategory(Category.WEAPON, Category.NO_MELEE, Category.HARVEST, Category.PROJECTILE);
    }

    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, ItemStack stack)
    {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

        if (slot == EntityEquipmentSlot.OFFHAND && !ToolHelper.isBroken(stack))
        {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getAttackDamage(stack), 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", ToolHelper.getActualAttackSpeed(stack) - 4d, 0));
        }

        return multimap;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        if (this.isInCreativeTab(tab))
            addDefaultSubItems(subItems, TinkerMaterials.string, null, null, null);
    }
    
    @Override
    public int[] getRepairParts()
    {
        return new int[]{1, 2};
    }
    
    @Override
    public float damagePotential()
    {
        return 1.0F;
    }
    
    @Override
    public double attackSpeed()
    {
        return 1.6;
    }
    
    @Override
    protected ToolNBT buildTagData(List<Material> materials)
    {
        CordMaterialStats chord = materials.get(0).getStatsOrUnknown(YoyoMaterialTypes.CORD);
        BodyMaterialStats side1 = materials.get(1).getStatsOrUnknown(YoyoMaterialTypes.BODY);
        BodyMaterialStats side2 = materials.get(2).getStatsOrUnknown(YoyoMaterialTypes.BODY);
        AxleMaterialStats core = materials.get(3).getStatsOrUnknown(YoyoMaterialTypes.AXLE);
        
        YoyoNBT data = new YoyoNBT();
        
        data.side(side1, side2);
        data.core(core);
        data.chord(chord);
        
        return data;
    }
    
    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
    {
        return false;
    }
    
    @Override
    public List<String> getInformation(ItemStack stack, boolean detailed)
    {
        TooltipBuilder info = new TooltipBuilder(stack);
        
        YoyoNBT nbt = YoyoNBT.from(stack);
        info.addDurability(!detailed);
        info.addAttack();
        info.add(CordMaterialStats.formatLength(nbt.chordLength));

        if (nbt.duration < 0)
            info.add(String.format("%s: %s%s", Util.translate(YoyoNBT.LOC_Duration), AxleMaterialStats.COLOR_Friction, Util.translate(YoyoNBT.LOC_Infinite)) + TextFormatting.RESET);
        else
            info.add(String.format("%s: %s%s %s", Util.translate(YoyoNBT.LOC_Duration), AxleMaterialStats.COLOR_Friction, Util.df.format(nbt.duration / 20F), Util.translate(YoyoNBT.LOC_Suffix)) + TextFormatting.RESET);

        info.add(BodyMaterialStats.formatWeight(nbt.weight));
        
        if (ToolHelper.getFreeModifiers(stack) > 0)
            info.addFreeModifiers();
        
        if (detailed)
            info.addModifierInfo();
        
        return info.getTooltip();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if (isSticky(stack))
        {
            tooltip.add("");
            tooltip.add(I18n.format("yoyos.info.sticky.name"));
            tooltip.add(I18n.format("yoyos.info.sticky.retraction.name", new TextComponentKeybind("key.sneak").getUnformattedText()));
            tooltip.add(I18n.format("yoyos.info.sticky.release.name", new TextComponentKeybind("key.jump").getUnformattedText()));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        if (!worldIn.isRemote && !EntityYoyo.CASTERS.containsKey(playerIn))
        {
            if (ToolHelper.getCurrentDurability(itemStackIn) > 0)
            {
                EntityYoyo yoyo;
                if (isSticky(itemStackIn))
                    yoyo = new EntityStickyYoyo(worldIn, playerIn);
                else
                    yoyo = new EntityYoyo(worldIn, playerIn);

                worldIn.spawnEntity(yoyo);

                worldIn.playSound(null, yoyo.posX, yoyo.posY, yoyo.posZ, Yoyos.YOYO_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

                playerIn.addExhaustion(0.05F);
            }
        }
        
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
    }

    public boolean isSticky(ItemStack yoyo)
    {
        NBTTagCompound root = TagUtil.getTagSafe(yoyo);
        return TinkerUtil.hasTrait(root, "sticky") || TinkerUtil.hasModifier(root, "gluey");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Material getMaterialForPartForGuiRendering(int index)
    {
        switch(index)
        {
            case 0: return ClientProxy.RenderMaterialString;
            case 1: return ClientProxy.RenderMaterials[2];
            case 2: return ClientProxy.RenderMaterials[1];
            case 3: return ClientProxy.RenderMaterials[0];
            default: return super.getMaterialForPartForGuiRendering(index);
        }
    }

    @Override
    public float getAttackDamage(ItemStack yoyo)
    {
        return ToolHelper.getActualAttack(yoyo);
    }

    @Override
    public float getWeight(ItemStack yoyo)
    {
        return YoyoNBT.from(yoyo).weight;
    }
    
    @Override
    public float getLength(ItemStack yoyo)
    {
        return YoyoNBT.from(yoyo).chordLength;
    }
    
    @Override
    public int getDuration(ItemStack yoyo)
    {
        return YoyoNBT.from(yoyo).duration;
    }
    
    @Override
    public int getAttackSpeed(ItemStack yoyo)
    {
        // https://www.desmos.com/calculator/a6kcnvxegv
        return (int)(Math.ceil(11 / Math.sqrt(ToolHelper.getActualAttackSpeed(yoyo)))) + 1;
    }

    @Override
    public int getMaxCollectedDrops(ItemStack yoyo)
    {
        return ItemYoyo.calculateMaxCollectedDrops(new ModifierNBT(TinkerUtil.getModifierTag(yoyo, "collecting")).level);
    }
    
    @Override
    public void damageItem(ItemStack yoyo, int amount, EntityLivingBase player)
    {
        if (!(player instanceof EntityPlayer) || !((EntityPlayer) player).capabilities.isCreativeMode)
        {
            ToolHelper.damageTool(yoyo, 1, player);
        }
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public int getCordColor(ItemStack yoyo, float ticks)
    {
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(yoyo));
        return materials.get(0).materialTextColor;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public int getLeftColor(ItemStack yoyo, float ticks)
    {
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(yoyo));
        return materials.get(1).materialTextColor;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public int getRightColor(ItemStack yoyo, float ticks)
    {
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(yoyo));
        return materials.get(2).materialTextColor;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public int getAxleColor(ItemStack yoyo, float ticks)
    {
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(yoyo));
        return materials.get(3).materialTextColor;
    }
    
    @Override
    public void entityInteraction(ItemStack yoyo, EntityPlayer player, EnumHand hand, EntityYoyo yoyoEntity, Entity target)
    {
        if (ItemYoyo.collectItem(yoyo, player, hand, yoyoEntity, target)) return;

        NBTTagList tagList = TagUtil.getModifiersTagList(yoyo);
        int index = TinkerUtil.getIndexInCompoundList(tagList, "gardening");

        if (index != -1)
        {
            ItemYoyo.shearEntity(yoyo, player, hand, yoyoEntity, target);
            return;
        }

        ToolHelper.attackEntity(yoyo, ((ToolCore) yoyo.getItem()), player, target, yoyoEntity);
    }

    @Override
    public boolean interactsWithBlocks(ItemStack yoyo)
    {
        NBTTagList tagList = TagUtil.getModifiersTagList(yoyo);
        int index = TinkerUtil.getIndexInCompoundList(tagList, "gardening");

        if (index != -1) return true;

        index = TinkerUtil.getIndexInCompoundList(tagList, "farming");

        return index != -1;
    }

    @Override
    public void blockInteraction(ItemStack yoyo, EntityPlayer player, World world, BlockPos pos, IBlockState state, Block block, EntityYoyo yoyoEntity)
    {
        NBTTagList tagList = TagUtil.getModifiersTagList(yoyo);
        int index = TinkerUtil.getIndexInCompoundList(tagList, "gardening");

        if (index != -1)
        {
            ItemYoyo.garden(yoyo, player, pos, state, block, yoyoEntity);
            return;
        }

        index = TinkerUtil.getIndexInCompoundList(tagList, "farming");

        if (index != -1)
        {
            ItemYoyo.farm(yoyo, player, pos, state, block, yoyoEntity);
            ItemYoyo.till(yoyo, player, pos, state, block, yoyoEntity);
        }
    }

    @Override
    public float getWaterMovementModifier(ItemStack yoyo)
    {
        NBTTagList tagList = TagUtil.getModifiersTagList(yoyo);

        for (int i = 0; i < tagList.tagCount(); i++) {
            ModifierNBT data = ModifierNBT.readTag(tagList.getCompoundTagAt(i));
            if (data.identifier.matches("fins|aquadynamic")) {
                return 0.8f;
            }
        }

        return 0.3f;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
    {
        if (!EntityYoyo.CASTERS.containsKey(entityIn)) super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public void onUpdate(ItemStack yoyo, EntityYoyo yoyoEntity)
    {
        int itemDamage = yoyo.getItemDamage();
        for (ITrait trait : TinkerUtil.getTraitsOrdered(yoyo))
        {
            trait.onUpdate(yoyo, yoyoEntity.world, yoyoEntity, 0, true);
        }

        // The traits might have accidentally damaged the item because they couldn't tell it was held by a creative player
        if (yoyoEntity.getThrower() instanceof EntityPlayer && ((EntityPlayer) yoyoEntity.getThrower()).isCreative())
            yoyo.setItemDamage(itemDamage);

        if (!yoyoEntity.isCollecting()) return;

        NBTTagList modifiersTagList = TagUtil.getModifiersTagList(yoyo);
        int index = TinkerUtil.getIndexInCompoundList(modifiersTagList, "magnetic");
        if (index == -1) return;
        ModifierNBT data = new ModifierNBT(modifiersTagList.getCompoundTagAt(index));

        double x = yoyoEntity.posX;
        double y = yoyoEntity.posY;
        double z = yoyoEntity.posZ;
        double range = 1.8d + data.level * 0.3;

        List<EntityItem> items = yoyoEntity.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(x - range, y - range, z - range, x + range, y + range, z + range));
        int pulled = 0;
        for (EntityItem item : items) {
            if (item.getItem().isEmpty() || item.isDead) continue;

            if (pulled > 200) break;

            // constant force!
            float strength = 0.07f;

            // calculate direction: item -> player
            Vector3d vec = new Vector3d(x, y, z);
            vec.sub(new Vector3d(item.posX, item.posY, item.posZ));

            vec.normalize();
            vec.scale(strength);

            // we calculated the movement vector and set it to the correct strength.. now we apply it \o/
            item.motionX += vec.x;
            item.motionY += vec.y;
            item.motionZ += vec.z;

            pulled++;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public RenderOrientation getRenderOrientation(ItemStack yoyo)
    {
        return (TinkerUtil.getModifierTag(yoyo, "gardening").hasNoTags()) ? RenderOrientation.Vertical : RenderOrientation.Horizontal;
    }
}
