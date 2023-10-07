package com.jozufozu.yoyos.common.init.conditions

import net.minecraftforge.common.crafting.CraftingHelper

object ModConditions {
    fun init() {
        CraftingHelper.register(VanillaEnabledCondition.Serializer)
        CraftingHelper.register(YoyoEnabledCondition.Serializer)
    }
}