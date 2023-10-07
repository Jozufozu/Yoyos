package com.jozufozu.yoyos.infrastructure.register;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemBuilder<T extends Item> {
    private final Callback<T> callback;
    private final ResourceLocation rl;
    private final Function<Item.Properties, T> factory;
    private final DataGenComponent dataGen = new DataGenComponent();

    private Supplier<Item.Properties> initialProperties = Item.Properties::new;
    private Function<Item.Properties, Item.Properties> propertiesFunction = p -> p;

    public ItemBuilder(Callback<T> callback, ResourceLocation rl, Function<Item.Properties, T> factory) {
        this.callback = callback;
        this.rl = rl;
        this.factory = factory;
    }

    public ItemBuilder<T> initialProperties(Supplier<Item.Properties> supplier) {
        initialProperties = supplier;
        return this;
    }

    public ItemBuilder<T> properties(Function<Item.Properties, Item.Properties> mutator) {
        this.propertiesFunction = this.propertiesFunction.andThen(mutator);
        return this;
    }

    public ItemBuilder<T> lang(String localized) {
        dataGen.setLang(Util.makeDescriptionId("item", rl), localized);
        return this;
    }

    public ItemEntry<T> register() {
        var supplier = callback.markForRegistration(rl, creator(), dataGen);
        return new ItemEntry<>(rl, supplier);
    }

    public Supplier<T> creator() {
        return () -> {
            var properties = initialProperties.get();
            properties = propertiesFunction.apply(properties);

            return factory.apply(properties);
        };
    }
}
