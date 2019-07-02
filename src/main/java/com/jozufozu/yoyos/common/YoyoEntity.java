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
import com.jozufozu.yoyos.network.CollectedDropsS2CPacket;
import com.jozufozu.yoyos.network.YoyoNetwork;
import javafx.util.Pair;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Stream;

public class YoyoEntity extends Entity
{
    public static final HashMap<Entity, YoyoEntity> CASTERS = new HashMap<>();

    protected static final int MAX_RETRACT_TIME = 40;
    public ArrayList<ItemStack> collectedDrops = new ArrayList<>();
    public int numCollectedDrops = 0;
    public int maxCollectedDrops;
    protected boolean needCollectedSync;

    protected PlayerEntity thrower;
    protected ItemStack yoyoStack = ItemStack.EMPTY;
    protected ItemStack yoyoStackLastTick = ItemStack.EMPTY;
    protected IYoyo yoyo = null;
    protected Hand hand;

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

    public YoyoEntity(EntityType<?> type, World world)
    {
        super(type, world);
        ignoreCameraFrustum = true;
        setNoGravity(true);
    }

    public YoyoEntity(World world)
    {
        this(Yoyos.YOYO_ENTITY_TYPE, world);
    }

    public YoyoEntity(EntityType<?> type, World world, PlayerEntity player)
    {
        this(type, world);
        thrower = player;

        CASTERS.put(player, this);

        yoyoStack = thrower.getMainHandStack();

        if (yoyoStack == ItemStack.EMPTY)
        {
            yoyoStack = thrower.getOffHandStack();

            if (yoyoStack != ItemStack.EMPTY) hand = Hand.OFF_HAND;
        }
        else hand = Hand.MAIN_HAND;

        yoyoStackLastTick = yoyoStack;

        Vec3d handPos = getPlayerHandPos(1);
        setPosition(handPos.x, handPos.y, handPos.z);

        if (!world.doesNotCollide(this)) setPosition(player.x, player.y + player.getEyeHeight(player.getPose()), player.z);
    }

    public YoyoEntity(World world, PlayerEntity player)
    {
        this(Yoyos.YOYO_ENTITY_TYPE, world, player);
    }

    @Override
    protected void initDataTracker()
    {

    }

    @Override
    protected void readCustomDataFromTag(CompoundTag compound)
    {
        collectedDrops.clear();
        ListTag list = compound.getList("collectedDrops", 10);

        for (int i = 0; i < list.size(); i++)
        {
            CompoundTag nbt = list.getCompoundTag(i);
            nbt.putByte("Count", (byte) 1);
            ItemStack stack = ItemStack.fromTag(nbt);
            stack.setCount(nbt.getInt("count"));
            collectedDrops.add(stack);
        }
    }

    @Override
    protected void writeCustomDataToTag(CompoundTag compound)
    {
        ListTag collected = new ListTag();

        for (ItemStack itemStack : collectedDrops)
        {
            CompoundTag stackTag = new CompoundTag();
            Identifier itemId = Registry.ITEM.getId(itemStack.getItem());
            stackTag.putString("id", itemId.toString());
            stackTag.putInt("count", itemStack.getCount());
            if (itemStack.hasTag()) {
                stackTag.put("tag", itemStack.getTag());
            }
            collected.add(stackTag);
        }

        compound.put("collectedDrops", collected);
    }

    @Override
    public Packet<?> createSpawnPacket()
    {
        return null;
    }

    public static void onLivingDrops(LivingDropsEvent event)
    {
        DamageSource source = event.getSource();
        Entity killer = source.getTrueSource();

        if (!(killer instanceof EntityPlayer) || killer.world.isRemote) return;

        EntityPlayer player = (EntityPlayer) killer;

        YoyoEntity yoyo = CASTERS.get(player);

        if (yoyo == null || !yoyo.isCollecting()) return;

        event.getDrops().forEach(yoyo::collectDrop);
        event.setCanceled(true);
    }

