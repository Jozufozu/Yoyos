package com.jozufozu.yoyos.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.jozufozu.yoyos.core.LivingEntityExtension;
import com.jozufozu.yoyos.core.YoyoTracker;

import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements LivingEntityExtension {

    // Wrapping everything into this one object seems easier
    // than having to constantly modify the mixin and Extension interface.
    @Unique
    private final YoyoTracker yoyos$yoyoTracker = new YoyoTracker();

    @Override
    @NotNull
    public YoyoTracker yoyos$getYoyoTracker() {
        return yoyos$yoyoTracker;
    }
}
