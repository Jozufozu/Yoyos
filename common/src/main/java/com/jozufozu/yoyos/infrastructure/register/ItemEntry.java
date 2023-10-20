package com.jozufozu.yoyos.infrastructure.register;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemEntry<T extends ItemLike> extends Entry<T> implements ItemLike {

    public ItemEntry(Register.Promise<T> promise) {
        super(promise);
    }

    @Override
    @NotNull
    public Item asItem() {
        return get().asItem();
    }

    public boolean matches(ItemStack stack) {
        return stack.is(asItem());
    }
}
