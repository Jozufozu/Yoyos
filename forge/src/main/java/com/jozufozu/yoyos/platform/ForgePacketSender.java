package com.jozufozu.yoyos.platform;

import com.jozufozu.yoyos.YoyosForge;
import com.jozufozu.yoyos.infrastructure.register.packet.PacketBehavior;
import com.jozufozu.yoyos.platform.services.PacketSender;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;

public class ForgePacketSender implements PacketSender {
    @Override
    public <T> void sendToAllTracking(Entity entity, PacketBehavior<T> packetBehavior, T msg) {
        YoyosForge.NETWORK.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }
}