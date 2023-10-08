package com.jozufozu.yoyos.infrastructure.register;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.jozufozu.yoyos.infrastructure.register.data.DataGen;
import com.jozufozu.yoyos.infrastructure.types.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

public class Register {

    private final String modId;

    private final List<Registration<Item, ?>> items = new ArrayList<>();
    private final List<Registration<EntityType<?>, ?>> entityTypes = new ArrayList<>();

    public final List<DataGen<Item, ?>> dataGen = new ArrayList<>();

    public Register(String modId) {
        this.modId = modId;
    }

    public <T extends Item> ItemBuilder<T> item(String name, Function<Item.Properties, T> factory) {
        return item(new ResourceLocation(modId, name), factory);
    }

    public <T extends Item> ItemBuilder<T> item(ResourceLocation rl, Function<Item.Properties, T> factory) {
        return new ItemBuilder<T>(this::itemCallback, rl, factory);
    }

    private <T extends Item> Future<T> itemCallback(ResourceLocation loc, Supplier<T> creator, DataGen<Item, T> dataGen) {
        var out = new Future<T>(loc);

        dataGen.inject(out);

        this.dataGen.add(dataGen);

        var registration = new Registration<Item, T>(out, loc, creator);

        items.add(registration);

        return out;
    }

    private <T extends Entity> Future<EntityType<T>> entityCallback(ResourceLocation loc, Supplier<EntityType<T>> creator, DataGen<EntityType<?>, ?> dataGen) {
        var out = new Future<EntityType<T>>(loc);

        var registration = new Registration<EntityType<?>, EntityType<T>>(out, loc, creator);

        entityTypes.add(registration);

        return out;
    }

    public void _registerItems(BiConsumer<ResourceLocation, Item> consumer) {
        for (var item : items) {
            item.doRegister(consumer);
        }
    }

    public void collectLang(Consumer<Pair<String, String>> consumer) {
        for (DataGen<?, ?> datagen : dataGen) {
            datagen._collectLang(consumer);
        }
    }

    public <T extends Entity> EntityTypeBuilder<T> entityType(String name, EntityType.EntityFactory<T> factory, MobCategory category) {
        return entityType(new ResourceLocation(modId, name), factory, category);
    }

    public <T extends Entity> EntityTypeBuilder<T> entityType(ResourceLocation name, EntityType.EntityFactory<T> factory, MobCategory category) {
        return new EntityTypeBuilder<T>(this::entityCallback, name, factory, category);
    }

    public void _registerEntityTypes(BiConsumer<ResourceLocation, EntityType<?>> register) {
        for (Registration<EntityType<?>, ?> entityType : entityTypes) {
            entityType.doRegister(register);
        }
    }

    public static class Future<T> implements Supplier<T> {
        public final ResourceLocation name;
        private T entry = null;

        public Future(ResourceLocation name) {
            this.name = name;
        }

        public boolean isReady() {
            return entry != null;
        }

        @Override
        public T get() {
            return entry;
        }
    }

    private static class Registration<R, T extends R> {
        private final Future<T> out;
        private final ResourceLocation loc;
        private final Supplier<T> creator;

        public Registration(Future<T> out, ResourceLocation loc, Supplier<T> creator) {
            this.out = out;
            this.loc = loc;
            this.creator = creator;
        }

        public void doRegister(BiConsumer<ResourceLocation, R> register) {
            var created = creator.get();

            register.accept(loc, created);

            out.entry = created;
        }
    }
}
