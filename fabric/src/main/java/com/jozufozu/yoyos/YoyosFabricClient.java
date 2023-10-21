package com.jozufozu.yoyos;

import com.jozufozu.yoyos.core.AllThings;
import com.jozufozu.yoyos.infrastructure.register.packet.PacketBehavior;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class YoyosFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AllThings.REGISTER._registerPackets((resourceLocation, packetBehavior) -> {
            ClientPlayNetworking.registerGlobalReceiver(resourceLocation, (client, handler, buf, responseSender) -> {
                handleClientPacket(client, handler, buf, responseSender, packetBehavior);
            });
        });
    }

    private static <T> void handleClientPacket(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender, PacketBehavior<T> packetBehavior) {
        var msg = packetBehavior.reconstruct(buf);

        client.execute(() -> packetBehavior.handleClient(msg));
    }
}