    public Entity getThrower()
    {
        return thrower;
    }

    public void setThrower(Entity entity)
    {
        if (entity instanceof PlayerEntity)
        {
            thrower = ((PlayerEntity) entity);
            CASTERS.put(entity, this);
        }
    }

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

    public Hand getHand()
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
        if (thrower == null) return new Vec3d(x, y, z);

        float yaw = thrower.yaw;
        float pitch = thrower.pitch;

        double posX = thrower.x;
        double posY = thrower.y;
        double posZ = thrower.z;

        if (partialTicks != 1)
        {
            yaw = (float) (interpolateValue(thrower.prevYaw, thrower.yaw, partialTicks));
            pitch = (float) (interpolateValue(thrower.prevPitch, thrower.pitch, partialTicks));

            posX = interpolateValue(thrower.prevX, thrower.x, (double) partialTicks);
            posY = interpolateValue(thrower.prevY, thrower.y, (double) partialTicks);
            posZ = interpolateValue(thrower.prevZ, thrower.z, (double) partialTicks);
        }

        double throwerLookOffsetX = Math.cos(yaw * 0.01745329238474369D);
        double throwerLookOffsetZ = Math.sin(yaw * 0.01745329238474369D);
        double throwerLookOffsetY = Math.sin(pitch * 0.01745329238474369D);
        double throwerLookWidth = Math.cos(pitch * 0.01745329238474369D);

        float side = -1;
        if ((thrower.getMainHand() == AbsoluteHand.RIGHT) == (hand == Hand.MAIN_HAND)) side = 1;

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
    public void tick()
    {
        super.tick();

        if (thrower != null && !thrower.removed)
        {
            yoyo = checkAndGetYoyoObject();

            if (yoyo == null) return;

            if (duration >= 0 && ++timeoutCounter >= duration) forceRetract();

            updateMotion();
            moveAndCollide();

            yoyo.onUpdate(yoyoStack, this);

            if (!world.isClient && interactsWithBlocks) worldInteraction();

            if (isCollecting()) updateCapturedDrops();

            if (ModConfig.yoyoSwing) handlePlayerPulling();

            resetOrIncrementAttackCooldown();
        }
        else remove();
    }

    protected IYoyo checkAndGetYoyoObject()
    {
        yoyoStack = thrower.getMainHandStack();

        if (yoyoStack == ItemStack.EMPTY || !(yoyoStack.getItem() instanceof IYoyo))
        {
            yoyoStack = thrower.getOffHandStack();

            if (yoyoStack != ItemStack.EMPTY) hand = Hand.OFF_HAND;
        }
        else hand = Hand.MAIN_HAND;

        int currentSlot = hand == Hand.MAIN_HAND ? thrower.inventory.selectedSlot : -2;

        ItemStack otherHand = thrower.getStackInHand(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND);

        if (!CASTERS.containsKey(thrower) || !(yoyoStack.getItem() instanceof IYoyo) || (lastSlot != -1 && lastSlot != currentSlot) || otherHand == yoyoStackLastTick)
        {
            remove();
            return null;
        }

        yoyoStackLastTick = yoyoStack;

        if (yoyoStack.getMaxDamage() < yoyoStack.getDamage() && yoyoStack.getItem() != Yoyos.CREATIVE_YOYO)
        {
            remove();
            return null;
        }

        if ((!world.isClient && CASTERS.get(thrower) != this))
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
        Vec3d motion = getTarget().subtract(x, y + getHeight() / 2, z).multiply(Math.min(1 / weight, 1.0));

        //Slow down in water, unless it has the modifier "aquadynamic"
        if (insideWater)
        {
            motion = motion.multiply(yoyo.getWaterMovementModifier(yoyoStack));
        }

        setVelocity(motion);

        onGround = true; //TODO: This is the only way I've found to get the yoyo to throw out smoothly
    }

