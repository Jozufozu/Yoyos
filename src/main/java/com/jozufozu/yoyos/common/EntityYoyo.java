package com.jozufozu.yoyos.common;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.jozufozu.yoyos.common.materials.YoyoNBT;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;

import javax.annotation.Nonnull;
import java.util.List;

public class EntityYoyo extends Entity implements IThrowableEntity {

    private static final Predicate<Entity> CAN_DAMAGE = entity -> entity instanceof EntityLiving || entity instanceof EntityPlayer;
    private static final int MAX_RETRACT_TIME = 40;

    private EntityPlayer thrower;
    private ItemStack yoyoStack;
    private EnumHand hand;
    private YoyoNBT nbt;
    private boolean gardening;

    private boolean isRetracting = false;
    private boolean canCancelRetract = true;

    private int lastSlot = -1;

    private int retractionTimeout = 0;
    private int attackCool;
    private int maxCool;

    public EntityYoyo(World world) {
        super(world);
        this.ignoreFrustumCheck = true;
        this.width = 0.25F;
        this.height = 0.25F;
    }

    public EntityYoyo(World world, EntityPlayer player) {
        this(world);
        this.thrower = player;

        YoyoCore.casters.put(player, this);

        this.yoyoStack = this.thrower.getHeldItemMainhand();

        if (this.yoyoStack == null) {
            this.yoyoStack = this.thrower.getHeldItemOffhand();

            if (this.yoyoStack != null)
                this.hand = EnumHand.OFF_HAND;
        }
        else
            this.hand = EnumHand.MAIN_HAND;

        Vec3d handPos = this.getPlayerHandPos(1);
        this.setPosition(handPos.xCoord, handPos.yCoord, handPos.zCoord);
    }

    @Override
    protected void entityInit() {
        this.setNoGravity(true);
    }

    public void setRetracting(boolean retracting) {
        if (canCancelRetract || !isRetracting)
            isRetracting = retracting;
    }

    public boolean isRetracting() {
        return isRetracting;
    }

