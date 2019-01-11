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

package com.jozufozu.yoyos.compat;

import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.common.IYoyo;
import com.jozufozu.yoyos.common.ItemYoyo;
import com.jozufozu.yoyos.common.ModConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import java.util.function.BiFunction;

public class ItemManaYoyo extends ItemYoyo implements IYoyo, IManaUsingItem
{
    private final int manaPerDamage;

    public ItemManaYoyo(String name, ToolMaterial material, int manaPerDamage)
    {
        this(name, material, manaPerDamage, EntityYoyo::new);
    }

    public ItemManaYoyo(String name, ToolMaterial material, int manaPerDamage, BiFunction<World, EntityPlayer, EntityYoyo> entityFactory)
    {
        super(name, material, entityFactory);
        this.manaPerDamage = manaPerDamage;
    }

    @Override
    public boolean usesMana(ItemStack itemStack)
    {
        return true;
    }

    @Override
    public float getAttackDamage(ItemStack yoyo)
    {
        return ModConfig.vanillaYoyos.ironYoyo.damage;
    }

    @Override
    public float getWeight(ItemStack yoyo)
    {
        return ModConfig.vanillaYoyos.ironYoyo.weight;
    }
    
    @Override
    public float getLength(ItemStack yoyo)
    {
        return ModConfig.vanillaYoyos.ironYoyo.length;
    }
    
    @Override
    public int getDuration(ItemStack yoyo)
    {
        return ModConfig.vanillaYoyos.ironYoyo.duration;
    }

    @Override
    public void damageItem(ItemStack yoyo, int amount, EntityLivingBase player)
    {
        ToolCommons.damageItem(yoyo, 1, player, manaPerDamage);
    }

    @Override
    public int getCordColor(ItemStack yoyo, float ticks)
    {
        return 0xadf4e2; // Mana mint
    }
}