    protected Vec3d getTarget()
    {
        if (isRetracting())
        {
            Vec3d handPos = getPlayerHandPos(1);
            double dX = this.x - handPos.x;
            double dY = this.y - handPos.y;
            double dZ = this.z - handPos.z;

            if (dX * dX + dY * dY + dZ * dZ < 0.8 || retractionTimeout++ >= MAX_RETRACT_TIME) remove();

            return handPos;
        }
        else
        {
            Vec3d eyePos = new Vec3d(thrower.x, thrower.y + thrower.eyeHeight, thrower.z);
            Vec3d lookVec = thrower.getLookVec();

            Vec3d target = new Vec3d(eyePos.x + lookVec.x * cordLength, eyePos.y + lookVec.y * cordLength, eyePos.z + lookVec.z * cordLength);
            retractionTimeout = 0;
            HitResult rayTraceResult = getTargetLook(eyePos, target);

            if (rayTraceResult != null) target = rayTraceResult.hitVec;

            return target;
        }
    }

    public void moveAndCollide()
    {
        Box yoyoBoundingBox = getBoundingBox();
        Box targetBoundingBox = yoyoBoundingBox.offset(getVelocity());
        if (noClip)
        {
            setBoundingBox(targetBoundingBox);
            moveToBoundingBoxCenter();
            return;
        }

        Box union = targetBoundingBox.union(yoyoBoundingBox);

        Stream<VoxelShape> collisionBoxes = world.method_20812(this, union);

        List<Entity> entities = world.getEntities(this, union);

        final int steps = 50;

        for (int step = 0; step < steps; step++)
        {
            double dx = motionX / steps;
            double dy = motionY / steps;
            double dz = motionZ / steps;

            collisionBoxes.forEach(voxelShape -> voxelShape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
                Box one = new Box(minX, minY, minZ, maxX, maxY, maxZ);
                dx = calculateXOffset(one, yoyoBoundingBox, dx);
                dy = calculateYOffset(one, yoyoBoundingBox, dy);
                dz = calculateZOffset(one, yoyoBoundingBox, dz);
            }));

            yoyoBoundingBox = yoyoBoundingBox.offset(dx, dy, dz);