    public YoyoNBT getNbt() {
        return nbt;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.thrower != null) {
            this.yoyoStack = this.thrower.getHeldItemMainhand();

            if (this.yoyoStack == null || !(yoyoStack.getItem() instanceof YoyoCore)) {
                this.yoyoStack = this.thrower.getHeldItemOffhand();

                if (this.yoyoStack != null)
                    this.hand = EnumHand.OFF_HAND;
            }
            else
                this.hand = EnumHand.MAIN_HAND;

            int currentSlot = this.hand == EnumHand.MAIN_HAND ? this.thrower.inventory.currentItem : -2;

            if (!YoyoCore.casters.containsKey(this.thrower) || yoyoStack == null || (this.lastSlot != -1 && this.lastSlot != currentSlot) || ToolHelper.getCurrentDurability(this.yoyoStack) <= 0 || !(yoyoStack.getItem() instanceof YoyoCore)) {
                this.setDead();
                return;
            }

            if ((!worldObj.isRemote && YoyoCore.casters.get(this.thrower) != this)) {
                YoyoCore.casters.put(this.thrower, this);
            }

            this.lastSlot = currentSlot;

            if (this.nbt == null) {
                this.nbt = YoyoNBT.from(this.yoyoStack);
                this.gardening = !TinkerUtil.getModifierTag(this.yoyoStack, "gardening").hasNoTags();
                this.maxCool = (int)(((YoyoCore) this.yoyoStack.getItem()).attackSpeed() * 5);
            }

            int duration = nbt.duration;

            if (duration != -1 && this.ticksExisted >= duration)
                this.forceRetract();

            float length = nbt.chordLength;

            //handle position
            Vec3d eyePos = new Vec3d(this.thrower.posX, this.thrower.posY + this.thrower.eyeHeight, + this.thrower.posZ);
            Vec3d lookVec = this.thrower.getLookVec();

            Vec3d target = new Vec3d(eyePos.xCoord + lookVec.xCoord * length, eyePos.yCoord + lookVec.yCoord * length, eyePos.zCoord + lookVec.zCoord * length);

            if (this.isRetracting()) {
                if (retractionTimeout++ >= MAX_RETRACT_TIME) {
                    this.setDead();
                    return;
                }
                target = this.getPlayerHandPos(1);
            }
            else {
                retractionTimeout = 0;
                RayTraceResult rayTraceResult = this.getMouseOver(eyePos, target, false);

                if (rayTraceResult != null)
                    target = rayTraceResult.hitVec;
            }

            Vec3d motionVec = target.subtract(this.posX, this.posY + this.height / 2, this.posZ).scale(Math.min(1/nbt.weight, 1.5));

            if (this.inWater)
                motionVec = motionVec.scale(0.3);

            this.motionX = motionVec.xCoord;
            this.motionY = motionVec.yCoord;
            this.motionZ = motionVec.zCoord;

            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.onGround = true; //TODO: This is the only way I've found to get the yoyo to throw out smoothly

            if (!worldObj.isRemote) {
                //Tend to the roses
                if (this.gardening) {
                    BlockPos pos = this.getPosition();
                    IBlockState state = this.worldObj.getBlockState(pos);
                    Block block = state.getBlock();
                    /*
                    if (block instanceof IShearable)
                    {
                        IShearable shearable = (IShearable)block;
                        if (shearable.isShearable(this.yoyoStack, worldObj, pos))
                        {
                            List<ItemStack> drops = shearable.onSheared(this.yoyoStack, worldObj, pos,
                                    EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.FORTUNE, this.yoyoStack));

                            for(ItemStack stack : drops) {
                                float f = 0.7F;
                                double d  = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                                double d1 = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                                double d2 = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                                net.minecraft.entity.item.EntityItem entityitem = new net.minecraft.entity.item.EntityItem(worldObj, (double)pos.getX() + d, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
                                entityitem.setDefaultPickupDelay();
                                worldObj.spawnEntityInWorld(entityitem);
                            }

                            worldObj.playSound(null, pos, block.getSoundType(state, worldObj, pos, this).getBreakSound(), SoundCategory.BLOCKS, 1, 1);
                            worldObj.setBlockToAir(pos);

                            ToolHelper.damageTool(this.yoyoStack, 1, this.thrower);
                            this.thrower.addStat(StatList.getBlockStats(block));
                        }
                    }
                    else */
                    if (block != Blocks.AIR && block instanceof BlockBush) {
                        block.dropBlockAsItemWithChance(this.worldObj, pos, state, 1.0F, 0);
                        if (!this.thrower.isCreative())
                            ToolHelper.damageTool(this.yoyoStack, 1, this.thrower);

                        worldObj.playSound(null, pos, block.getSoundType(state, worldObj, pos, this).getBreakSound(), SoundCategory.BLOCKS, 1, 1);
                        worldObj.setBlockToAir(pos);
                    }
                }

                //Kill stuff
                boolean hit = false;
                for (Entity entity : this.worldObj.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expandXyz(0.4), CAN_DAMAGE)) {
                    if (entity != this.thrower) {
                        if (attackCool >= maxCool) {
                            ToolHelper.attackEntity(this.yoyoStack, ((ToolCore) this.yoyoStack.getItem()), this.thrower, entity);
                            hit = true;
                        }
                    } else if (this.isRetracting())
                        this.setDead();
                }
                ++attackCool;
                if (hit) attackCool = 0;
            }
        }
        else
            this.setDead();
    }

    @Override
    public void setDead() {
        super.setDead();
        YoyoCore.casters.remove(this.thrower, this);
    }

    @Override
    protected void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
    }

    @Override
    protected void writeEntityToNBT(@Nonnull NBTTagCompound compound) {
    }

    @Override
    public void setThrower(Entity entity) {
        if (entity instanceof EntityPlayer && !(entity instanceof FakePlayer)) {
            this.thrower = ((EntityPlayer) entity);
            YoyoCore.casters.put(entity, this);
        }
    }

    @Override
    public Entity getThrower() {
        return thrower;
    }

    public ItemStack getYoyoStack() {
        return yoyoStack;
    }

    public Vec3d getPlayerHandPos(float partialTicks) {
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

    public static double interpolateValue(double start, double end, double pct) {
        return start + (end - start) * pct;
    }

    private void forceRetract() {
        this.canCancelRetract = false;
        this.isRetracting = true;
    }

    public RayTraceResult getMouseOver(Vec3d vec3d, Vec3d vec3d2, boolean stopOnLiquid) {
        double distance = vec3d.distanceTo(vec3d2);
        RayTraceResult objectMouseOver = rayTraceBlocks(this.worldObj, vec3d, vec3d2, stopOnLiquid);
        boolean flag = false;
        double d1 = distance;

        if (distance > 3.0D)
        {
            flag = true;
        }

        if (objectMouseOver != null)
        {
            d1 = objectMouseOver.hitVec.distanceTo(vec3d);
        }

        Vec3d vec3d1 = this.thrower.getLook(1);
        Entity pointedEntity = null;
        Vec3d vec3d3 = null;
        List<Entity> list = this.worldObj.getEntitiesInAABBexcluding(this.thrower, this.thrower.getEntityBoundingBox().addCoord(vec3d1.xCoord * distance, vec3d1.yCoord * distance, vec3d1.zCoord * distance).expand(1.0D, 1.0D, 1.0D),
                Predicates.and(EntitySelectors.NOT_SPECTATING, entity -> entity != null && entity.canBeCollidedWith()));
        double d2 = d1;

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = list.get(j);
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expandXyz((double)entity1.getCollisionBorderSize());
            RayTraceResult raytraceresult = axisalignedbb.calculateIntercept(vec3d, vec3d2);

            if (axisalignedbb.isVecInside(vec3d))
            {
                if (d2 >= 0.0D)
                {
                    pointedEntity = entity1;
                    vec3d3 = raytraceresult == null ? vec3d : raytraceresult.hitVec;
                    d2 = 0.0D;
                }
            }
            else if (raytraceresult != null)
            {
                double d3 = vec3d.distanceTo(raytraceresult.hitVec);

                if (d3 < d2 || d2 == 0.0D)
                {
                    if (entity1.getLowestRidingEntity() == this.thrower.getLowestRidingEntity() && !this.thrower.canRiderInteract())
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
            objectMouseOver = new RayTraceResult(RayTraceResult.Type.MISS, vec3d3, null, new BlockPos(vec3d3));
        }

        if (pointedEntity != null && objectMouseOver == null)
        {
            objectMouseOver = new RayTraceResult(pointedEntity, vec3d3);
        }

        return objectMouseOver;
    }

    public static RayTraceResult rayTraceBlocks(World world, Vec3d start, Vec3d end, boolean stopOnLiquid) {
        if (!Double.isNaN(start.xCoord) && !Double.isNaN(start.yCoord) && !Double.isNaN(start.zCoord))
        {
            if (!Double.isNaN(end.xCoord) && !Double.isNaN(end.yCoord) && !Double.isNaN(end.zCoord))
            {
                int endX = MathHelper.floor_double(end.xCoord);
                int endY = MathHelper.floor_double(end.yCoord);
                int endZ = MathHelper.floor_double(end.zCoord);
                int startX = MathHelper.floor_double(start.xCoord);
                int startY = MathHelper.floor_double(start.yCoord);
                int startZ = MathHelper.floor_double(start.zCoord);
                BlockPos blockpos = new BlockPos(startX, startY, startZ);
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if ((!(block instanceof BlockBush) || iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, stopOnLiquid))
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
                    if (Double.isNaN(start.xCoord) || Double.isNaN(start.yCoord) || Double.isNaN(start.zCoord))
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
                        currentX = (double)startX + 1.0D;
                    }
                    else if (endX < startX)
                    {
                        currentX = (double)startX + 0.0D;
                    }
                    else
                    {
                        atMaxX = false;
                    }

                    if (endY > startY)
                    {
                        currentY = (double)startY + 1.0D;
                    }
                    else if (endY < startY)
                    {
                        currentY = (double)startY + 0.0D;
                    }
                    else
                    {
                        atMaxY = false;
                    }

                    if (endZ > startZ)
                    {
                        currentZ = (double)startZ + 1.0D;
                    }
                    else if (endZ < startZ)
                    {
                        currentZ = (double)startZ + 0.0D;
                    }
                    else
                    {
                        atMaxZ = false;
                    }

                    double x = 999.0D;
                    double y = 999.0D;
                    double z = 999.0D;
                    double xDiff = end.xCoord - start.xCoord;
                    double yDiff = end.yCoord - start.yCoord;
                    double zDiff = end.zCoord - start.zCoord;

                    if (atMaxX)
                    {
                        x = (currentX - start.xCoord) / xDiff;
                    }

                    if (atMaxY)
                    {
                        y = (currentY - start.yCoord) / yDiff;
                    }

                    if (atMaxZ)
                    {
                        z = (currentZ - start.zCoord) / zDiff;
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
                        start = new Vec3d(currentX, start.yCoord + yDiff * x, start.zCoord + zDiff * x);
                    }
                    else if (y < z)
                    {
                        enumfacing = endY > startY ? EnumFacing.DOWN : EnumFacing.UP;
                        start = new Vec3d(start.xCoord + xDiff * y, currentY, start.zCoord + zDiff * y);
                    }
                    else
                    {
                        enumfacing = endZ > startZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        start = new Vec3d(start.xCoord + xDiff * z, start.yCoord + yDiff * z, currentZ);
                    }

                    startX = MathHelper.floor_double(start.xCoord) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    startY = MathHelper.floor_double(start.yCoord) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    startZ = MathHelper.floor_double(start.zCoord) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(startX, startY, startZ);
                    IBlockState state = world.getBlockState(blockpos);
                    Block block1 = state.getBlock();

                    if (!(block1 instanceof BlockBush) || state.getMaterial() == Material.PORTAL || state.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) {
                        if (block1.canCollideCheck(state, stopOnLiquid)) {
                            RayTraceResult rayTraceResult = state.collisionRayTrace(world, blockpos, start, end);

                            if (rayTraceResult != null) {
                                return rayTraceResult;
                            }
                        } else {
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
