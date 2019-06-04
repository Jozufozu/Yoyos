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

import com.jozufozu.yoyos.common.ModConfig;
import net.minecraft.item.ItemStack;
import vazkii.botania.api.item.IPixieSpawner;

public class ItemElementiumYoyo extends ItemManaYoyo implements IPixieSpawner
{
    public ItemElementiumYoyo(String name, ToolMaterial material, int manaPerDamage)
    {
        super(name, material, manaPerDamage);
    }

    @Override
    public int getMaxCollectedDrops(ItemStack yoyo)
    {
        return 64 + super.getMaxCollectedDrops(yoyo);
    }

    @Override
    public float getPixieChance(ItemStack itemStack)
    {
        return 0.05f;
    }

    @Override
    public float getAttackDamage(ItemStack yoyo)
    {
        return ModConfig.botaniaYoyos.elementiumYoyo.damage;
    }

    @Override
    public float getWeight(ItemStack yoyo)
    {
        return ModConfig.botaniaYoyos.elementiumYoyo.weight;
    }

    @Override
    public float getLength(ItemStack yoyo)
    {
        return ModConfig.botaniaYoyos.elementiumYoyo.length;
    }

    @Override
    public int getDuration(ItemStack yoyo)
    {
        return ModConfig.botaniaYoyos.elementiumYoyo.duration;
    }
}
