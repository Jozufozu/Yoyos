package com.jozufozu.yoyos.core;


import org.jetbrains.annotations.Nullable;

import net.minecraft.world.phys.Vec3;

public interface TargetProvider {
    boolean isExpired();

    @Nullable
    Vec3 target();
}
