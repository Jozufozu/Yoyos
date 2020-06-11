package com.jozufozu.yoyos

import com.electronwill.nightconfig.core.file.CommentedFileConfig
import com.electronwill.nightconfig.core.io.WritingMode
import com.jozufozu.yoyos.client.NetworkHandlers
import com.jozufozu.yoyos.client.YoyoRenderer
import com.jozufozu.yoyos.client.YoyosClient
import com.jozufozu.yoyos.common.YoyoEntity
import com.jozufozu.yoyos.common.YoyosConfig
import com.jozufozu.yoyos.common.init.ModEnchantments
import com.jozufozu.yoyos.common.init.ModEntityTypes
import com.jozufozu.yoyos.common.init.ModItems
import com.jozufozu.yoyos.common.init.ModSounds
import com.jozufozu.yoyos.common.init.conditions.ModConditions
import com.jozufozu.yoyos.network.YoyoNetwork
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.util.SoundEvent
import net.minecraftforge.client.event.RenderHandEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.client.registry.RenderingRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.loading.FMLPaths

@Mod(Yoyos.MODID)
object Yoyos {
    const val MODID = "yoyos"

    init {
        FMLKotlinModLoadingContext.get().modEventBus.addListener<FMLCommonSetupEvent> { this.setup(it) }
        FMLKotlinModLoadingContext.get().modEventBus.addListener<FMLClientSetupEvent> { this.doClientStuff(it) }

        FMLKotlinModLoadingContext.get().modEventBus.addGenericListener<RegistryEvent.Register<Item>, Item>(Item::class.java) { ModItems.onItemsRegistry(it) }
        FMLKotlinModLoadingContext.get().modEventBus.addGenericListener<RegistryEvent.Register<Enchantment>, Enchantment>(Enchantment::class.java) { ModEnchantments.registerEnchantment(it) }
        FMLKotlinModLoadingContext.get().modEventBus.addGenericListener<RegistryEvent.Register<EntityType<*>>, EntityType<*>>(EntityType::class.java) { ModEntityTypes.registerEntityTypes(it) }
        FMLKotlinModLoadingContext.get().modEventBus.addGenericListener<RegistryEvent.Register<SoundEvent>, SoundEvent>(SoundEvent::class.java) { ModSounds.registerSounds(it) }

        MinecraftForge.EVENT_BUS.addListener<LivingDropsEvent> { YoyoEntity.onLivingDrops(it) }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, YoyosConfig.spec, "$MODID.toml")
        FMLKotlinModLoadingContext.get().modEventBus.addListener<ModConfig.ModConfigEvent> { YoyosConfig.onConfig(it) }

        val configData = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve("$MODID.toml"))
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build()

        configData.load()

        YoyosConfig.spec.setConfig(configData)

        ModConditions.init() // Make sure the thing is actually initialized
    }

    private fun setup(event: FMLCommonSetupEvent) {
        YoyoNetwork.initialize()
    }

    private fun doClientStuff(event: FMLClientSetupEvent) {
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.YOYO) { YoyoRenderer(it) }
        RenderingRegistry.registerEntityRenderingHandler(ModEntityTypes.STICKY_YOYO) { YoyoRenderer(it) }

        MinecraftForge.EVENT_BUS.addListener<TickEvent.WorldTickEvent> { NetworkHandlers.onTickWorldTick(it) }
        MinecraftForge.EVENT_BUS.addListener<RenderHandEvent> { YoyosClient.onRenderHand(it) }
    }
}
