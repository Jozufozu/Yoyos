package com.jozufozu.yoyos.infrastructure.register;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.jozufozu.yoyos.infrastructure.types.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class Register {

    private final String modId;

    private final List<Registration<Item, ?>> items = new ArrayList<>();

    private final List<DataGenComponent> dataGenComponents = new ArrayList<>();

    public Register(String modId) {
        this.modId = modId;
    }

    public <T extends Item> ItemBuilder<T> item(String name, Function<Item.Properties, T> factory) {
        return item(new ResourceLocation(modId, name), factory);
    }

    public <T extends Item> ItemBuilder<T> item(ResourceLocation rl, Function<Item.Properties, T> factory) {
        return new ItemBuilder<>(this::itemCallback, rl, factory);
    }

    private <T extends Item> Supplier<T> itemCallback(ResourceLocation loc, Supplier<T> creator, DataGenComponent dataGen) {
        dataGenComponents.add(dataGen);

        var out = new RegisterFuture<T>();

        var registration = new Registration<Item, T>(out, loc, creator);

        items.add(registration);

        return out;
    }

    public void _registerItems(BiConsumer<ResourceLocation, Item> consumer) {
        for (var item : items) {
            item.doRegister(consumer);
        }
    }

    public List<Pair<String, String>> getLangEntries() {
        var out = new ArrayList<Pair<String, String>>();
        for (DataGenComponent dataGenComponent : dataGenComponents) {
            dataGenComponent._collectLang(out);
        }
        return out;
    }

    private static class RegisterFuture<T> implements Supplier<T> {
        private T entry = null;

        @Override
        public T get() {
            return entry;
        }
    }

    private static class Registration<R, T extends R> {
        private final RegisterFuture<T> out;
        private final ResourceLocation loc;
        private final Supplier<T> creator;

        public Registration(RegisterFuture<T> out, ResourceLocation loc, Supplier<T> creator) {
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
