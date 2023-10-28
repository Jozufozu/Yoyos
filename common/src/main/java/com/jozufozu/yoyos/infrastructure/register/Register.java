package com.jozufozu.yoyos.infrastructure.register;

import java.util.*;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jozufozu.yoyos.Constants;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullBiConsumer;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullConsumer;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullFunction;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;
import com.jozufozu.yoyos.infrastructure.register.data.DataGen;
import com.jozufozu.yoyos.infrastructure.register.data.providers.ProviderType;
import com.jozufozu.yoyos.infrastructure.register.packet.PacketBehavior;
import com.jozufozu.yoyos.infrastructure.register.packet.PacketBuilder;

import net.minecraft.core.Registry;
import net.minecraft.data.DataProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

public class Register {
    public final String modId;

    private final Map<ProviderType<?>, List<NotNullConsumer<? extends DataProvider>>> providers = new HashMap<>();
    private final Table<ResourceKey<? extends Registry<?>>, ResourceLocation, Registration<?, ?>> registrations = HashBasedTable.create();
    // Special case packets since they don't have a formal registry.
    private final List<Registration<PacketBehavior<?>, ?>> packets = new ArrayList<>();

    public Register(String modId) {
        this.modId = modId;
    }

    public <T> PacketBuilder<T> packet(String name, Class<T> clazz, NotNullFunction<FriendlyByteBuf, T> reconstruct) {
        return packet(new ResourceLocation(modId, name), clazz, reconstruct);
    }

    public <T> PacketBuilder<T> packet(ResourceLocation name, Class<T> clazz, NotNullFunction<FriendlyByteBuf, T> reconstruct) {
        return new PacketBuilder<>(this::packetCallback, name, clazz, reconstruct);
    }

    public <T extends Item> ItemBuilder<T> item(String name, NotNullFunction<Item.Properties, T> factory) {
        return item(new ResourceLocation(modId, name), factory);
    }

    public <T extends Item> ItemBuilder<T> item(ResourceLocation name, NotNullFunction<Item.Properties, T> factory) {
        return new ItemBuilder<>(this::callback, name, factory)
            .defaultLang();
    }

    public <T extends Entity> EntityBuilder<T> entity(String name, EntityType.EntityFactory<T> factory, MobCategory category) {
        return entity(new ResourceLocation(modId, name), factory, category);
    }

    public <T extends Entity> EntityBuilder<T> entity(ResourceLocation name, EntityType.EntityFactory<T> factory, MobCategory category) {
        return new EntityBuilder<>(this::callback, name, factory, category);
    }

    private <T> RegistrylessPromise<PacketBehavior<T>> packetCallback(ResourceLocation name, NotNullSupplier<PacketBehavior<T>> creator, NotNullConsumer<PacketBehavior<T>> onRegister) {
        var out = new RegistrylessPromise<PacketBehavior<T>>(name);

        var reg = new Registration<PacketBehavior<?>, PacketBehavior<T>>(onRegister.butBeforeThat(out::acceptEntry), name, creator);

        packets.add(reg);

        return out;
    }

    private <R, T extends R> Promise<R, T> callback(ResourceKey<? extends Registry<R>> key, ResourceLocation name, NotNullSupplier<T> creator, DataGen<R, T> dataGen, NotNullConsumer<T> postRegister) {
        var out = new Promise<R, T>(ResourceKey.create(key, name));

        for (var entry : dataGen.getProviders().entrySet()) {
            providers.computeIfAbsent(entry.getKey(), $ -> new ArrayList<>())
                .add(entry.getValue().applyFirst(out));
        }

        var registration = new Registration<R, T>(postRegister.butBeforeThat(out::acceptEntry), name, creator);

        registrations.put(key, name, registration);

        return out;
    }

    @SuppressWarnings("unchecked")
    public <R> void _register(ResourceKey<? extends Registry<R>> key, NotNullBiConsumer<ResourceLocation, R> consumer) {
        for (var registration : registrations.row(key).values()) {
            ((Registration<R, ?>) registration).doRegister(consumer);
        }
    }

    public void _registerPackets(NotNullBiConsumer<ResourceLocation, PacketBehavior<?>> consumer) {
        for (Registration<PacketBehavior<?>, ?> registration : packets) {
            registration.doRegister(consumer);
        }
    }

    public DamageTypeBuilder damageType(String name) {
        return damageType(name, name);
    }

    public DamageTypeBuilder damageType(String name, String msgId) {
        return damageType(new ResourceLocation(modId, name), msgId);
    }

    public DamageTypeBuilder damageType(ResourceLocation name, String msgId) {
        return new DamageTypeBuilder(name, this::callback, msgId);
    }

    public <D extends DataProvider> void runData(ProviderType<? extends D> type, D dataProvider) {
        providers.getOrDefault(type, Collections.emptyList()).forEach(cons -> {
            try {
                ((NotNullConsumer<D>) cons).accept(dataProvider);
            } catch (Exception e) {
                Constants.LOG.error("Error while running datagen", e);
            }
        });
    }

    public static class Promise<R, T extends R> implements NotNullSupplier<T> {
        public final ResourceKey<R> name;
        private T entry = null;

        private Promise(ResourceKey<R> name) {
            this.name = name;
        }

        public boolean isReady() {
            return entry != null;
        }

        private void acceptEntry(T entry) {
            this.entry = entry;
        }

        @Override
        @NotNull
        public T get() {
            return Objects.requireNonNull(entry, name + " was never registered.");
        }
    }

    public static class RegistrylessPromise<T> implements NotNullSupplier<T> {
        public final ResourceLocation name;
        private T entry = null;

        private RegistrylessPromise(ResourceLocation name) {
            this.name = name;
        }

        public boolean isReady() {
            return entry != null;
        }

        private void acceptEntry(T entry) {
            this.entry = entry;
        }

        @Override
        @NotNull
        public T get() {
            return Objects.requireNonNull(entry, name + " was never registered.");
        }
    }

    private record Registration<R, T extends R>(NotNullConsumer<T> postRegister, ResourceLocation loc, NotNullSupplier<T> creator) {
        public void doRegister(BiConsumer<ResourceLocation, R> register) {
                var created = creator.get();

                register.accept(loc, created);

                postRegister.accept(created);
        }
    }
}
