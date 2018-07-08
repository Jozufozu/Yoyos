package com.jozufozu.yoyos.tinkers;

import com.google.common.collect.Multimap;
import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.common.EntityStickyYoyo;
import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.common.IYoyo;
import com.jozufozu.yoyos.common.ItemYoyo;
import com.jozufozu.yoyos.network.MessageRetractYoYo;
import com.jozufozu.yoyos.network.YoyoNetwork;
import com.jozufozu.yoyos.tinkers.materials.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.common.ClientProxy;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.library.utils.TooltipBuilder;
import slimeknights.tconstruct.tools.TinkerMaterials;

import javax.annotation.Nonnull;
import java.util.List;

public class YoyoCore extends TinkerToolCore implements IYoyo
{
    
    public YoyoCore()
    {
        super(new PartMaterialType(TinkersYoyos.YOYO_CORD, YoyoMaterialTypes.CORD),
              new PartMaterialType(TinkersYoyos.YOYO_BODY, YoyoMaterialTypes.BODY),
              new PartMaterialType(TinkersYoyos.YOYO_BODY, YoyoMaterialTypes.BODY),
              new PartMaterialType(TinkersYoyos.YOYO_AXLE, YoyoMaterialTypes.AXLE));
        
        addCategory(Category.WEAPON, Category.NO_MELEE);
    }

    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, ItemStack stack)
    {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

        // ToolCore handles mainhand, we take care of offhand
        if(slot == EntityEquipmentSlot.OFFHAND && !ToolHelper.isBroken(stack))
        {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", ToolHelper.getActualAttack(stack), 0));
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
        return 0.85F;
    }
    
    @Override
    public double attackSpeed()
    {
        return 0.8;
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
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        if (!worldIn.isRemote)
        {
            EntityYoyo entityYoyo = EntityYoyo.CASTERS.get(playerIn);
            
            if (entityYoyo != null && entityYoyo.isEntityAlive())
            {
                entityYoyo.setRetracting(!entityYoyo.isRetracting());
                YoyoNetwork.INSTANCE.sendToAll(new MessageRetractYoYo(entityYoyo));
            }
            else if (ToolHelper.getCurrentDurability(itemStackIn) > 0)
            {
                NBTTagCompound root = TagUtil.getTagSafe(itemStackIn);

                EntityYoyo yoyo;
                if (TinkerUtil.hasTrait(root, "sticky") || TinkerUtil.hasModifier(root, "gluey"))
                    yoyo = new EntityStickyYoyo(worldIn, playerIn);
                else
                    yoyo = new EntityYoyo(worldIn, playerIn);

                worldIn.spawnEntity(yoyo);

                worldIn.playSound(null, yoyo.posX, yoyo.posY, yoyo.posZ, Yoyos.YOYO_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            }
        }
        
        return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
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
        return ((int) (ToolHelper.getActualAttackSpeed(yoyo) * 5));
    }
    
    @Override
    public boolean gardening(ItemStack yoyo)
    {
        return !TinkerUtil.getModifierTag(yoyo, "gardening").hasNoTags();
    }
    
    @Override
    public boolean collecting(ItemStack yoyo)
    {
        return EnchantmentHelper.getEnchantmentLevel(Yoyos.COLLECTING, yoyo) > 0;
    }
    
    @Override
    public void damageItem(ItemStack yoyo, EntityLivingBase player)
    {
        ToolHelper.damageTool(yoyo, 1, player);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public int getCordColor(ItemStack yoyo)
    {
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(yoyo));
        return materials.get(0).materialTextColor;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public int getLeftColor(ItemStack yoyo)
    {
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(yoyo));
        return materials.get(1).materialTextColor;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public int getRightColor(ItemStack yoyo)
    {
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(yoyo));
        return materials.get(2).materialTextColor;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public int getAxleColor(ItemStack yoyo)
    {
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(yoyo));
        return materials.get(3).materialTextColor;
    }
    
    @Override
    public void attack(ItemStack yoyo, EntityPlayer player, EnumHand hand, EntityYoyo yoyoEntity, Entity target)
    {
        ToolHelper.attackEntity(yoyo, ((ToolCore) yoyo.getItem()), player, target);
    }

    @Override
    public boolean interactsWithBlocks(ItemStack yoyo)
    {
        return gardening(yoyo);
    }

    @Override
    public void blockInteraction(ItemStack yoyo, EntityPlayer player, World world, BlockPos pos, IBlockState state, Block block, EntityYoyo yoyoEntity)
    {
        ItemYoyo.garden(yoyo, player, world, pos, state, block, yoyoEntity);
    }
}
