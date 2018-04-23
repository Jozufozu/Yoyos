package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.Yoyos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class EntityStickyYoyo extends EntityYoyo
{
    public EntityStickyYoyo(World world)
    {
        super(world);
    }

    public EntityStickyYoyo(World world, EntityPlayer player)
    {
        super(world, player);
    }

    private boolean stuck = false;
    private int stuckSince = 0;

    @Override
    public void onUpdate()
    {
        if (!world.isRemote)
        {
            setFlag(6, isGlowing());
        }

        onEntityUpdate();

        if (thrower != null && !thrower.isDead)
        {
            IYoyo yoyo = checkThrowerAndGetStats();

            if (yoyo == null) return;

            if (thrower.isSneaking() && cordLength > 0.5) cordLength -= 0.1F;

            if (!world.getCollisionBoxes(this, getEntityBoundingBox().grow(0.1)).isEmpty() && !isRetracting())
            {
                motionX = 0;
                motionY = 0;
                motionZ = 0;

                if (!stuck)
                {
                    stuckSince = ticksExisted;
                    world.playSound(null, posX, posY, posZ, Yoyos.YOYO_STICK, SoundCategory.PLAYERS, 0.7f, 3.0f);
                }
                stuck = true;
            }
            else
            {
                if (duration >= 0 && ticksExisted >= duration) forceRetract();
                updatePosition();

                stuck = false;
            }

            if (!world.isRemote)
            {
                doEntityCollisions(yoyo);

                if (gardening)
                    garden(yoyo);
            }

            handleSwing();
            if (collecting)
                updateCapturedDrops();
        }
        else setDead();
    }

    @Override
    public float getRotation(int age, float partialTicks)
    {
        return stuck ? super.getRotation(stuckSince, 0) : super.getRotation(age - stuckSince, partialTicks);
    }
}
