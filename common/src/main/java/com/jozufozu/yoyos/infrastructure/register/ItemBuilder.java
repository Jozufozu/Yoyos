package com.jozufozu.yoyos.infrastructure.register;

import java.util.function.Function;
import java.util.function.Supplier;

import com.jozufozu.yoyos.infrastructure.register.data.DataGen;
import com.jozufozu.yoyos.infrastructure.register.data.ModelBuilder;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemBuilder<T extends Item> {
    private final Callback<Item, T> callback;
    private final ResourceLocation rl;
    private final Function<Item.Properties, T> factory;
    private final DataGen<Item, T> dataGen = new DataGen<>();

    private Supplier<Item.Properties> initialProperties = Item.Properties::new;
    private Function<Item.Properties, Item.Properties> propertiesFunction = p -> p;

    public ItemBuilder(Callback<Item, T> callback, ResourceLocation rl, Function<Item.Properties, T> factory) {
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
        return () -> factory.apply(createAndMutateProperties());
    }

    public ItemBuilder<T> model(Function<ModelBuilder, ModelBuilder> mutator) {
        dataGen.model(mutator);
        return this;
    }

    private Item.Properties createAndMutateProperties() {
        var properties = initialProperties.get();
        properties = propertiesFunction.apply(properties);
        return properties;
    }
}