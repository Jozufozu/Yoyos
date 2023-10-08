package com.jozufozu.yoyos.register.datagen;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.yoyos.Constants;
import com.jozufozu.yoyos.core.AllThings;

import net.minecraft.data.DataProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

public class Datagenner {

    private static final String EN_US = "en_us";

    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var efh = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), lang());
        generator.addProvider(event.includeClient(), itemModels(efh));
    }

    @NotNull
    private static DataProvider.Factory<RegisterLangProvider> lang() {
        return output -> new RegisterLangProvider(AllThings.REGISTER, output, Constants.MOD_ID, EN_US);
    }

    @NotNull
    private static DataProvider.Factory<RegisterItemModelProvider> itemModels(ExistingFileHelper efh) {
        return output -> new RegisterItemModelProvider(AllThings.REGISTER, output, Constants.MOD_ID, efh);
    }
}
