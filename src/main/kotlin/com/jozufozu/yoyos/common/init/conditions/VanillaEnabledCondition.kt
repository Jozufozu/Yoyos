package com.jozufozu.yoyos.common.init.conditions

import com.google.gson.JsonObject
import com.jozufozu.yoyos.Yoyos
import com.jozufozu.yoyos.common.YoyosConfig
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.conditions.ICondition
import net.minecraftforge.common.crafting.conditions.IConditionSerializer

object VanillaEnabledCondition : ICondition {
    override fun getID() = Serializer.name

    override fun test(): Boolean = YoyosConfig.vanillaYoyos.enabled.get()

    override fun toString() = "vanilla_yoyos_enabled"

    object Serializer : IConditionSerializer<VanillaEnabledCondition> {
        val name = ResourceLocation(Yoyos.MODID, "vanilla_enabled")

        override fun getID() = name

        override fun write(json: JsonObject, value: VanillaEnabledCondition) { }

        override fun read(json: JsonObject) = VanillaEnabledCondition
    }
}