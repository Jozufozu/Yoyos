package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.TinkersYoyos;
import com.jozufozu.yoyos.common.materials.*;
import com.jozufozu.yoyos.network.MessageRetractYoYo;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.library.utils.TooltipBuilder;
import slimeknights.tconstruct.tools.TinkerMaterials;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

public class YoyoCore extends TinkerToolCore {

    public static HashMap<Entity, EntityYoyo> casters = new HashMap<>();

    public YoyoCore() {
        super(  new PartMaterialType(TinkersYoyos.YOYO_CORD, YoyoMaterialTypes.CORD),
                new PartMaterialType(TinkersYoyos.YOYO_BODY, YoyoMaterialTypes.BODY),
                new PartMaterialType(TinkersYoyos.YOYO_BODY, YoyoMaterialTypes.BODY),
                new PartMaterialType(TinkersYoyos.YOYO_AXLE, YoyoMaterialTypes.AXLE));

        addCategory(Category.WEAPON, Category.NO_MELEE);
    }

    @Override
    public void getSubItems(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        addDefaultSubItems(subItems, TinkerMaterials.string, null, null, null);
    }

    @Override
    public int[] getRepairParts() {
        return new int[]{1, 2};
    }

    @Override
    public float damagePotential() {
        return 0.85F;
    }

    @Override
    public double attackSpeed() {
        return 0.8;
    }

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
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
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return false;
    }

    @Override
    public List<String> getInformation(ItemStack stack, boolean detailed) {
        TooltipBuilder info = new TooltipBuilder(stack);

        YoyoNBT nbt = YoyoNBT.from(stack);
        info.addDurability(!detailed);
        info.addAttack();
        info.add(CordMaterialStats.formatLength(nbt.chordLength));
        if (nbt.duration == -1) {
            info.add(String.format("%s: %s%s",
                    Util.translate(YoyoNBT.LOC_Duration),
                    AxleMaterialStats.COLOR_Friction,
                    Util.translate(YoyoNBT.LOC_Infinite))
                    + TextFormatting.RESET);
        }
        else
            info.add(String.format("%s: %s%s %s",
                    Util.translate(YoyoNBT.LOC_Duration),
                    AxleMaterialStats.COLOR_Friction,
                    Util.df.format(nbt.duration / 20F),
                    Util.translate(YoyoNBT.LOC_Suffix))
                    + TextFormatting.RESET);
        info.add(BodyMaterialStats.formatWeight(nbt.weight));

        if (ToolHelper.getFreeModifiers(stack) > 0) {
            info.addFreeModifiers();
        }

        if (detailed) {
            info.addModifierInfo();
        }

        return info.getTooltip();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        if (!worldIn.isRemote) {
            EntityYoyo entityYoyo = casters.get(playerIn);

            if (entityYoyo != null && entityYoyo.isEntityAlive()) {
                entityYoyo.setRetracting(!entityYoyo.isRetracting());
                YoyoNetwork.INSTANCE.sendToAll(new MessageRetractYoYo(entityYoyo, false));
                playerIn.swingArm(hand);
            }
            else if (ToolHelper.getCurrentDurability(itemStackIn) > 0) {
                worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

                worldIn.spawnEntityInWorld(new EntityYoyo(worldIn, playerIn));

                playerIn.swingArm(hand);
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemStackIn);
    }
}
