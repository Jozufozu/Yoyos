package com.jozufozu.yoyos.common.init

import com.jozufozu.yoyos.Yoyos
import com.jozufozu.yoyos.common.YoyosConfig
import net.minecraft.util.JSONUtils
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.common.crafting.IConditionSerializer
import java.util.function.BooleanSupplier

object ModConditions {
    val vanillaEnabledCondition = condition("vanilla_enabled", IConditionSerializer { BooleanSupplier(YoyosConfig.vanillaYoyos.enabled::get) })
    val yoyoEnabledCondition = condition("yoyo_enabled", IConditionSerializer {
        BooleanSupplier {
            when (JSONUtils.getString(it, "yoyo")) {
                "wooden_yoyo" -> YoyosConfig.vanillaYoyos.woodenYoyo
                "stone_yoyo" -> YoyosConfig.vanillaYoyos.stoneYoyo
                "iron_yoyo" -> YoyosConfig.vanillaYoyos.ironYoyo
                "gold_yoyo" -> YoyosConfig.vanillaYoyos.goldYoyo
                "diamond_yoyo" -> YoyosConfig.vanillaYoyos.diamondYoyo
                "shear_yoyo" -> YoyosConfig.vanillaYoyos.shearYoyo
                "sticky_yoyo" -> YoyosConfig.vanillaYoyos.stickyYoyo
                "hoe_yoyo" -> YoyosConfig.vanillaYoyos.hoeYoyo
                else -> return@BooleanSupplier false
            }.enabled.get()
        }
    })

    private fun condition(name: String, serializer: IConditionSerializer): IConditionSerializer {
        return CraftingHelper.register(ResourceLocation(Yoyos.MODID, name), serializer)
    }

    fun init() {}
}