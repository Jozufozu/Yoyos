/*
 * Copyright (c) 2018 Jozsef Augusztiny
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.network.MessageCollectedDrops;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber
public class EntityYoyo extends Entity implements IThrowableEntity
{
    public static final HashMap<Entity, EntityYoyo> CASTERS = new HashMap<>();

    protected static final int MAX_RETRACT_TIME = 40;
    public ArrayList<ItemStack> collectedDrops = new ArrayList<>();
    public int numCollectedDrops = 0;
    public int maxCollectedDrops;
    protected boolean needCollectedSync;

    protected EntityPlayer thrower;
    protected ItemStack yoyoStack = ItemStack.EMPTY;
    protected ItemStack yoyoStackLastTick = ItemStack.EMPTY;
    protected IYoyo yoyo = null;
    protected EnumHand hand;

    protected float weight;

    protected float cordLength;
    protected float maxLength;

    protected int attackCool;
    protected int maxCool;
    protected boolean shouldResetCool;

    protected int timeoutCounter;
    protected int duration;

    protected boolean interactsWithBlocks;

    protected boolean isRetracting = false;
    protected boolean canCancelRetract = true;
    protected int retractionTimeout = 0;

    protected int lastSlot = -1;

    protected boolean shouldGetStats = true;


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

        yoyoStackLastTick = yoyoStack;

        Vec3d handPos = getPlayerHandPos(1);
        setPosition(handPos.x, handPos.y, handPos.z);

        if (!world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty())
            setPosition(player.posX, player.posY + player.height * 0.85, player.posZ);
    }

    @Override
    protected void entityInit()
    {
        setNoGravity(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDrops(LivingDropsEvent event)
    {
        DamageSource source = event.getSource();
        Entity killer = source.getTrueSource();

        if (!(killer instanceof EntityPlayer) || killer.world.isRemote) return;

        EntityPlayer player = (EntityPlayer) killer;

        EntityYoyo yoyo = CASTERS.get(player);

        if (yoyo == null || !yoyo.isCollecting()) return;

        event.getDrops().forEach(yoyo::collectDrop);
        event.setCanceled(true);
    }

    @Override
    public Entity getThrower()
    {
        return thrower;
    }

    @Override
    public void setThrower(Entity entity)
    {
        if (entity instanceof EntityPlayer && !(entity instanceof FakePlayer))
        {
            thrower = ((EntityPlayer) entity);
            CASTERS.put(entity, this);
        }
    }

    @Nullable
    public IYoyo getYoyo()
    {
        return yoyo;
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

    public EnumHand getHand()
    {
        return hand;
    }

    public boolean isCollecting()
    {
        return maxCollectedDrops > 0;
    }

    public boolean canAttack()
    {
        return attackCool >= maxCool;
    }

    public void resetAttackCooldown()
    {
        shouldResetCool = true;
    }

    public int getTimeout()
    {
        return timeoutCounter;
    }

    public void increaseTimeout(int n)
    {
        timeoutCounter += n;
    }

    public Vec3d getPlayerHandPos(float partialTicks)
    {
        if (thrower == null) return new Vec3d(posX, posY, posZ);

        float yaw = thrower.rotationYaw;
        float pitch = thrower.rotationPitch;

        double posX = thrower.posX;
        double posY = thrower.posY;
        double posZ = thrower.posZ;

        if (partialTicks != 1)
        {
            yaw = (float) (interpolateValue(thrower.prevRotationYaw, thrower.rotationYaw, partialTicks));
            pitch = (float) (interpolateValue(thrower.prevRotationPitch, thrower.rotationPitch, partialTicks));

            posX = interpolateValue(thrower.prevPosX, thrower.posX, (double) partialTicks);
            posY = interpolateValue(thrower.prevPosY, thrower.posY, (double) partialTicks);
            posZ = interpolateValue(thrower.prevPosZ, thrower.posZ, (double) partialTicks);
        }

        double throwerLookOffsetX = Math.cos(yaw * 0.01745329238474369D);
        double throwerLookOffsetZ = Math.sin(yaw * 0.01745329238474369D);
        double throwerLookOffsetY = Math.sin(pitch * 0.01745329238474369D);
        double throwerLookWidth = Math.cos(pitch * 0.01745329238474369D);

        float side = -1;
        if ((thrower.getPrimaryHand() == EnumHandSide.RIGHT) == (hand == EnumHand.MAIN_HAND)) side = 1;

        return new Vec3d(posX - throwerLookOffsetX * side * 0.4D - throwerLookOffsetZ * 0.5D * throwerLookWidth, posY + thrower.eyeHeight * 0.85D - throwerLookOffsetY * 0.5D - 0.25D, posZ - throwerLookOffsetZ * side * 0.4D + throwerLookOffsetX * 0.5D * throwerLookWidth);
    }

    public float getRotation(int age, float partialTicks)
    {
        float ageInTicks = age + partialTicks;
        float multiplier = 35;

        if (duration != -1) multiplier *= 2 - ageInTicks / ((float) duration);

        return ageInTicks * multiplier;
    }

    public boolean isRetracting()
    {
        return isRetracting;
    }

    public void setRetracting(boolean retracting)
    {
        if (canCancelRetract || !isRetracting) isRetracting = retracting;
    }

    public void forceRetract()
    {
        canCancelRetract = false;
        isRetracting = true;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (thrower != null && !thrower.isDead)
        {
            yoyo = checkAndGetYoyoObject();

            if (yoyo == null) return;

            if (duration >= 0 && ++timeoutCounter >= duration) forceRetract();

            updateMotion();
            moveAndCollide();

            yoyo.onUpdate(yoyoStack, this);

            if (!world.isRemote && interactsWithBlocks) worldInteraction();

            if (isCollecting()) updateCapturedDrops();

            if (ModConfig.yoyoSwing) handlePlayerPulling();

            resetOrIncrementAttackCooldown();
        }
        else setDead();
    }

    @Nullable
    protected IYoyo checkAndGetYoyoObject()
    {
        yoyoStack = thrower.getHeldItemMainhand();

        if (yoyoStack == ItemStack.EMPTY || !(yoyoStack.getItem() instanceof IYoyo))
        {
            yoyoStack = thrower.getHeldItemOffhand();

            if (yoyoStack != ItemStack.EMPTY) hand = EnumHand.OFF_HAND;
        }
        else hand = EnumHand.MAIN_HAND;

        int currentSlot = hand == EnumHand.MAIN_HAND ? thrower.inventory.currentItem : -2;

        ItemStack otherHand = thrower.getHeldItem(hand == EnumHand.MAIN_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);

        if (!CASTERS.containsKey(thrower) || !(yoyoStack.getItem() instanceof IYoyo) || (ticksExisted > 1 && ((lastSlot != -1 && lastSlot != currentSlot) || otherHand == yoyoStackLastTick)))
        {
            setDead();
            return null;
        }

        yoyoStackLastTick = yoyoStack;

        if (yoyoStack.getMaxDamage() < yoyoStack.getItemDamage() && yoyoStack.getItem() != Yoyos.CREATIVE_YOYO)
        {
            setDead();
            return null;
        }

        if ((!world.isRemote && CASTERS.get(thrower) != this))
        {
            CASTERS.put(thrower, this);
        }

        IYoyo yoyo = (IYoyo) yoyoStack.getItem();

        getStats(yoyo);

        lastSlot = currentSlot;

        return yoyo;
    }

    protected void getStats(IYoyo yoyo)
    {
        if (shouldGetStats)
        {
            maxCollectedDrops = yoyo.getMaxCollectedDrops(yoyoStack);
            maxCool = yoyo.getAttackInterval(yoyoStack);
            duration = yoyo.getDuration(yoyoStack);
            cordLength = maxLength = yoyo.getLength(yoyoStack);
            weight = yoyo.getWeight(yoyoStack);

            interactsWithBlocks = yoyo.interactsWithBlocks(yoyoStack);

            shouldGetStats = false;
        }
    }

    protected void updateMotion()
    {
        Vec3d motion = getTarget().subtract(posX, posY + height / 2, posZ).scale(Math.min(1 / weight, 1.0));

        motionX = motion.x;
        motionY = motion.y;
        motionZ = motion.z;

        //Slow down in water, unless it has the modifier "aquadynamic"
        if (inWater)
        {
            float multiplier = yoyo.getWaterMovementModifier(yoyoStack);
            motionX *= multiplier;
            motionY *= multiplier;
            motionZ *= multiplier;
        }

        onGround = true; //TODO: This is the only way I've found to get the yoyo to throw out smoothly
    }

    protected Vec3d getTarget()
    {
        if (isRetracting())
        {
            Vec3d handPos = getPlayerHandPos(1);
            double dX = this.posX - handPos.x;
            double dY = this.posY - handPos.y;
            double dZ = this.posZ - handPos.z;

            if (dX * dX + dY * dY + dZ * dZ < 0.8 || retractionTimeout++ >= MAX_RETRACT_TIME) setDead();

            return handPos;
        }
        else
        {
            Vec3d eyePos = new Vec3d(thrower.posX, thrower.posY + thrower.eyeHeight, thrower.posZ);
            Vec3d lookVec = thrower.getLookVec();

            Vec3d target = new Vec3d(eyePos.x + lookVec.x * cordLength, eyePos.y + lookVec.y * cordLength, eyePos.z + lookVec.z * cordLength);
            retractionTimeout = 0;
            RayTraceResult rayTraceResult = getTargetLook(eyePos, target);

            if (rayTraceResult != null) target = rayTraceResult.hitVec;

            return target;
        }
    }

    public void moveAndCollide()
    {
        AxisAlignedBB yoyoBoundingBox = getEntityBoundingBox();
        AxisAlignedBB targetBoundingBox = yoyoBoundingBox.offset(motionX, motionY, motionZ);
        if (noClip)
        {
            setEntityBoundingBox(targetBoundingBox);
            resetPositionToBB();
            return;
        }

        AxisAlignedBB union = targetBoundingBox.union(yoyoBoundingBox);

        List<AxisAlignedBB> collisionBoxes = world.getCollisionBoxes(this, union);

        List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, union);

        final int steps = 50;

        for (int step = 0; step < steps; step++)
        {
            double dx = motionX / steps;
            double dy = motionY / steps;
            double dz = motionZ / steps;

            for (AxisAlignedBB box : collisionBoxes)
            {
                dx = box.calculateXOffset(yoyoBoundingBox, dx);
                dy = box.calculateYOffset(yoyoBoundingBox, dy);
                dz = box.calculateZOffset(yoyoBoundingBox, dz);
            }

            yoyoBoundingBox = yoyoBoundingBox.offset(dx, dy, dz);

            for (AxisAlignedBB box : collisionBoxes)
            {
                if (box.intersects(yoyoBoundingBox))
                {
                    dx = box.calculateXOffset(yoyoBoundingBox, dx);
                    dy = box.calculateYOffset(yoyoBoundingBox, dy);
                    dz = box.calculateZOffset(yoyoBoundingBox, dz);

                    yoyoBoundingBox = yoyoBoundingBox.offset(-dx, -dy, -dz);
                }
            }

            if (!world.isRemote)
            {
                ListIterator<Entity> iterator = entities.listIterator();

                while (iterator.hasNext())
                {
                    Entity entity = iterator.next();

                    if (entity == thrower)
                    {
                        iterator.remove();
                        continue;
                    }

                    if (entity.getEntityBoundingBox().intersects(yoyoBoundingBox))
                    {
                        interactWithEntity(entity);

                        iterator.remove();
                    }
                }
            }
        }

        setEntityBoundingBox(yoyoBoundingBox);
        resetPositionToBB();
    }

    public void interactWithEntity(Entity entity)
    {
        yoyo.entityInteraction(yoyoStack, thrower, hand, this, entity);
    }

    protected void worldInteraction()
    {
        BlockPos pos = getPosition();

        AxisAlignedBB entityBox = getEntityBoundingBox().grow(0.1);

        for (BlockPos.MutableBlockPos checkPos : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1)))
        {
            IBlockState state = world.getBlockState(checkPos);
            Block block = state.getBlock();

            if (block != Blocks.AIR && state.getBoundingBox(world, checkPos).offset(checkPos).intersects(entityBox))
            {
                yoyo.blockInteraction(yoyoStack, thrower, world, checkPos, state, block, this);
            }
        }
    }

    @Override
    public BlockPos getPosition()
    {
        return new BlockPos(this.posX, this.posY, this.posZ);
    }

    protected void updateCapturedDrops()
    {
        // If we're on the client, we trust the server
        if (!world.isRemote && !collectedDrops.isEmpty() && needCollectedSync)
        {
            Iterator<ItemStack> iterator = collectedDrops.iterator();

            HashMap<Item, ItemStack> existing = new HashMap<>();

            // We don't have to respect the items' max size here
            while (iterator.hasNext())
            {
                ItemStack collectedDrop = iterator.next();

                if (!collectedDrop.isEmpty())
                {
                    if (collectedDrop.getTagCompound() != null) continue;

                    Item item = collectedDrop.getItem();

                    ItemStack master = existing.get(item);

                    if (master != null && collectedDrop.isItemEqual(master))
                    {
                        master.grow(collectedDrop.getCount());
                        iterator.remove();
                    }
                    else
                    {
                        existing.put(item, collectedDrop);
                    }
                }
            }

            YoyoNetwork.INSTANCE.sendToAll(new MessageCollectedDrops(this));

            needCollectedSync = false;
        }
    }

    public void createItemDropOrCollect(ItemStack drop, BlockPos pos)
    {
        ItemStack remaining = drop;

        if (isCollecting())
        {
            remaining = collectDrop(drop);

            if (remaining == ItemStack.EMPTY) return;
        }

        float f = 0.7F;
        double d = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
        double d1 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
        double d2 = (double) (world.rand.nextFloat() * f) + (double) (1.0F - f) * 0.5D;
        EntityItem entityitem = new EntityItem(world, (double) pos.getX() + d, (double) pos.getY() + d1, (double) pos.getZ() + d2, remaining);
        entityitem.setDefaultPickupDelay();
        world.spawnEntity(entityitem);
    }

    /**
     * @return the amount of stack left uncollected
     */
    public ItemStack collectDrop(ItemStack stack)
    {
        if (!isCollecting()) return stack;

        int maxTake = maxCollectedDrops - numCollectedDrops;

        ItemStack take = stack.splitStack(maxTake);
        collectedDrops.add(take);
        needCollectedSync = true;
        numCollectedDrops += take.getCount();

        return stack;
    }

    public void collectDrop(@Nullable EntityItem drop)
    {
        if (drop == null) return;

        ItemStack stack = drop.getItem();
        int countBefore = stack.getCount();
        collectDrop(stack);

        if (countBefore == stack.getCount()) return;

        drop.setItem(stack);

        if (stack.isEmpty())
        {
            drop.setInfinitePickupDelay();
            drop.setDead();
        }
        world.playSound(null, drop.posX, drop.posY, drop.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
    }

    protected void handlePlayerPulling()
    {
        double dx = posX - thrower.posX;
        double dy = (posY + height * 0.5) - (thrower.posY + thrower.eyeHeight);

        if (dy < 0 && cordLength < thrower.eyeHeight) dy += thrower.eyeHeight * 1.2;

        double dz = posZ - thrower.posZ;
        double distanceSqr = dx * dx + dy * dy + dz * dz;

        if (distanceSqr > cordLength * cordLength)
        {
            double stretch = Math.sqrt(distanceSqr) - cordLength;
            double scale = Math.min(0.04 * stretch * stretch, 0.4);
            thrower.addVelocity(dx * scale, dy * scale, dz * scale);
            thrower.fallDistance = 0;
            if (isRetracting) setDead();
        }
    }

    protected void resetOrIncrementAttackCooldown()
    {
        if (shouldResetCool)
        {
            attackCool = 0;
            shouldResetCool = false;
        }
        else
        {
            attackCool++;
        }
    }

    @Override
    public void setDead()
    {
        super.setDead();
        CASTERS.remove(thrower, this);

        if (collectedDrops.isEmpty()) return;

        if (!world.isRemote)
        {
            if (thrower != null)
            {
                for (ItemStack drop : collectedDrops)
                {
                    if (drop != null && !drop.isEmpty()) ItemHandlerHelper.giveItemToPlayer(thrower, drop);
                }
            }
            else // the yoyo was loaded into the world with items still attached
            {
                for (ItemStack drop : collectedDrops)
                {
                    if (drop != null && !drop.isEmpty())
                    {
                        while (drop.getCount() > 0)
                        {
                            ItemStack itemStack = drop.splitStack(drop.getMaxStackSize());

                            EntityItem entityitem = new EntityItem(world, posX, posY + height, posZ, itemStack);
                            entityitem.setDefaultPickupDelay();
                            entityitem.motionX = 0;
                            entityitem.motionZ = 0;

                            world.spawnEntity(entityitem);
                        }
                    }
                }
            }

        }
        collectedDrops.clear();
    }

    @Override
    protected void readEntityFromNBT(@Nonnull NBTTagCompound compound)
    {
        collectedDrops.clear();
        NBTTagList list = compound.getTagList("collectedDrops", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            ItemStack stack = new ItemStack(nbt);
            stack.setCount(nbt.getInteger("BigCount"));
            collectedDrops.add(stack);
        }
    }

    @Override
    protected void writeEntityToNBT(@Nonnull NBTTagCompound compound)
    {
        NBTTagList collected = new NBTTagList();

        for (ItemStack itemStack : collectedDrops)
        {
            NBTTagCompound nbt = itemStack.serializeNBT();
            nbt.setByte("Count", (byte) 1);
            nbt.setInteger("BigCount", itemStack.getCount());
            collected.appendTag(nbt);
        }

        compound.setTag("collectedDrops", collected);
    }

    @Nullable
    @Override
    public Team getTeam()
    {
        return thrower == null ? null : thrower.getTeam();
    }

    @Nullable
    protected RayTraceResult getTargetLook(final Vec3d from, final Vec3d to)
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

        List<Entity> list = world.getEntitiesInAABBexcluding(thrower, expanded, entity -> (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isSpectator()) && entity != null && entity.canBeCollidedWith());

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

    protected static double interpolateValue(double start, double end, double pct)
    {
        return start + (end - start) * pct;
    }

    @Nullable
    protected static RayTraceResult rayTraceBlocks(World world, Vec3d start, Vec3d end)
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

                    if (raytraceresult != null) return raytraceresult;
                }

                RayTraceResult rayTraceResult2 = null;
                int steps = 200;

                while (steps-- >= 0)
                {
                    if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) return null;

                    if (startX == endX && startY == endY && startZ == endZ) return rayTraceResult2;

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

                            if (rayTraceResult != null) return rayTraceResult;
                        }
                        else
                        {
                            rayTraceResult2 = new RayTraceResult(RayTraceResult.Type.MISS, start, enumfacing, blockpos);
                        }
                    }

                }

                return rayTraceResult2;
            }
            else return null;
        }
        else return null;
    }

}
