package com.jozufozu.yoyos;

import com.jozufozu.yoyos.core.AllThings;
import com.jozufozu.yoyos.infrastructure.register.packet.PacketBehavior;
import com.jozufozu.yoyos.register.datagen.YoyosDatagen;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class YoyosForge {

    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(Yoyos.rl("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    private static int NETWORK_ID = 0;
    
    public YoyosForge() {
        Yoyos.init();

        var modEventBus = FMLJavaModLoadingContext.get()
            .getModEventBus();

        modEventBus.addListener(this::register);

        modEventBus.addListener(YoyosDatagen::gatherData);

        AllThings.REGISTER._registerPackets((resourceLocation, packetBehavior) -> registerPacket(packetBehavior));
    }

    private static <T> void registerPacket(PacketBehavior<T> packetBehavior) {
        NETWORK.registerMessage(NETWORK_ID++, packetBehavior.clazz(), packetBehavior.write(), packetBehavior.reconstruct(), (msg, ctx) -> {
            ctx.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> packetBehavior.handleClient(msg));
                DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> packetBehavior.handleServer(msg));
            });

            ctx.get().setPacketHandled(true);
        });
    }

    public void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            AllThings.REGISTER._register(ForgeRegistries.Keys.ITEMS, helper::register);
        });

        event.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
            AllThings.REGISTER._register(ForgeRegistries.Keys.ENTITY_TYPES, helper::register);
        });
    }
}