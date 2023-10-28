package com.jozufozu.yoyos.core.control;

import org.joml.Vector3dc;

import com.jozufozu.yoyos.core.Yoyo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class ParticleSprayBehavior implements YoyoBehavior {
    @Override
    public void onTouchBlock(Yoyo yoyo, YoyoContext ctx, BlockPos touchedPos, Vector3dc touchedExact) {
        if (yoyo.level() instanceof ServerLevel serverLevel) {
            var blockParticleOption = new BlockParticleOption(ParticleTypes.BLOCK, serverLevel.getBlockState(touchedPos));
            serverLevel.sendParticles(blockParticleOption, touchedExact.x(), touchedExact.y(), touchedExact.z(), 1, 0.0D, 0.0D, 0.0D, 0.15F);
        }
    }
}
