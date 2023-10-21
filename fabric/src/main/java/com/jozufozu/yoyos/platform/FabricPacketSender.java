package com.jozufozu.yoyos.platform;

import com.jozufozu.yoyos.infrastructure.register.packet.PacketBehavior;
import com.jozufozu.yoyos.platform.services.PacketSender;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class FabricPacketSender implements PacketSender {
    @Override
    public <T> void sendToAllTracking(Entity entity, PacketBehavior<T> packetBehavior, T msg) {
        var buf = PacketByteBufs.create();

        packetBehavior.write(msg, buf);

        for (ServerPlayer serverPlayer : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(serverPlayer, packetBehavior.name(), buf);
        }

        if (entity instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, packetBehavior.name(), buf);
        }
    }
}