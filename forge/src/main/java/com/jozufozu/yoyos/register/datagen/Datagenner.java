package com.jozufozu.yoyos.register.datagen;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.yoyos.Constants;
import com.jozufozu.yoyos.core.AllThings;

import net.minecraft.data.DataProvider;
import net.minecraftforge.data.event.GatherDataEvent;

public class Datagenner {

    private static final String EN_US = "en_us";

    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        generator.addProvider(event.includeClient(), langProvider());
    }

    @NotNull
    private static DataProvider.Factory<RegisterLangProvider> langProvider() {
        return output -> new RegisterLangProvider(AllThings.REGISTER, output, Constants.MOD_ID, EN_US);
    }
}
