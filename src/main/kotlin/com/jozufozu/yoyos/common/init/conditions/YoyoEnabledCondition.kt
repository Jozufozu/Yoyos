package com.jozufozu.yoyos.common.init.conditions

import com.google.gson.JsonObject
import com.jozufozu.yoyos.Yoyos
import com.jozufozu.yoyos.common.YoyosConfig
import net.minecraft.util.JSONUtils
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.conditions.ICondition
import net.minecraftforge.common.crafting.conditions.IConditionSerializer

class YoyoEnabledCondition(val yoyoName: String) : ICondition {
    override fun getID() = Serializer.name

    override fun test(): Boolean {
        return when (yoyoName) {
            "wooden_yoyo" -> YoyosConfig.vanillaYoyos.woodenYoyo
            "stone_yoyo" -> YoyosConfig.vanillaYoyos.stoneYoyo
            "iron_yoyo" -> YoyosConfig.vanillaYoyos.ironYoyo
            "gold_yoyo" -> YoyosConfig.vanillaYoyos.goldYoyo
            "diamond_yoyo" -> YoyosConfig.vanillaYoyos.diamondYoyo
            "shear_yoyo" -> YoyosConfig.vanillaYoyos.shearYoyo
            "sticky_yoyo" -> YoyosConfig.vanillaYoyos.stickyYoyo
            "hoe_yoyo" -> YoyosConfig.vanillaYoyos.hoeYoyo
            else -> return false
        }.enabled.get()
    }

    override fun toString() = "yoyo_enabled(\"$yoyoName\")"

    object Serializer : IConditionSerializer<YoyoEnabledCondition> {
        val name = ResourceLocation(Yoyos.MODID, "yoyo_enabled")

        override fun getID() = name

        override fun write(json: JsonObject, value: YoyoEnabledCondition) {
            json.addProperty("yoyo", value.yoyoName)
        }

        override fun read(json: JsonObject) = YoyoEnabledCondition(JSONUtils.getString(json, "yoyo"))
    }
}
