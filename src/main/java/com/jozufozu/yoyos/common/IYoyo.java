package com.jozufozu.yoyos.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IYoyo {

    float getWeight(ItemStack yoyo);

    float getLength(ItemStack yoyo);

    int getDuration(ItemStack yoyo);

    int getAttackSpeed(ItemStack yoyo);

    boolean gardening(ItemStack yoyo);

    void damageItem(ItemStack yoyo, EntityLivingBase player);

    void attack(Entity target, ItemStack yoyo, EntityPlayer player, EntityYoyo yoyoEntity);

    @SideOnly(Side.CLIENT)
    default int getChordColor(ItemStack yoyo) {
        return 0xDDDDDD;
    }
}
