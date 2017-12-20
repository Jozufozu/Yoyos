package com.jozufozu.yoyos.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
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
            
            if (duration >= 0 && this.ticksExisted >= duration) this.forceRetract();
            
            if (this.thrower.isSneaking() && this.cordLength > 0.5) this.cordLength -= 0.1F;
            
            if (!this.world.getCollisionBoxes(this, this.getEntityBoundingBox().grow(0.1)).isEmpty() && !this.isRetracting())
            {
                if (this.motionX != 0 || this.motionY != 0 || this.motionZ != 0)
                    world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_SLIME_DEATH, SoundCategory.PLAYERS, 0.7f, 3.0f);
                
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
                
                if (this.gardening)
                    garden(yoyo);
            }
            
            handleSwing();
            if (collecting)
                updateCapturedDrops();
        }
        else this.setDead();
    }
}
