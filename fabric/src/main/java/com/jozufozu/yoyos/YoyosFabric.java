package com.jozufozu.yoyos;

import com.jozufozu.yoyos.core.AllThings;
import com.jozufozu.yoyos.infrastructure.register.packet.PacketBehavior;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerPacketListener;
import net.minecraft.server.MinecraftServer;

public class YoyosFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        Yoyos.init();

        AllThings.REGISTER._register(Registries.ITEM, (rl, item) -> {
            Registry.register(BuiltInRegistries.ITEM, rl, item);
        });

        AllThings.REGISTER._register(Registries.ENTITY_TYPE, (rl, entityType) -> {
            Registry.register(BuiltInRegistries.ENTITY_TYPE, rl, entityType);
        });

        AllThings.REGISTER._registerPackets((resourceLocation, packetBehavior) -> {
            ServerPlayNetworking.registerGlobalReceiver(resourceLocation, (server, player, handler, buf, responseSender) -> {
                handleServerPacket(server, handler, buf, responseSender, packetBehavior);
            });
        });
    }

    private static <T> void handleServerPacket(MinecraftServer server, ServerPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender, PacketBehavior<T> packetBehavior) {
        var msg = packetBehavior.reconstruct(buf);

        server.execute(() -> packetBehavior.handleServer(msg));
    }
}
