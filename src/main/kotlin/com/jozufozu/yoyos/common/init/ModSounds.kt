package com.jozufozu.yoyos.common.init

import com.jozufozu.yoyos.Yoyos
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.registries.ForgeRegistries

object ModSounds {
    val yoyoThrow: SoundEvent by name("entity.yoyo.throw")
    val yoyoStick: SoundEvent by name("entity.yoyo.stick")

    fun name(name: String) = registryName(ForgeRegistries.SOUND_EVENTS, name)

    fun registerSounds(event: RegistryEvent.Register<SoundEvent>) {
        event.registry.register(SoundEvent(ResourceLocation(Yoyos.MODID, "entity.yoyo.throw")).setRegistryName(Yoyos.MODID, "entity.yoyo.throw"))
        event.registry.register(SoundEvent(ResourceLocation(Yoyos.MODID, "entity.yoyo.stick")).setRegistryName(Yoyos.MODID, "entity.yoyo.stick"))
    }
}