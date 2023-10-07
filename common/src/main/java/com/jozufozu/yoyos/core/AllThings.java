package com.jozufozu.yoyos.core;

import com.jozufozu.yoyos.Constants;
import com.jozufozu.yoyos.infrastructure.register.ItemEntry;
import com.jozufozu.yoyos.infrastructure.register.Register;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Tiers;

public class AllThings {
    public static Register REGISTER = new Register(Constants.MOD_ID);

    public static ItemEntry<YoyoItem> DIAMOND_YOYO = REGISTER.item("diamond_yoyo", p -> new YoyoItem(Tiers.DIAMOND, p))
        .properties(p -> p.stacksTo(1))
        .lang("Diamond Yoyo")
        .register();

    public static EntityType<Yoyo> YOYO_ENTITY_TYPE = null;
//    public static EntityType<Yoyo> YOYO_ENTITY_TYPE = EntityType.Builder.<Yoyo>of(Yoyo::new, MobCategory.MISC)
//            .sized(0.25F, 0.25F)
//            .clientTrackingRange(4)
//            .updateInterval(10)
//            .build("yoyo");
}
