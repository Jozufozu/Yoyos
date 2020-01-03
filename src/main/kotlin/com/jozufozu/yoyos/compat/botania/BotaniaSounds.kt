package com.jozufozu.yoyos.compat.botania

import com.jozufozu.yoyos.Yoyos
import com.jozufozu.yoyos.common.init.ModSounds
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraftforge.event.RegistryEvent

object BotaniaSounds {
    val chase: SoundEvent by ModSounds.name("yoyo_chase")

    fun registerSounds(event: RegistryEvent.Register<SoundEvent>) {
        event.registry.register(SoundEvent(ResourceLocation(Yoyos.MODID, "entity.yoyo.chase")).setRegistryName(Yoyos.MODID, "entity.yoyo.chase"))
    }
}