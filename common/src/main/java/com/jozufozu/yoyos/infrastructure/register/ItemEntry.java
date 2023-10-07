package com.jozufozu.yoyos.infrastructure.register;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class ItemEntry<T extends ItemLike> extends Entry<T> implements ItemLike {

    public ItemEntry(ResourceLocation loc, Supplier<T> supplier) {
        super(loc, supplier);
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }
}
