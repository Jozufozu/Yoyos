package com.jozufozu.yoyos.common.conditions;

import com.google.gson.JsonObject;
import com.jozufozu.yoyos.common.ModConfig;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

public class AreYoyosEnabledFactory implements IConditionFactory
{
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json)
    {
        return () -> ModConfig.vanillaYoyos.enable;
    }
}
