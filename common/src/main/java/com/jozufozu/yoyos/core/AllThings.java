package com.jozufozu.yoyos.core;

import com.jozufozu.yoyos.Constants;
import com.jozufozu.yoyos.core.network.RetractYoyoPacket;
import com.jozufozu.yoyos.core.network.YoyoTracker;
import com.jozufozu.yoyos.infrastructure.register.EntityEntry;
import com.jozufozu.yoyos.infrastructure.register.ItemEntry;
import com.jozufozu.yoyos.infrastructure.register.Register;
import com.jozufozu.yoyos.infrastructure.register.packet.PacketEntry;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Tiers;

public class AllThings {
    public static Register REGISTER = new Register(Constants.MOD_ID);

    public static ItemEntry<YoyoItem> DIAMOND_YOYO = REGISTER.item("diamond_yoyo", p -> new YoyoItem(Tiers.DIAMOND, p))
        .properties(p -> p.stacksTo(1))
        .lang("Diamond Yoyo")
        .model(p -> p.parentModel(p.mcLoc("item/handheld")))
        .register();

    public static EntityEntry<Yoyo> YOYO_ENTITY_TYPE = REGISTER.<Yoyo>entity("yoyo", Yoyo::new, MobCategory.MISC)
        .properties(b -> b.sized(0.25F, 0.25F)
            .clientTrackingRange(8)
            .updateInterval(1))
        .renderer(() -> () -> YoyoRenderer::new)
        .register();

    public static PacketEntry<RetractYoyoPacket> RETRACT_PACKET = REGISTER.packet("retract", RetractYoyoPacket.class, RetractYoyoPacket::new)
        .encoder(RetractYoyoPacket::write)
        .onClient(() -> RetractYoyoPacket::onClient)
        .register();

    public static PacketEntry<YoyoTracker.Packet> TRACK_PACKET = REGISTER.packet("track", YoyoTracker.Packet.class, YoyoTracker.Packet::new)
        .encoder(YoyoTracker.Packet::write)
        .onClient(() -> YoyoTracker.Packet::onClient)
        .register();
}
