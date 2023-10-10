package com.jozufozu.yoyos.core;

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
        var stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            var yoyo = new Yoyo(level, player);
            yoyo.onThrow();
            yoyo.setItem(stack);
            level.addFreshEntity(yoyo);
        }

        player.awardStat(Stats.ITEM_USED.get(this));

        return InteractionResultHolder.success(stack);
    }
}