            for (VoxelShape box : collisionBoxes)
            {
                if (box.intersects(yoyoBoundingBox))
                {
                    dy = box.calculateYOffset(yoyoBoundingBox, dy);
                    dx = box.calculateXOffset(yoyoBoundingBox, dx);
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

    public double calculateXOffset(Box one, Box other, double offsetX)
    {
        if (other.maxY > one.minY && other.minY < one.maxY && other.maxZ > one.minZ && other.minZ < one.maxZ)
        {
            if (offsetX > 0.0D && other.maxX <= one.minX)
            {
                double d1 = one.minX - other.maxX;

                if (d1 < offsetX)
                {
                    offsetX = d1;
                }
            }
            else if (offsetX < 0.0D && other.minX >= one.maxX)
            {
                double d0 = one.maxX - other.minX;

                if (d0 > offsetX)
                {
                    offsetX = d0;
                }
            }

            return offsetX;
        }
        else
        {
            return offsetX;
        }
    }

    public double calculateYOffset(Box one, Box other, double offsetY)
    {
        if (other.maxX > one.minX && other.minX < one.maxX && other.maxZ > one.minZ && other.minZ < one.maxZ)
        {
            if (offsetY > 0.0D && other.maxY <= one.minY)
            {
                double d1 = one.minY - other.maxY;

                if (d1 < offsetY)
                {
                    offsetY = d1;
                }
            }
            else if (offsetY < 0.0D && other.minY >= one.maxY)
            {
                double d0 = one.maxY - other.minY;

                if (d0 > offsetY)
                {
                    offsetY = d0;
                }
            }

            return offsetY;
        }
        else
        {
            return offsetY;
        }
    }

    public double calculateZOffset(Box one, Box other, double offsetZ)
    {
        if (other.maxX > one.minX && other.minX < one.maxX && other.maxY > one.minY && other.minY < one.maxY)
        {
            if (offsetZ > 0.0D && other.maxZ <= one.minZ)
            {
                double d1 = one.minZ - other.maxZ;

                if (d1 < offsetZ)
                {
                    offsetZ = d1;
                }
            }
            else if (offsetZ < 0.0D && other.minZ >= one.maxZ)
            {
                double d0 = one.maxZ - other.minZ;

                if (d0 > offsetZ)
                {
                    offsetZ = d0;
                }
            }

            return offsetZ;
        }
        else
        {
            return offsetZ;
        }
    }

    public void interactWithEntity(Entity entity)
    {
        yoyo.entityInteraction(yoyoStack, thrower, hand, this, entity);
    }

    protected void worldInteraction()
    {
        BlockPos pos = getPosition();

        Box entityBox = getEntityBoundingBox().grow(0.1);

        BlockPos.Mutable.stream(pos.add(-1, -1, -1), pos.add(1, 1, 1))
                        .map((blockPos) -> new Pair<>(blockPos, world.getBlockState(blockPos)))
                        .filter((pair) -> pair.getValue().getBlock() != Blocks.AIR)
        for (BlockPos.Mutable checkPos : )
        {
            BlockState state = world.getBlockState(checkPos);
            Block block = state.getBlock();

            if (block != Blocks.AIR && state.getBoundingBox(world, checkPos).offset(checkPos).intersects(entityBox))
            {
                yoyo.blockInteraction(yoyoStack, thrower, world, checkPos, state, block, this);
            }
        }
    }

    protected void updateCapturedDrops()
    {
        // If we're on the client, we trust the server
        if (!world.isClient && !collectedDrops.isEmpty() && needCollectedSync)
        {
            Iterator<ItemStack> iterator = collectedDrops.iterator();

            HashMap<Item, ItemStack> existing = new HashMap<>();

            // We don't have to respect the items' max size here
            while (iterator.hasNext())
            {
                ItemStack collectedDrop = iterator.next();

                if (!collectedDrop.isEmpty())
                {
                    if (collectedDrop.hasTag()) continue;

                    Item item = collectedDrop.getItem();

                    ItemStack master = existing.get(item);

                    if (master != null && collectedDrop.isItemEqual(master))
                    {
                        master.increment(collectedDrop.getCount());
                        iterator.remove();
                    }
                    else
                    {
                        existing.put(item, collectedDrop);
                    }
                }
            }


            CollectedDropsS2CPacket packet = new CollectedDropsS2CPacket(this);
            PlayerStream.watching(this).forEach((playerEntity) -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerEntity, packet));

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
        ItemEntity entityitem = EntityType.ITEM.create(world);
        entityitem.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        entityitem.setStack(remaining);
        entityitem.setVelocity((double) (world.random.nextFloat() * f) + (double) (1.0F - f) * 0.5D,
                               (double) (world.random.nextFloat() * f) + (double) (1.0F - f) * 0.5D,
                               (double) (world.random.nextFloat() * f) + (double) (1.0F - f) * 0.5D);
        entityitem.setToDefaultPickupDelay();
        world.spawnEntity(entityitem);
    }

    /**
     * @return the amount of stack left uncollected
     */
    public ItemStack collectDrop(ItemStack stack)
    {
        if (!isCollecting()) return stack;

        int maxTake = maxCollectedDrops - numCollectedDrops;

        ItemStack take = stack.split(maxTake);
        collectedDrops.add(take);
        needCollectedSync = true;
        numCollectedDrops += take.getCount();

        return stack;
    }

    public void collectDrop(ItemEntity drop)
    {
        if (drop == null) return;

        ItemStack stack = drop.getStack();
        int countBefore = stack.getCount();
        collectDrop(stack);

        if (countBefore == stack.getCount()) return;

        drop.setStack(stack);

        if (stack.isEmpty())
        {
            drop.setPickupDelayInfinite();
            drop.remove();
        }
        world.playSound(null, drop.x, drop.y, drop.z, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.NEUTRAL, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
    }

    protected void handlePlayerPulling()
    {
        double dx = x - thrower.x;

        double eyeHeight = thrower.getActiveEyeHeight(thrower.getPose(), thrower.getDimensions(thrower.getPose()));

        double dy = (y + getHeight() * 0.5) - (thrower.y + eyeHeight);

        if (dy < 0 && cordLength < eyeHeight) dy += eyeHeight * 1.2;

        double dz = z - thrower.z;
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
    public void remove()
    {
        super.remove();
        CASTERS.remove(thrower, this);

        if (collectedDrops.isEmpty()) return;

        if (!world.isClient)
        {
            if (thrower != null)
            {
                for (ItemStack drop : collectedDrops)
                {
                    if (drop != null && !drop.isEmpty()) thrower.inventory.offerOrDrop(world, drop);
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
                            ItemStack itemStack = drop.split(drop.getMaxCount());

                            ItemEntity entityitem = new ItemEntity(world, x, y + getHeight(), z, itemStack);
                            entityitem.setToDefaultPickupDelay();
                            entityitem.setVelocity(0, 0, 0);

                            world.spawnEntity(entityitem);
                        }
                    }
                }
            }

        }
        collectedDrops.clear();
    }

    @Override
    public AbstractTeam getScoreboardTeam()
    {
        return thrower == null ? null : thrower.getScoreboardTeam();
    }

    @Nullable
    protected HitResult getTargetLook(final Vec3d from, final Vec3d to)
    {
        double distance = from.distanceTo(to);
        BlockHitResult objectMouseOver = world.rayTrace(new RayTraceContext(from, to, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, thrower));
        boolean flag = false;
        double d1 = distance;

        if (distance > 3.0D)
        {
            flag = true;
        }

        if (objectMouseOver != null)
        {
            d1 = objectMouseOver.getPos().distanceTo(from);
        }

        Vec3d vec3d1 = thrower.getCameraPosVec(1);
        Entity pointedEntity = null;
        Vec3d vec3d3 = null;
        Box expanded = thrower.getBoundingBox().expand(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance).expand(1.0D, 1.0D, 1.0D);

        List<Entity> list = world.getEntities(thrower, expanded, entity -> (!(entity instanceof PlayerEntity) || !(entity.isSpectator()) && entity.collides()));

        double d2 = d1;

        for (Entity entity : list)
        {
            Box box = entity.getBoundingBox().expand(entity.getBoundingBoxMarginForTargeting());
            BlockHitResult raytraceresult = box.rayTrace(from, to);

            if (box.contains(from))
            {
                if (d2 >= 0.0D)
                {
                    pointedEntity = entity;
                    vec3d3 = raytraceresult == null ? from : raytraceresult.hitVec;
                    d2 = 0.0D;
                }
            }
            else if (raytraceresult != null)
            {
                double d3 = from.distanceTo(raytraceresult.hitVec);

                if (d3 < d2 || d2 == 0.0D)
                {
                    if (entity.getLowestRidingEntity() == thrower.getLowestRidingEntity() && !thrower.canRiderInteract())
                    {
                        if (d2 == 0.0D)
                        {
                            pointedEntity = entity;
                            vec3d3 = raytraceresult.hitVec;
                        }
                    }
                    else
                    {
                        pointedEntity = entity;
                        vec3d3 = raytraceresult.hitVec;
                        d2 = d3;
                    }
                }
            }
        }

        if (pointedEntity != null && flag)
        {
            pointedEntity = null;
            objectMouseOver = new BlockHitResult(BlockHitResult.Type.MISS, vec3d3, EnumFacing.UP, new BlockPos(vec3d3));
        }

        if (pointedEntity != null && objectMouseOver == null)
        {
            objectMouseOver = new BlockHitResult(pointedEntity, vec3d3);
        }

        return objectMouseOver;
    }

    protected static double interpolateValue(double start, double end, double pct)
    {
        return start + (end - start) * pct;
    }
}
