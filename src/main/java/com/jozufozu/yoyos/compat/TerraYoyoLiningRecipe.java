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

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

public class TerraYoyoLiningRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
    public boolean isDynamic()
    {
        return true;
    }

    public boolean matches(@Nonnull InventoryCrafting var1, @Nonnull World var2)
    {
        boolean foundTerraYoyo = false;
        boolean foundElementiumYoyo = false;

        for (int i = 0; i < var1.getSizeInventory(); ++i)
        {
            ItemStack stack = var1.getStackInSlot(i);
            if (!stack.isEmpty())
            {
                if (stack.getItem() instanceof ItemTerraYoyo && !ItemTerraYoyo.isLined(stack))
                    foundTerraYoyo = true;
                else if (stack.getItem() instanceof ItemElementiumYoyo)
                    foundElementiumYoyo = true;
                else
                    return false;
            }
        }

        return foundTerraYoyo && foundElementiumYoyo;
    }

    @Nonnull
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1)
    {
        ItemStack terraYoyo = ItemStack.EMPTY;

        for (int i = 0; i < var1.getSizeInventory(); ++i)
        {
            ItemStack stack = var1.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemTerraYoyo)
            {
                terraYoyo = stack;
            }
        }

        if (terraYoyo.isEmpty())
        {
            return ItemStack.EMPTY;
        }
        else
        {
            ItemStack copy = terraYoyo.copy();
            ItemTerraYoyo.setLined(copy);
            return copy;
        }
    }

    public boolean canFit(int width, int height)
    {
        return width * height >= 2;
    }

    @Nonnull
    public ItemStack getRecipeOutput()
    {
        return ItemStack.EMPTY;
    }
}
