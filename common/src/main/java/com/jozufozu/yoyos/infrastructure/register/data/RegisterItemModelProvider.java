package com.jozufozu.yoyos.infrastructure.register.data;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.jozufozu.yoyos.infrastructure.register.Register;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class RegisterItemModelProvider implements DataProvider {

    private final Register register;
    private final PackOutput packOutput;

    private final Map<ResourceLocation, ItemModelBuilder> generatedModels = new HashMap<>();

    public RegisterItemModelProvider(Register register, PackOutput packOutput) {
        this.register = register;
        this.packOutput = packOutput;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        register.runData(ProviderType.ITEM_MODEL, this);

        return generateAll(cache);
    }

    protected CompletableFuture<?> generateAll(CachedOutput cache) {
        CompletableFuture<?>[] futures = new CompletableFuture<?>[this.generatedModels.size()];
        int i = 0;

        for (var model : this.generatedModels.values()) {
            Path target = getPath(model);
            futures[i++] = DataProvider.saveStable(cache, model.serialize(), target);
        }

        return CompletableFuture.allOf(futures);
    }

    protected Path getPath(ItemModelBuilder model) {
        ResourceLocation loc = model.name;
        return this.packOutput.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
            .resolve(loc.getNamespace())
            .resolve("models")
            .resolve(loc.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "Item models for " + register.modId;
    }

    public ResourceLocation itemTexture(Register.Promise<Item, ?> item) {
        return register.rl("item/" + item.resourcePath());
    }

    public ItemModelBuilder handheld(Register.Promise<Item, ?> item) {
        return handheld(item, itemTexture(item));
    }

    public ItemModelBuilder handheld(Register.Promise<Item, ?> item, ResourceLocation texture) {
        return withExistingParent(item.resourcePath(), "item/handheld").texture("layer0", texture);
    }

    public ItemModelBuilder generated(Register.Promise<Item, ?> item) {
        return handheld(item, itemTexture(item));
    }

    public ItemModelBuilder generated(Register.Promise<Item, ?> item, ResourceLocation texture) {
        return withExistingParent(item.resourcePath(), "item/generated").texture("layer0", texture);
    }

    public ItemModelBuilder withExistingParent(String name, String mcLoc) {
        return withExistingParent(name, mcLoc(mcLoc));
    }

    public ResourceLocation mcLoc(String mcLoc) {
        return new ResourceLocation("minecraft", mcLoc);
    }

    public ItemModelBuilder withExistingParent(String name, ResourceLocation loc) {
        return model(name).parent(loc);
    }

    public ItemModelBuilder model(String path) {
        ResourceLocation outputLoc = extendWithFolder(path.contains(":") ? new ResourceLocation(path) : register.rl(path));

        return generatedModels.computeIfAbsent(outputLoc, ItemModelBuilder::of);
    }

    private ResourceLocation extendWithFolder(ResourceLocation rl) {
        if (rl.getPath().contains("/")) {
            return rl;
        }
        return new ResourceLocation(rl.getNamespace(), "item/" + rl.getPath());
    }
}
