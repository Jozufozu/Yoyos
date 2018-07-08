package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.common.yotools.YoToolData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityArbitraryYoyo extends EntityYoyo
{
    public EntityArbitraryYoyo(World world)
    {
        super(world);
        ignoreFrustumCheck = true;
        width = 0.25F;
        height = 0.25F;
    }

    public EntityArbitraryYoyo(World world, EntityPlayer player)
    {
        this(world);
        thrower = player;

        CASTERS.put(player, this);

        yoyoStack = thrower.getHeldItemMainhand();

        if (yoyoStack == ItemStack.EMPTY)
        {
            yoyoStack = thrower.getHeldItemOffhand();

            if (yoyoStack != ItemStack.EMPTY) hand = EnumHand.OFF_HAND;
        }
        else hand = EnumHand.MAIN_HAND;

        Vec3d handPos = getPlayerHandPos(1);
        setPosition(handPos.x, handPos.y, handPos.z);

        if (!world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty())
            setPosition(player.posX, player.posY + player.height * 0.85, player.posZ);
    }

    @Nullable
    @Override
    protected IYoyo checkAndGetYoyoObject()
    {
        yoyoStack = thrower.getHeldItemMainhand();

        if (yoyoStack == ItemStack.EMPTY || !YoToolData.hasData(yoyoStack))
        {
            yoyoStack = thrower.getHeldItemOffhand();

            if (yoyoStack != ItemStack.EMPTY)
                hand = EnumHand.OFF_HAND;
        }
        else hand = EnumHand.MAIN_HAND;

        int currentSlot = hand == EnumHand.MAIN_HAND ? thrower.inventory.currentItem : -2;

        if (!CASTERS.containsKey(thrower) || !YoToolData.hasData(yoyoStack) || (lastSlot != -1 && lastSlot != currentSlot))
        {
            setDead();
            return null;
        }

        if (yoyoStack.isItemStackDamageable() && yoyoStack.getMaxDamage() < yoyoStack.getItemDamage())
        {
            setDead();
            return null;
        }

        if ((!world.isRemote && CASTERS.get(thrower) != this))
        {
            CASTERS.put(thrower, this);
        }

        IYoyo yoyo = new YoToolData(yoyoStack);

        getStats(yoyo);

        lastSlot = currentSlot;

        return yoyo;
    }
}
