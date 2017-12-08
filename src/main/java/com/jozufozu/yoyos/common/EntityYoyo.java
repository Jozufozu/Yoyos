package com.jozufozu.yoyos.common;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.registry.IThrowableEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class EntityYoyo extends Entity implements IThrowableEntity
{
    
    public static final HashMap<Entity, EntityYoyo> CASTERS = new HashMap<>();
    
    protected static final Predicate<Entity> CAN_DAMAGE = entity -> entity instanceof EntityLiving || entity instanceof EntityPlayer;
    protected static final int MAX_RETRACT_TIME = 40;
    
    protected EntityPlayer thrower;
    protected ItemStack yoyoStack = ItemStack.EMPTY;
    protected EnumHand hand;
    
    protected float weight;
    protected float cordLength;
    protected int maxCool;
    protected int duration;
    protected boolean gardening;
    
    protected boolean isRetracting = false;
    protected boolean canCancelRetract = true;
    
    protected int lastSlot = -1;
    
    //counters
    protected int retractionTimeout = 0;
    protected int attackCool;
    
    protected boolean shouldGetStats = true;
    
    @Override
    public void setThrower(Entity entity)
    {
        if (entity instanceof EntityPlayer && !(entity instanceof FakePlayer))
        {
            thrower = ((EntityPlayer) entity);
            CASTERS.put(entity, this);
        }
    }
    
    @Override
    public Entity getThrower()
    {
        return thrower;
    }
    
    public ItemStack getYoyoStack()
    {
        return yoyoStack;
    }
    
    public float getWeight()
    {
        return weight;
    }
    
    public float getCordLength()
    {
        return cordLength;
    }
    
    public int getDuration()
    {
        return duration;
    }
    
    public EntityYoyo(World world)
    {
        super(world);
        ignoreFrustumCheck = true;
        width = 0.25F;
        height = 0.25F;
    }
    
    public EntityYoyo(World world, EntityPlayer player)
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
    }
    
    @Override
    protected void entityInit()
    {
        setNoGravity(true);
    }
    
    public void setRetracting(boolean retracting)
    {
        if (canCancelRetract || !isRetracting) isRetracting = retracting;
    }
    
    public boolean isRetracting()
    {
        return isRetracting;
    }
    
    @Override
    public void onUpdate()
    {
        super.onUpdate();
        
        if (thrower != null && !thrower.isDead)
        {
            IYoyo yoyo = checkThrowerAndGetStats();
            
            if (yoyo == null) return;
            
            if (duration != -1 && ticksExisted >= duration) forceRetract();
            
            //handle position
            updatePosition();
            
            if (ModConfig.yoyoSwing)
                handleSwing();
            
            if (!world.isRemote)
            {
                if (gardening)
                    garden(yoyo);
                
                doEntityCollisions(yoyo);
            }
        }
        else setDead();
    }
    
    @Nullable
    protected IYoyo checkThrowerAndGetStats()
    {
        yoyoStack = thrower.getHeldItemMainhand();
    
        if (yoyoStack == ItemStack.EMPTY || !(yoyoStack.getItem() instanceof IYoyo))
        {
            yoyoStack = thrower.getHeldItemOffhand();
        
            if (yoyoStack != ItemStack.EMPTY) hand = EnumHand.OFF_HAND;
        }
        else hand = EnumHand.MAIN_HAND;
    
        int currentSlot = hand == EnumHand.MAIN_HAND ? thrower.inventory.currentItem : -2;
    
        if (!CASTERS.containsKey(thrower) || yoyoStack == null || (lastSlot != -1 && lastSlot != currentSlot) || yoyoStack.getMaxDamage() - yoyoStack.getItemDamage() <= 0 || !(yoyoStack.getItem() instanceof IYoyo))
        {
            setDead();
            return null;
        }
    
        if ((!world.isRemote && CASTERS.get(thrower) != this))
        {
            CASTERS.put(thrower, this);
        }
    
        IYoyo yoyo = (IYoyo) yoyoStack.getItem();
    
        if (shouldGetStats)
        {
            gardening = yoyo.gardening(yoyoStack);
            maxCool = yoyo.getAttackSpeed(yoyoStack);
            duration = yoyo.getDuration(yoyoStack);
            cordLength = yoyo.getLength(yoyoStack);
            weight = yoyo.getWeight(yoyoStack);
        
            shouldGetStats = false;
        }
    
        lastSlot = currentSlot;
        
        return yoyo;
    }
    
    public void updatePosition()
    {
        Vec3d eyePos = new Vec3d(thrower.posX, thrower.posY + thrower.eyeHeight, +thrower.posZ);
        Vec3d lookVec = thrower.getLookVec();
    
        Vec3d target = new Vec3d(eyePos.x + lookVec.x * cordLength, eyePos.y + lookVec.y * cordLength, eyePos.z + lookVec.z * cordLength);
    
        if (isRetracting())
        {
            if (retractionTimeout++ >= MAX_RETRACT_TIME)
            {
                setDead();
                return;
            }
            target = getPlayerHandPos(1);
        }
        else
        {
            retractionTimeout = 0;
            RayTraceResult rayTraceResult = getTargetLook(eyePos, target);
        
            if (rayTraceResult != null) target = rayTraceResult.hitVec;
        }
    
        Vec3d motionVec = target.subtract(posX, posY + height / 2, posZ).scale(Math.min(1 / weight, 1.1));
    
        if (inWater) motionVec = motionVec.scale(0.3);
    
        motionX = motionVec.x;
        motionY = motionVec.y;
        motionZ = motionVec.z;
    
        move(MoverType.SELF, motionX, motionY, motionZ);
        onGround = true; //TODO: This is the only way I've found to get the yoyo to throw out smoothly
    }
    
    public void handleSwing()
    {
        Vec3d thisPos = getPositionVector();
        Vec3d throwerPos = new Vec3d(thrower.posX, thrower.posY + thrower.height / 2, thrower.posZ);
    
        double distance = thisPos.distanceTo(throwerPos);
    
        if (distance > cordLength + 1)
        {
            Vec3d dif = getPositionVector().subtract(thrower.posX, thrower.posY + thrower.height / 2, thrower.posZ).scale(0.01 * (distance - cordLength - 1));
            thrower.addVelocity(dif.x, dif.y, dif.z);
            thrower.fallDistance = 0;
            if (isRetracting) setDead();
        }
    }
    
    public void doEntityCollisions(IYoyo yoyo)
    {
        boolean hit = false;
        for (Entity entity : world.getEntitiesInAABBexcluding(this, getEntityBoundingBox().grow(0.4), CAN_DAMAGE))
        {
            if (entity != thrower &&  !entity.getUniqueID().equals(thrower.getLeftShoulderEntity().getUniqueId("UUID")))
            {
                if (gardening && entity instanceof EntityLivingBase && entity instanceof IShearable)
                {
                    shearEntity(yoyo, entity);
                }
                else if (attackCool >= maxCool)
                {
                    yoyo.attack(entity, yoyoStack, thrower, this);
                    hit = true;
                }
            }
            else if (isRetracting())
            {
                setDead();
            }
        }
    
        ++attackCool;
        if (hit) attackCool = 0;
    }
    
    public void shearEntity(IYoyo yoyo, Entity entity)
    {
        IShearable shearable = (IShearable) entity;
        BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
    
        if (shearable.isShearable(yoyoStack, world, pos))
        {
            List<ItemStack> drops = shearable.onSheared(yoyoStack, world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, yoyoStack));
        
            for (ItemStack stack : drops)
            {
                EntityItem ent = entity.entityDropItem(stack, 1.0F);
            
                if (ent == null) continue;
            
                ent.motionY += rand.nextFloat() * 0.05F;
                ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
            }
        
            if (!thrower.isCreative()) yoyo.damageItem(yoyoStack, thrower);
        }
    }
    
    public void garden(IYoyo yoyo)
    {
        BlockPos pos = getPosition();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IShearable && !block.isLeaves(state, world, pos))
        {
            IShearable shearable = (IShearable) block;
            if (shearable.isShearable(yoyoStack, world, pos))
            {
                List<ItemStack> drops = shearable.onSheared(yoyoStack, world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, yoyoStack));
            
                for (ItemStack stack : drops)
                {
                    float f = 0.7F;
                    double d = (double) (rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    double d1 = (double) (rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    double d2 = (double) (rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
                    EntityItem entityitem = new EntityItem(world, (double) pos.getX() + d, (double) pos.getY() + d1, (double) pos.getZ() + d2, stack);
                    entityitem.setDefaultPickupDelay();
                    thrower.world.spawnEntity(entityitem);
                }
            
                if (!thrower.isCreative()) yoyo.damageItem(yoyoStack, thrower);
            
                thrower.addStat(StatList.getBlockStats(block));
            
                world.playSound(null, pos, block.getSoundType(state, world, pos, this).getBreakSound(), SoundCategory.BLOCKS, 1, 1);
                block.removedByPlayer(state, world, pos, thrower, true);
                world.playEvent(2001, pos, Block.getStateId(state));
            }
        }
        else if (block != Blocks.AIR && block instanceof BlockBush)
        {
            block.dropBlockAsItemWithChance(world, pos, state, 1.0F, 0);
            if (!thrower.isCreative()) yoyo.damageItem(yoyoStack, thrower);
        
            world.playSound(null, pos, block.getSoundType(state, world, pos, this).getBreakSound(), SoundCategory.BLOCKS, 1, 1);
            world.setBlockToAir(pos);
        }
    }
    
    @Override
    public void setDead()
    {
        super.setDead();
        CASTERS.remove(thrower, this);
    }
    
    @Override
    protected void readEntityFromNBT(@Nonnull NBTTagCompound compound)
    {
    }
    
    @Override
    protected void writeEntityToNBT(@Nonnull NBTTagCompound compound)
    {
    }
    
    public Vec3d getPlayerHandPos(float partialTicks) {
        if (this.thrower == null)
        {
            return new Vec3d(this.posX, this.posY, this.posZ);
        }
        
        float yaw = this.thrower.rotationYaw;
        float pitch = this.thrower.rotationPitch;
        
        double posX = this.thrower.posX;
        double posY = this.thrower.posY;
        double posZ = this.thrower.posZ;
        
        if (partialTicks != 1) {
            yaw = (float) (interpolateValue(this.thrower.prevRotationYaw, this.thrower.rotationYaw, partialTicks));
            pitch = (float) (interpolateValue(this.thrower.prevRotationPitch, this.thrower.rotationPitch, partialTicks));
            
            posX = interpolateValue(this.thrower.prevPosX, this.thrower.posX, (double) partialTicks);
            posY = interpolateValue(this.thrower.prevPosY, this.thrower.posY, (double) partialTicks);
            posZ = interpolateValue(this.thrower.prevPosZ, this.thrower.posZ, (double) partialTicks);
        }
        
        double throwerLookOffsetX = Math.cos(yaw * 0.01745329238474369D);
        double throwerLookOffsetZ = Math.sin(yaw * 0.01745329238474369D);
        double throwerLookOffsetY = Math.sin(pitch * 0.01745329238474369D);
        double throwerLookWidth = Math.cos(pitch * 0.01745329238474369D);
        
        float side = this.hand == EnumHand.MAIN_HAND || this.hand == null ? 1 : -1;
        
        return new Vec3d(   posX - throwerLookOffsetX * side * 0.4D - throwerLookOffsetZ * 0.5D * throwerLookWidth,
                posY + this.thrower.eyeHeight * 0.7D - throwerLookOffsetY * 0.5D - 0.25D,
                posZ - throwerLookOffsetZ * side * 0.4D + throwerLookOffsetX * 0.5D * throwerLookWidth);
    }
    
    public static double interpolateValue(double start, double end, double pct)
    {
        return start + (end - start) * pct;
    }
    
    protected void forceRetract()
    {
        canCancelRetract = false;
        isRetracting = true;
    }
    
    //Enter if ye dare
    
    @Nullable
    public RayTraceResult getTargetLook(Vec3d from, Vec3d to)
    {
        double distance = from.distanceTo(to);
        RayTraceResult objectMouseOver = rayTraceBlocks(world, from, to);
        boolean flag = false;
        double d1 = distance;
        
        if (distance > 3.0D)
        {
            flag = true;
        }
        
        if (objectMouseOver != null)
        {
            d1 = objectMouseOver.hitVec.distanceTo(from);
        }
        
        Vec3d vec3d1 = thrower.getLook(1);
        Entity pointedEntity = null;
        Vec3d vec3d3 = null;
        AxisAlignedBB expanded = thrower.getEntityBoundingBox().expand(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance).expand(1.0D, 1.0D, 1.0D);
        
        List<Entity> list = world.getEntitiesInAABBexcluding(thrower, expanded, Predicates.and(EntitySelectors.NOT_SPECTATING, entity -> entity != null && entity.canBeCollidedWith()));
        double d2 = d1;
    
        for (Entity entity1 : list)
        {
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow((double) entity1.getCollisionBorderSize());
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(from, to);
        
            if (axisalignedbb.contains(from))
            {
                if (d2 >= 0.0D)
                {
                    pointedEntity = entity1;
                    vec3d3 = raytraceresult == null ? from : raytraceresult.hitVec;
                    d2 = 0.0D;
                }
            }
            else if (raytraceresult != null)
            {
                double d3 = from.distanceTo(raytraceresult.hitVec);
            
                if (d3 < d2 || d2 == 0.0D)
                {
                    if (entity1.getLowestRidingEntity() == thrower.getLowestRidingEntity() && !thrower.canRiderInteract())
                    {
                        if (d2 == 0.0D)
                        {
                            pointedEntity = entity1;
                            vec3d3 = raytraceresult.hitVec;
                        }
                    }
                    else
                    {
                        pointedEntity = entity1;
                        vec3d3 = raytraceresult.hitVec;
                        d2 = d3;
                    }
                }
            }
        }
        
        if (pointedEntity != null && flag)
        {
            pointedEntity = null;
            objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS, vec3d3, EnumFacing.UP, new BlockPos(vec3d3));
        }
        
        if (pointedEntity != null && objectMouseOver == null)
        {
            objectMouseOver = new RayTraceResult(pointedEntity, vec3d3);
        }
        
        return objectMouseOver;
    }
    
    @Nullable
    public static RayTraceResult rayTraceBlocks(World world, Vec3d start, Vec3d end)
    {
        if (!Double.isNaN(start.x) && !Double.isNaN(start.y) && !Double.isNaN(start.z))
        {
            if (!Double.isNaN(end.x) && !Double.isNaN(end.y) && !Double.isNaN(end.z))
            {
                int endX = MathHelper.floor(end.x);
                int endY = MathHelper.floor(end.y);
                int endZ = MathHelper.floor(end.z);
                int startX = MathHelper.floor(start.x);
                int startY = MathHelper.floor(start.y);
                int startZ = MathHelper.floor(start.z);
                BlockPos blockpos = new BlockPos(startX, startY, startZ);
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();
                
                if ((!(block instanceof BlockBush) || iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, false))
                {
                    RayTraceResult raytraceresult = iblockstate.collisionRayTrace(world, blockpos, start, end);
                    
                    if (raytraceresult != null)
                    {
                        return raytraceresult;
                    }
                }
                
                RayTraceResult rayTraceResult2 = null;
                int steps = 200;
                
                while (steps-- >= 0)
                {
                    if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z))
                    {
                        return null;
                    }
                    
                    if (startX == endX && startY == endY && startZ == endZ)
                    {
                        return rayTraceResult2;
                    }
                    
                    boolean atMaxX = true;
                    boolean atMaxY = true;
                    boolean atMaxZ = true;
                    double currentX = 999.0D;
                    double currentY = 999.0D;
                    double currentZ = 999.0D;
                    
                    if (endX > startX)
                    {
                        currentX = (double) startX + 1.0D;
                    }
                    else if (endX < startX)
                    {
                        currentX = (double) startX + 0.0D;
                    }
                    else
                    {
                        atMaxX = false;
                    }
                    
                    if (endY > startY)
                    {
                        currentY = (double) startY + 1.0D;
                    }
                    else if (endY < startY)
                    {
                        currentY = (double) startY + 0.0D;
                    }
                    else
                    {
                        atMaxY = false;
                    }
                    
                    if (endZ > startZ)
                    {
                        currentZ = (double) startZ + 1.0D;
                    }
                    else if (endZ < startZ)
                    {
                        currentZ = (double) startZ + 0.0D;
                    }
                    else
                    {
                        atMaxZ = false;
                    }
                    
                    double x = 999.0D;
                    double y = 999.0D;
                    double z = 999.0D;
                    double xDiff = end.x - start.x;
                    double yDiff = end.y - start.y;
                    double zDiff = end.z - start.z;
                    
                    if (atMaxX)
                    {
                        x = (currentX - start.x) / xDiff;
                    }
                    
                    if (atMaxY)
                    {
                        y = (currentY - start.y) / yDiff;
                    }
                    
                    if (atMaxZ)
                    {
                        z = (currentZ - start.z) / zDiff;
                    }
                    
                    if (x == -0.0D)
                    {
                        x = -1.0E-4D;
                    }
                    
                    if (y == -0.0D)
                    {
                        y = -1.0E-4D;
                    }
                    
                    if (z == -0.0D)
                    {
                        z = -1.0E-4D;
                    }
                    
                    EnumFacing enumfacing;
                    
                    if (x < y && x < z)
                    {
                        enumfacing = endX > startX ? EnumFacing.WEST : EnumFacing.EAST;
                        start = new Vec3d(currentX, start.y + yDiff * x, start.z + zDiff * x);
                    }
                    else if (y < z)
                    {
                        enumfacing = endY > startY ? EnumFacing.DOWN : EnumFacing.UP;
                        start = new Vec3d(start.x + xDiff * y, currentY, start.z + zDiff * y);
                    }
                    else
                    {
                        enumfacing = endZ > startZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        start = new Vec3d(start.x + xDiff * z, start.y + yDiff * z, currentZ);
                    }
                    
                    startX = MathHelper.floor(start.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    startY = MathHelper.floor(start.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    startZ = MathHelper.floor(start.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(startX, startY, startZ);
                    IBlockState state = world.getBlockState(blockpos);
                    Block block1 = state.getBlock();
                    
                    if (!(block1 instanceof BlockBush) || state.getMaterial() == Material.PORTAL || state.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB)
                    {
                        if (block1.canCollideCheck(state, false))
                        {
                            RayTraceResult rayTraceResult = state.collisionRayTrace(world, blockpos, start, end);
                            
                            if (rayTraceResult != null)
                            {
                                return rayTraceResult;
                            }
                        }
                        else
                        {
                            rayTraceResult2 = new RayTraceResult(RayTraceResult.Type.MISS, start, enumfacing, blockpos);
                        }
                    }
                    
                }
                
                return rayTraceResult2;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
}
