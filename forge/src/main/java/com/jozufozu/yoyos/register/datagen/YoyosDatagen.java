package com.jozufozu.yoyos.register.datagen;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.yoyos.core.AllThings;
import com.jozufozu.yoyos.infrastructure.register.data.RegisterDataProvider;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraftforge.data.event.GatherDataEvent;

public class YoyosDatagen {
    private static final String EN_US = "en_us";

    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();

        generator.addProvider(true, getRegisterDataProviderFactory(event, generator));
    }

    @NotNull
    private static DataProvider.Factory<RegisterDataProvider> getRegisterDataProviderFactory(GatherDataEvent event, DataGenerator generator) {
        return output -> new RegisterDataProvider(AllThings.REGISTER, event.getLookupProvider(), generator.getPackOutput());
    }
}
