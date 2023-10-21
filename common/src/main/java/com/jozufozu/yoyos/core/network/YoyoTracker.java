package com.jozufozu.yoyos.core.network;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.yoyos.core.AllThings;
import com.jozufozu.yoyos.core.LivingEntityExtension;
import com.jozufozu.yoyos.core.Yoyo;
import com.jozufozu.yoyos.platform.Services;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Every {@link LivingEntity} has a YoyoTracker mixin'd.
 */
public class YoyoTracker {
    private final LivingEntity parent;

    private Yoyo mainHandYoyo = null;
    private Yoyo offhandYoyo = null;


    public static YoyoTracker on(LivingEntity living) {
        return ((LivingEntityExtension) living).yoyos$getYoyoTracker();
    }

    public YoyoTracker(LivingEntity parent) {
        this.parent = parent;
    }

    public boolean hasYoyo(InteractionHand hand) {
        return getYoyoInHand(hand) != null;
    }

    @Nullable
    public Yoyo getYoyoInHand(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> mainHandYoyo;
            case OFF_HAND -> offhandYoyo;
        };
    }

    public void setYoyoInHand(InteractionHand hand, @Nullable Yoyo yoyo) {
        switch (hand) {
            case MAIN_HAND -> mainHandYoyo = yoyo;
            case OFF_HAND -> offhandYoyo = yoyo;
        }

        if (!parent.level().isClientSide) {
            sync();
        }
    }

    private void sync() {
        Services.NETWORK.sendToAllTracking(parent, AllThings.TRACK_PACKET.get(), new Packet(this));
    }

    private void sync(Packet packet) {
        mainHandYoyo = nullableFromId(parent.level(), packet.mainHandYoyoId);
        offhandYoyo = nullableFromId(parent.level(), packet.offhandYoyoId);
    }

    private static int nullableId(@Nullable Yoyo e) {
        return e == null ? 0 : e.getId();
    }

    @Nullable
    private static Yoyo nullableFromId(Level level, int id) {
        return level.getEntity(id) instanceof Yoyo yoyo ? yoyo : null;
    }

    public static class Packet {
        private final int trackedEntity;
        private final int mainHandYoyoId;
        private final int offhandYoyoId;


        public Packet(FriendlyByteBuf buf) {
            this.trackedEntity = buf.readVarInt();
            this.mainHandYoyoId = buf.readVarInt();
            this.offhandYoyoId = buf.readVarInt();
        }

        private Packet(YoyoTracker tracker) {
            this.trackedEntity = tracker.parent.getId();
            this.mainHandYoyoId = nullableId(tracker.mainHandYoyo);
            this.offhandYoyoId = nullableId(tracker.offhandYoyo);
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(trackedEntity);
            buf.writeVarInt(mainHandYoyoId);
            buf.writeVarInt(offhandYoyoId);
        }

        public void onClient() {
            var level = Minecraft.getInstance().level;

            if (level == null) {
                return;
            }

            if (level.getEntity(trackedEntity) instanceof LivingEntity living) {
                on(living).sync(this);
            }
        }
    }
}
