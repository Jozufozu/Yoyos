package com.jozufozu.yoyos.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
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
        
        if (this.thrower != null)
        {
            
            this.yoyoStack = this.thrower.getHeldItemMainhand();
            
            if (this.yoyoStack == ItemStack.EMPTY || !(yoyoStack.getItem() instanceof IYoyo))
            {
                this.yoyoStack = this.thrower.getHeldItemOffhand();
                
                if (this.yoyoStack != ItemStack.EMPTY) this.hand = EnumHand.OFF_HAND;
            }
            else this.hand = EnumHand.MAIN_HAND;
            
            int currentSlot = this.hand == EnumHand.MAIN_HAND ? this.thrower.inventory.currentItem : -2;
            
            if (!CASTERS.containsKey(this.thrower) || this.yoyoStack == null || (this.lastSlot != -1 && this.lastSlot != currentSlot) || this.yoyoStack.getMaxDamage() - this.yoyoStack.getItemDamage() <= 0 || !(yoyoStack.getItem() instanceof IYoyo))
            {
                this.setDead();
                return;
            }
            
            if ((!world.isRemote && CASTERS.get(this.thrower) != this))
            {
                CASTERS.put(this.thrower, this);
            }
            
            IYoyo yoyo = (IYoyo) this.yoyoStack.getItem();
            
            if (this.shouldGetStats)
            {
                this.maxCool = yoyo.getAttackSpeed(this.yoyoStack);
                this.duration = yoyo.getDuration(this.yoyoStack);
                this.cordLength = yoyo.getLength(this.yoyoStack);
                this.weight = yoyo.getWeight(this.yoyoStack);
                
                this.shouldGetStats = false;
            }
            
            this.lastSlot = currentSlot;
            
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
                //handle position
                updatePosition();
            }
            
            handleSwing();
        }
        else this.setDead();
    }
}
