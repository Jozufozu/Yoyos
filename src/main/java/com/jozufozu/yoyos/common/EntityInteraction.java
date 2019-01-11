package com.jozufozu.yoyos.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

@FunctionalInterface
public interface EntityInteraction
{
    void doInteraction(IYoyo yoyo, ItemStack stack, EntityPlayer attacker, EnumHand yoyoHand, EntityYoyo yoyoEntity, Entity targetEntity);
}
