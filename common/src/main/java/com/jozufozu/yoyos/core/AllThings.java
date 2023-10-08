package com.jozufozu.yoyos.core;

import com.jozufozu.yoyos.Constants;
import com.jozufozu.yoyos.infrastructure.register.EntityTypeEntry;
import com.jozufozu.yoyos.infrastructure.register.ItemEntry;
import com.jozufozu.yoyos.infrastructure.register.Register;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Tiers;

public class AllThings {
    public static Register REGISTER = new Register(Constants.MOD_ID);

    public static ItemEntry<YoyoItem> DIAMOND_YOYO = REGISTER.item("diamond_yoyo", p -> new YoyoItem(Tiers.DIAMOND, p))
        .properties(p -> p.stacksTo(1))
        .lang("Diamond Yoyo")
        .model(p -> p.parentModel(p.mcLoc("item/handheld")))
        .register();

    public static EntityTypeEntry<Yoyo> YOYO_ENTITY_TYPE = REGISTER.<Yoyo>entityType("yoyo", Yoyo::new, MobCategory.MISC)
        .and(b -> b.sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10))
        .register();
}
