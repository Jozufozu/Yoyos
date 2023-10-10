package com.jozufozu.yoyos.platform;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullSupplier;
import com.jozufozu.yoyos.platform.services.IPlatformHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public void runOnClient(NotNullSupplier<Runnable> run) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            run.get().run();
        }
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

}
