package com.jozufozu.yoyos.core;

import com.jozufozu.yoyos.Constants;
import com.jozufozu.yoyos.core.network.RetractYoyoPacket;
import com.jozufozu.yoyos.core.network.YoyoTracker;
import com.jozufozu.yoyos.infrastructure.register.*;
import com.jozufozu.yoyos.infrastructure.register.packet.PacketEntry;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

public class AllThings {
    public static Register REGISTER = new Register(Constants.MOD_ID);

    public static ItemBuilder<YoyoItem> yoyo(String name, Tier tier) {
        return REGISTER.item(name, p -> new YoyoItem(tier, p))
            .model(p -> p.parentModel(p.mcLoc("item/handheld")));
    }

    public static ItemEntry<YoyoItem> WOODEN_YOYO = yoyo("wooden_yoyo", Tiers.WOOD).register();
    public static ItemEntry<YoyoItem> STONE_YOYO = yoyo("stone_yoyo", Tiers.STONE).register();
    public static ItemEntry<YoyoItem> IRON_YOYO = yoyo("iron_yoyo", Tiers.IRON).register();
    public static ItemEntry<YoyoItem> DIAMOND_YOYO = yoyo("diamond_yoyo", Tiers.DIAMOND).register();
    public static ItemEntry<YoyoItem> GOLDEN_YOYO = yoyo("golden_yoyo", Tiers.GOLD).register();
    public static ItemEntry<YoyoItem> NETHERITE_YOYO = yoyo("netherite_yoyo", Tiers.NETHERITE)
        .properties(Item.Properties::fireResistant)
        .register();

    public static EntityEntry<Yoyo> YOYO_ENTITY_TYPE = REGISTER.<Yoyo>entity("yoyo", Yoyo::new, MobCategory.MISC)
        .properties(b -> b.sized(0.25F, 0.25F)
            .clientTrackingRange(8)
            .updateInterval(1))
        .renderer(() -> () -> YoyoRenderer::new)
        .register();

    public static DamageTypeEntry YOYO_DAMAGE_TYPE = REGISTER.damageType("yoyo")
        .register();

    public static PacketEntry<RetractYoyoPacket> RETRACT_PACKET = REGISTER.packet("retract", RetractYoyoPacket.class, RetractYoyoPacket::new)
        .encoder(RetractYoyoPacket::write)
        .onClient(() -> RetractYoyoPacket::onClient)
        .register();

    public static PacketEntry<YoyoTracker.Packet> TRACK_PACKET = REGISTER.packet("track", YoyoTracker.Packet.class, YoyoTracker.Packet::new)
        .encoder(YoyoTracker.Packet::write)
        .onClient(() -> YoyoTracker.Packet::onClient)
        .register();

    public static void init() {
        // no-op, call this to initialize static fields.
    }
}
