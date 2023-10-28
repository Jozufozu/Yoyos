package com.jozufozu.yoyos.infrastructure.register;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;

public class DamageTypeBuilder extends AbstractBuilder<DamageType, DamageType, DamageTypeBuilder> {

    private String msgId;
    private float exhaustion;
    private DamageScaling scaling = DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER;
    private DamageEffects effects = DamageEffects.HURT;

    private DeathMessageType deathMessageType = DeathMessageType.DEFAULT;
    public DamageTypeBuilder(ResourceLocation name, RegistrationCallback<DamageType, DamageType> registrationCallback, String msgId) {
        super(name, registrationCallback, Registries.DAMAGE_TYPE);
        this.msgId = msgId;
    }

    public DamageTypeBuilder msgId(String msgId) {
        this.msgId = msgId;
        return this;
    }

    public DamageTypeBuilder exhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
        return this;
    }

    public DamageTypeBuilder scaling(DamageScaling scaling) {
        this.scaling = scaling;
        return this;
    }

    public DamageTypeBuilder effects(DamageEffects effects) {
        this.effects = effects;
        return this;
    }

    public DamageTypeBuilder deathMessageType(DeathMessageType deathMessageType) {
        this.deathMessageType = deathMessageType;
        return this;
    }

    @Override
    protected DamageTypeEntry wrap(Register.Promise<DamageType, DamageType> promise) {
        return new DamageTypeEntry(promise);
    }

    @Override
    public DamageTypeEntry register() {
        return ((DamageTypeEntry) super.register());
    }

    @Override
    protected DamageType create() {
        return new DamageType(msgId, scaling, exhaustion, effects, deathMessageType);
    }
}
