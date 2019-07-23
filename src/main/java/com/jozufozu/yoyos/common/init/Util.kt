package com.jozufozu.yoyos.common.init

import com.jozufozu.yoyos.Yoyos
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry

fun <T: IForgeRegistryEntry<T>> registryName(registry: IForgeRegistry<T>, name: String) = registryName(registry, ResourceLocation(Yoyos.MODID, name))

fun <T: IForgeRegistryEntry<T>> registryName(registry: IForgeRegistry<T>, name: ResourceLocation) = lazy {
    registry.getValue(name)?: throw Exception("$name could not be found in ${registry.registryName}")
}