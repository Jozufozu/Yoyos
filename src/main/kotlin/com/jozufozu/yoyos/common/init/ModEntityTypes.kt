package com.jozufozu.yoyos.common.init

import com.jozufozu.yoyos.Yoyos
import com.jozufozu.yoyos.common.StickyYoyoEntity
import com.jozufozu.yoyos.common.YoyoEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityClassification
import net.minecraft.entity.EntityType
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.registries.ForgeRegistries

object ModEntityTypes {
    val YOYO: EntityType<YoyoEntity> by name("yoyo")
    val STICKY_YOYO: EntityType<StickyYoyoEntity> by name("sticky_yoyo")
    
    private fun <T: Entity> name(name: String) = lazy {
        (ForgeRegistries.ENTITIES.getValue(ResourceLocation(Yoyos.MODID, name)) as? EntityType<T>)?: throw Exception("$name could not be found in ${ForgeRegistries.ENTITIES.registryName}")
    }

    fun registerEntityTypes(event: RegistryEvent.Register<EntityType<*>>) {
        event.registry.register(EntityType.Builder.create(::YoyoEntity, EntityClassification.MISC)
                .disableSummoning()
                .size(0.25f, 0.25f)
                .setTrackingRange(64)
                .setUpdateInterval(1)
                .setShouldReceiveVelocityUpdates(true)
                .setCustomClientFactory { _, world -> YOYO.create(world) }
                .build("yoyo")
                .setRegistryName("yoyo"))
        event.registry.register(EntityType.Builder.create(::StickyYoyoEntity, EntityClassification.MISC)
                .disableSummoning()
                .size(0.25f, 0.25f)
                .setTrackingRange(64)
                .setUpdateInterval(1)
                .setShouldReceiveVelocityUpdates(true)
                .setCustomClientFactory { _, world -> STICKY_YOYO.create(world) }
                .build("sticky_yoyo")
                .setRegistryName("sticky_yoyo"))

    }
}