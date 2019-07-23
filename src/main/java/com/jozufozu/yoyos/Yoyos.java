package com.jozufozu.yoyos;

import com.jozufozu.yoyos.client.NetworkHandlers;
import com.jozufozu.yoyos.client.YoyoRenderer;
import com.jozufozu.yoyos.common.YoyoEntity;
import com.jozufozu.yoyos.common.init.ModEnchantments;
import com.jozufozu.yoyos.common.init.ModEntityTypes;
import com.jozufozu.yoyos.common.init.ModItems;
import com.jozufozu.yoyos.common.init.ModSounds;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Yoyos.MODID)
public class Yoyos
{
    public static final String MODID = "yoyos";
    public static final String NAME = "Yoyos";
    public static final String VERSION = "@VERSION@";
    public static final Logger LOG = LogManager.getLogger(MODID);

    public Yoyos() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, ModItems::onItemsRegistry);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Enchantment.class, ModEnchantments::registerEnchantment);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(EntityType.class, ModEntityTypes::registerEntityTypes);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(SoundEvent.class, ModSounds::registerSounds);

        MinecraftForge.EVENT_BUS.addListener(YoyoEntity::onLivingDrops);
    }

    public void setup(FMLCommonSetupEvent event) {
        YoyoNetwork.initialize();
    }

    public void doClientStuff(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(YoyoEntity.class, YoyoRenderer::new);

        MinecraftForge.EVENT_BUS.addListener(NetworkHandlers::onTickWorldTick);
        MinecraftForge.EVENT_BUS.addListener(NetworkHandlers::onPlayerInteractRightClickItem);
    }
}
