package com.jozufozu.yoyos.platform.services;

import com.jozufozu.yoyos.infrastructure.register.packet.PacketBehavior;

import net.minecraft.world.entity.Entity;

public interface PacketSender {
    <T> void sendToAllTracking(Entity entity, PacketBehavior<T> packetBehavior, T msg);
}