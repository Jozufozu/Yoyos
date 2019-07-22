package com.jozufozu.yoyos;

import com.jozufozu.yoyos.common.EnchantmentCollecting;
import com.jozufozu.yoyos.common.StickyYoyoEntity;
import com.jozufozu.yoyos.common.YoyoEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;

import static com.jozufozu.yoyos.Yoyos.MODID;

public class RegistryEvents
{
    public static void registerEnchantment(RegistryEvent.Register<Enchantment> event)
    {
        Yoyos.YOYOS_TAB.setRelevantEnchantmentTypes(Yoyos.Enchantments.YOYO_ENCHANTMENT_TYPE);

        event.getRegistry().register(new EnchantmentCollecting());
    }

    public static void registerSounds(RegistryEvent.Register<SoundEvent> event)
    {
        event.getRegistry().register(new SoundEvent(new ResourceLocation(MODID, "entity.yoyo.throw")).setRegistryName(MODID, "entity.yoyo.throw"));
        event.getRegistry().register(new SoundEvent(new ResourceLocation(MODID, "entity.yoyo.stick")).setRegistryName(MODID, "entity.yoyo.stick"));
        event.getRegistry().register(new SoundEvent(new ResourceLocation(MODID, "entity.yoyo.chase")).setRegistryName(MODID, "entity.yoyo.chase"));
    }

    public static void registerEntityTypes(RegistryEvent.Register<EntityType<?>> event)
    {
        event.getRegistry().register(EntityType.Builder.<YoyoEntity>create(YoyoEntity::new, EntityClassification.MISC)
                                             .disableSummoning()
                                             .size(0.25f, 0.25f)
                                             .setTrackingRange(64)
                                             .setUpdateInterval(2)
                                             .setShouldReceiveVelocityUpdates(true)
                                             .setCustomClientFactory((spawnEntity, world) -> Yoyos.EntityTypes.YOYO.create(world))
                                             .build("yoyo")
                                             .setRegistryName("yoyo"));
        event.getRegistry().register(EntityType.Builder.<StickyYoyoEntity>create(StickyYoyoEntity::new, EntityClassification.MISC)
                                             .disableSummoning()
                                             .size(0.25f, 0.25f)
                                             .setTrackingRange(64)
                                             .setUpdateInterval(2)
                                             .setShouldReceiveVelocityUpdates(true)
                                             .setCustomClientFactory((spawnEntity, world) -> Yoyos.EntityTypes.STICKY_YOYO.create(world))
                                             .build("sticky_yoyo")
                                             .setRegistryName("sticky_yoyo"));

    }
}
