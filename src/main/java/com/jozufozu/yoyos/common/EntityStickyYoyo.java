package com.jozufozu.yoyos.common;

import net.minecraft.entity.player.EntityPlayer;
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
    
    @Override
    public void onUpdate()
    {
        if (!this.world.isRemote)
        {
            this.setFlag(6, this.isGlowing());
        }
        
        this.onEntityUpdate();
        
        if (this.thrower != null && !thrower.isDead)
        {
    
            IYoyo yoyo = checkThrowerAndGetStats();
    
            if (yoyo == null) return;
            
            if (duration != -1 && this.ticksExisted >= duration) this.forceRetract();
            
            if (this.thrower.isSneaking() && this.cordLength > 0.5) this.cordLength -= 0.1F;
            
            if (this.isCollided && !this.isRetracting())
            {
                this.motionX = 0;
                this.motionY = 0;
                this.motionZ = 0;
            }
            else
            {
                updatePosition();
            }
            
            if (!world.isRemote)
            {
                doEntityCollisions(yoyo);
            }
            
            handleSwing();
        }
        else this.setDead();
    }
}
