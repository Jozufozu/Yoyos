package com.jozufozu.yoyos.infrastructure.register;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullConsumer;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullFunction;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;
import com.jozufozu.yoyos.infrastructure.register.data.DataGen;
import com.jozufozu.yoyos.infrastructure.types.Pair;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
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

    private final Table<ResourceKey<? extends Registry<?>>, ResourceLocation, Registration<?, ?>> registrations = HashBasedTable.create();

    public Register(String modId) {
        this.modId = modId;
    }

    public <T extends Item> ItemBuilder<T> item(String name, NotNullFunction<Item.Properties, T> factory) {
        return item(new ResourceLocation(modId, name), factory);
    }

    public <T extends Item> ItemBuilder<T> item(ResourceLocation rl, NotNullFunction<Item.Properties, T> factory) {
        return new ItemBuilder<>(this::callback, rl, factory);
    }

    public <T extends Entity> EntityBuilder<T> entity(String name, EntityType.EntityFactory<T> factory, MobCategory category) {
        return entity(new ResourceLocation(modId, name), factory, category);
    }

    public <T extends Entity> EntityBuilder<T> entity(ResourceLocation name, EntityType.EntityFactory<T> factory, MobCategory category) {
        return new EntityBuilder<>(this::callback, name, factory, category);
    }

    private <R, T extends R> Promise<T> callback(ResourceKey<? extends Registry<R>> key, ResourceLocation name, NotNullSupplier<T> creator, DataGen<R, T> dataGen, NotNullConsumer<T> postRegister) {
        var out = new Promise<T>(name);

        dataGen.inject(out);

        var registration = new Registration<R, T>(out, name, creator, postRegister);

        registrations.put(key, name, registration);

        return out;
    }

    public <R> void _register(ResourceKey<? extends Registry<R>> key, BiConsumer<ResourceLocation, R> consumer) {
        for (var registration : registrations.row(key).values()) {
            ((Registration<R, ?>) registration).doRegister(consumer);
        }
    }

    public void collectLang(Consumer<Pair<String, String>> consumer) {
        for (DataGen<?, ?> datagen : dataGen) {
            datagen._collectLang(consumer);
        }
    }

    public static class Promise<T> implements NotNullSupplier<T> {
        public final ResourceLocation name;
        private T entry = null;

        public Promise(ResourceLocation name) {
            this.name = name;
        }

        public boolean isReady() {
            return entry != null;
        }

        @Override
        @NotNull
        public T get() {
            return Objects.requireNonNull(entry, name.toString() + " was never registered.");
        }
    }

    private record Registration<R, T extends R>(Promise<T> out, ResourceLocation loc, NotNullSupplier<T> creator,
                                                NotNullConsumer<T> postRegister) {

        public void doRegister(BiConsumer<ResourceLocation, R> register) {
                var created = creator.get();

                register.accept(loc, created);

                postRegister.accept(created);

                out.entry = created;
            }
        }
}
