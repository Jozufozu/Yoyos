package com.jozufozu.yoyos.platform;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;
import com.jozufozu.yoyos.platform.services.IPlatformHelper;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public void runOnClient(NotNullSupplier<Runnable> run) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, run);
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }
}