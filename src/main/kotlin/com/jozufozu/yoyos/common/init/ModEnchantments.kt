package com.jozufozu.yoyos.common.init

import com.jozufozu.yoyos.common.EnchantmentCollecting
import com.jozufozu.yoyos.common.ItemYoyo
import com.jozufozu.yoyos.common.YoyosConfig
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentType
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.registries.ForgeRegistries

object ModEnchantments {
    val COLLECTING: Enchantment by name("collecting")
    val YOYO_ENCHANTMENT_TYPE: EnchantmentType = EnchantmentType.create("yoyo") { item -> item is ItemYoyo }

    private fun name(name: String) = registryName(ForgeRegistries.ENCHANTMENTS, name)

    fun registerEnchantment(event: RegistryEvent.Register<Enchantment>) {
        if (YoyosConfig.vanillaYoyos.collectingEnabled.get()) {
            ModItems.YOYOS_TAB.setRelevantEnchantmentTypes(YOYO_ENCHANTMENT_TYPE)

            event.registry.register(EnchantmentCollecting())
        }
    }
}