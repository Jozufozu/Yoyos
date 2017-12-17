package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.Yoyos;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;

public class EnchantmentCollecting extends Enchantment
{
    public EnchantmentCollecting(Rarity rarityIn, EnumEnchantmentType typeIn, EntityEquipmentSlot[] slots)
    {
        super(rarityIn, typeIn, slots);
        this.setName("collecting");
        this.setRegistryName(new ResourceLocation(Yoyos.MODID, "collecting"));
    }
}
