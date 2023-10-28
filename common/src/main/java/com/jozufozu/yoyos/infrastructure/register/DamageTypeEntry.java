package com.jozufozu.yoyos.infrastructure.register;

import net.minecraft.world.damagesource.DamageType;

public class DamageTypeEntry extends Entry<DamageType, DamageType> {
    public DamageTypeEntry(Register.Promise<DamageType, DamageType> promise) {
        super(promise);
    }

//    private DamageSource source() {
//        return new DamageSource(get());
//    }
//
//    private DamageSource source(@Nullable Entity $$1) {
//        return new DamageSource(get(), $$1);
//    }
//
//    private DamageSource source(@Nullable Entity $$1, @Nullable Entity $$2) {
//        return new DamageSource(get(), $$1, $$2);
//    }
}
