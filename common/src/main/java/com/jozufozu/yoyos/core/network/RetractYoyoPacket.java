package com.jozufozu.yoyos.core.network;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

public class RetractYoyoPacket {
    private final boolean isRetracting;
    private final int yoyoId;

    public RetractYoyoPacket(Yoyo entity, boolean isRetracting) {
        yoyoId = entity.getId();
        this.isRetracting = isRetracting;
    }

    public RetractYoyoPacket(FriendlyByteBuf buf) {
        this.yoyoId = buf.readVarInt();
        this.isRetracting = buf.readBoolean();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(yoyoId);
        buf.writeBoolean(isRetracting);
    }

    public void onClient() {
        var mc = Minecraft.getInstance();

        var level = mc.level;

        if (level == null) {
            return;
        }

        if (level.getEntity(yoyoId) instanceof Yoyo yoyo) {
            yoyo.getController()
                .signalRetract();
        }
    }
}
