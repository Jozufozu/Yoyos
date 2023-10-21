package com.jozufozu.yoyos.core;

import com.jozufozu.yoyos.core.network.YoyoTracker;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;

public class YoyoItem extends TieredItem {
    public YoyoItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            if (hasThrownYoyo(player, hand)) {
                retractYoyo(player, hand);
            } else {
                throwYoyo(level, player, hand);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    private static void retractYoyo(Player player, InteractionHand hand) {
        YoyoTracker yoyoTracker = YoyoTracker.on(player);

        var yoyo = yoyoTracker.getYoyoInHand(hand);

        if (yoyo == null) {
            return;
        }

        yoyo.sendRetract();
    }

    private static boolean hasThrownYoyo(Player player, InteractionHand hand) {
        return YoyoTracker.on(player)
            .hasYoyo(hand);
    }

    private static void throwYoyo(Level level, Player player, InteractionHand hand) {
        var yoyo = new Yoyo(level);
        yoyo.onThrow(player, hand);

        level.addFreshEntity(yoyo);

        YoyoTracker.on(player)
            .setYoyoInHand(hand, yoyo);
    }
}
