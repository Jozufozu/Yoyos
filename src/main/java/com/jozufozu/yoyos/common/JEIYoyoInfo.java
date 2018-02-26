package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.Yoyos;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JEIYoyoInfo implements IModPlugin
{
    public JEIYoyoInfo() { }

    @Override
    public void register(IModRegistry registry)
    {
        registry.addIngredientInfo(ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(Yoyos.COLLECTING, 1)),
                                   ItemStack.class,
                                   "yoyos.jei.info.collecting");
    }
}
