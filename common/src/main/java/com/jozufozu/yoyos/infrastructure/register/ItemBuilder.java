package com.jozufozu.yoyos.infrastructure.register;

import java.util.Objects;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullFunction;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;
import com.jozufozu.yoyos.infrastructure.register.data.DataGen;
import com.jozufozu.yoyos.infrastructure.register.data.ModelBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemBuilder<T extends Item> extends AbstractBuilder<Item, T, ItemBuilder<T>> {
    private final NotNullFunction<Item.Properties, T> factory;
    private final DataGen<Item, T> dataGen = new DataGen<>();

    private NotNullSupplier<Item.Properties> initialProperties = Item.Properties::new;
    private NotNullFunction<Item.Properties, Item.Properties> propertiesFunction = NotNullFunction.identity();

    public ItemBuilder(RegistrationCallback<Item, T> registrationCallback, ResourceLocation rl, NotNullFunction<Item.Properties, T> factory) {
        super(rl, registrationCallback, Registries.ITEM);
        this.factory = factory;
    }

    public ItemBuilder<T> initialProperties(NotNullSupplier<Item.Properties> supplier) {
        Objects.requireNonNull(supplier);
        initialProperties = supplier;
        return this;
    }

    public ItemBuilder<T> properties(NotNullFunction<Item.Properties, Item.Properties> mutator) {
        Objects.requireNonNull(mutator);
        this.propertiesFunction = this.propertiesFunction.andThen(mutator);
        return this;
    }

    public ItemBuilder<T> defaultLang() {
        return lang(Item::getDescriptionId);
    }

    public ItemBuilder<T> lang(String localized) {
        return lang(Item::getDescriptionId, localized);
    }

    @Override
    public ItemEntry<T> register() {
        return (ItemEntry<T>) super.register();
    }

    @Override
    protected ItemEntry<T> wrap(Register.Promise<Item, T> promise) {
        return new ItemEntry<>(promise);
    }

    @Override
    protected T create() {
        return factory.apply(createAndMutateProperties());
    }

    public ItemBuilder<T> model(NotNullFunction<ModelBuilder, ModelBuilder> mutator) {
        dataGen.model(mutator);
        return this;
    }

    private Item.Properties createAndMutateProperties() {
        var properties = initialProperties.get();
        properties = propertiesFunction.apply(properties);
        return properties;
    }
}
