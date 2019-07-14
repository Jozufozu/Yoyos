package com.jozufozu.yoyos.common.api;

import com.jozufozu.yoyos.common.EntityYoyo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

@FunctionalInterface
public interface YoyoFactory
{
    EntityYoyo create(World world, EntityPlayer player, EnumHand hand);
}
