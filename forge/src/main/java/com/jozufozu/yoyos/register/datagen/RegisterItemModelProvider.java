package com.jozufozu.yoyos.register.datagen;

import com.jozufozu.yoyos.infrastructure.register.Register;
import com.jozufozu.yoyos.infrastructure.register.data.DataGen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class RegisterItemModelProvider extends ItemModelProvider {
    private final Register register;

    public RegisterItemModelProvider(Register register, PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
        this.register = register;
    }

    @Override
    protected void registerModels() {
        for (DataGen<Item, ?> holder : register.dataGen) {
            holder.applyModelFunction(new WrappedItemModelBuilder(basicItem(holder.get())));
        }
    }
}
