package com.jozufozu.yoyos.register.datagen;

import com.jozufozu.yoyos.infrastructure.register.data.ModelBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;

public record WrappedItemModelBuilder(ItemModelBuilder inner) implements ModelBuilder {
    // TODO: move this to common
    @Override
    public ModelBuilder parentModel(ResourceLocation resourceLocation) {
        inner.parent(new ModelFile.UncheckedModelFile(resourceLocation));
        return this;
    }
}
