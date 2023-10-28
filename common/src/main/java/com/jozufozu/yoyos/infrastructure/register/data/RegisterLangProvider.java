package com.jozufozu.yoyos.infrastructure.register.data;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;
import com.jozufozu.yoyos.infrastructure.register.Register;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

public class RegisterLangProvider implements DataProvider {
    private final Register register;
    // Needs to be a tree map so #save has a stable ordering.
    private final Map<String, String> data = new TreeMap<>();
    private final PackOutput output;
    private final String modid;
    private final String locale;

    public RegisterLangProvider(Register register, PackOutput packOutput) {
        this.output = packOutput;
        this.modid = register.modId;
        this.locale = "en_us";
        this.register = register;
    }

    public static String toEnglishName(ResourceLocation name) {
        return toEnglishName(name.getPath());
    }

    public static String toEnglishName(String internalName) {
        return Arrays.stream(internalName.toLowerCase(Locale.ROOT).split("_"))
            .map(StringUtils::capitalize)
            .collect(Collectors.joining(" "));
    }

    public <R, T extends R> String getAutomaticName(Register.Promise<R, T> sup) {
        return toEnglishName(sup.name.location());
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        register.runData(ProviderType.LANG, this);

        if (!data.isEmpty())
            return save(cache, this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(this.modid).resolve("lang").resolve(this.locale + ".json"));

        return CompletableFuture.allOf();
    }

    @Override
    public String getName() {
        return "Languages: " + locale;
    }

    private CompletableFuture<?> save(CachedOutput cache, Path target) {
        JsonObject json = new JsonObject();
        this.data.forEach(json::addProperty);

        return DataProvider.saveStable(cache, json, target);
    }

    public void addBlock(NotNullSupplier<? extends Block> key, String name) {
        add(key.get(), name);
    }

    public void add(Block key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addItem(NotNullSupplier<? extends Item> key, String name) {
        add(key.get(), name);
    }

    public void add(Item key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addItemStack(NotNullSupplier<ItemStack> key, String name) {
        add(key.get(), name);
    }

    public void add(ItemStack key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addEnchantment(NotNullSupplier<? extends Enchantment> key, String name) {
        add(key.get(), name);
    }

    public void add(Enchantment key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addEffect(NotNullSupplier<? extends MobEffect> key, String name) {
        add(key.get(), name);
    }

    public void add(MobEffect key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addEntityType(NotNullSupplier<? extends EntityType<?>> key, String name) {
        add(key.get(), name);
    }

    public void add(EntityType<?> key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void add(String key, String value) {
        if (data.put(key, value) != null)
            throw new IllegalStateException("Duplicate translation key " + key);
    }
}
