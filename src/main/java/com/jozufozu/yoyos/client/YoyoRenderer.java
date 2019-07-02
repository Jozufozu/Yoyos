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

package com.jozufozu.yoyos.client;

import com.jozufozu.yoyos.common.YoyoEntity;
import com.jozufozu.yoyos.common.IYoyo;
import com.jozufozu.yoyos.common.RenderOrientation;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VisibleRegion;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AbsoluteHand;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class YoyoRenderer extends EntityRenderer<YoyoEntity>
{
    private ItemRenderer itemRenderer;
    private Random random = new Random();

    public YoyoRenderer(EntityRenderDispatcher renderManager)
    {
        super(renderManager);
        itemRenderer = MinecraftClient.getInstance().getItemRenderer();
    }

    @Override
    public boolean isVisible(YoyoEntity entity_1, VisibleRegion visibleRegion_1, double double_1, double double_2, double double_3)
    {
        return super.isVisible(entity_1, visibleRegion_1, double_1, double_2, double_3);
    }

    @Override
    public void render(YoyoEntity yoyoEntity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        Profiler mcProfiler = MinecraftClient.getInstance().getProfiler();
        mcProfiler.push("renderYoyo");
        
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y + yoyoEntity.getHeight() / 2, z);
        GlStateManager.scaled(.5, .5, .5);

        Vec3d pointTo = yoyoEntity.getPlayerHandPos(partialTicks).subtract(yoyoEntity.x, yoyoEntity.y, yoyoEntity.z).normalize();

        float yaw = (float) (Math.atan2(pointTo.x, pointTo.z) * -180 / Math.PI);

        GlStateManager.pushMatrix();

        if (yoyoEntity.getYoyo() != null && yoyoEntity.getYoyo().getRenderOrientation(yoyoEntity.getYoyoStack()) == RenderOrientation.Horizontal)
        {
            GlStateManager.rotated(-90, 1, 0, 0);         //be flat, like a lawnmower
        }
        else
        {
            GlStateManager.rotated(270 - yaw, 0, 1, 0);   //face away from player
        }

        GlStateManager.rotated(yoyoEntity.getRotation(yoyoEntity.getTimeout(), partialTicks), 0, 0, 1);    //spin around

        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entityYoyo));
        }

        itemRenderer.renderItem(yoyoEntity.getYoyoStack(), ModelTransformation.Type.NONE);

        GlStateManager.popMatrix();
    
        if (yoyoEntity.isCollecting() && !yoyoEntity.collectedDrops.isEmpty())
        {
            renderCollectedItems(yoyoEntity, partialTicks);
        }
    
        GlStateManager.popMatrix();
        
        renderChord(yoyoEntity, x, y, z, partialTicks);
    
        if (this.renderOutlines)
        {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }
        
        super.render(yoyoEntity, x, y, z, entityYaw, partialTicks);
        
        mcProfiler.pop();
    }

    public void renderCollectedItems(YoyoEntity entity, float partialTicks)
    {
        boolean boundTexture = false;
        if (this.bindEntityTexture(entity))
        {
            this.renderManager.textureManager.getTexture(this.getTexture(entity)).pushFilter(false, false);
            boundTexture = true;
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        int i = 0;
        for (ItemStack drop : entity.collectedDrops)
        {
            int count = drop.getCount();
            int max = drop.getMaxCount();

            while (count > 0)
            {
                doRenderItem(i++, entity, drop, partialTicks);
                count -= max;
            }
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        this.bindEntityTexture(entity);

        if (boundTexture)
        {
            this.renderManager.textureManager.getTexture(this.getTexture(entity)).popFilter();
        }
    }

    public static void renderChord(YoyoEntity entity, double x, double y, double z, float partialTicks)
    {
        Entity thrower = entity.getThrower();
        if (!(thrower instanceof PlayerEntity)) return;

        PlayerEntity player = ((PlayerEntity) thrower);

        y = y - (1.6 - thrower.getHeight()) * 0.5;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBufferBuilder();

        boolean rightHand = (player.getMainHand() == AbsoluteHand.RIGHT) == (entity.getHand() == Hand.MAIN_HAND);

        Vec3d handPos;

        if (MinecraftClient.getInstance().options.perspective != 0 || player.getEntityId() != MinecraftClient.getInstance().player.getEntityId())
        {
            double posX = interpolateValue(thrower.prevX, thrower.x, (double) partialTicks);
            double posY = interpolateValue(thrower.prevY, thrower.y, (double) partialTicks) + 1.272;
            double posZ = interpolateValue(thrower.prevZ, thrower.z, (double) partialTicks);
            double bodyRotation = interpolateValue(player.prevYaw, player.yaw, partialTicks);

            bodyRotation = Math.toRadians(bodyRotation);

            double rotation = bodyRotation;

            if (rightHand)
                rotation += Math.PI;

            final double shoulderRadius = 0.347;
            posX += Math.cos(rotation) * shoulderRadius;
            posZ += Math.sin(rotation) * shoulderRadius;

            double f = 1.0;

            if (player.getPose() == EntityPose.FALL_FLYING)
            {
                f = thrower.getVelocity().lengthSquared();
                f = f / 0.2F;
                f = f * f * f;
            }

            if (f < 1.0F)
            {
                f = 1.0F;
            }

            float limbSwing = player.handSwingTicks;
            float limbSwingAmount = player.getHandSwingProgress(partialTicks);

            double pitch = Math.cos(limbSwing * 0.6662 + (rightHand ? 0 : Math.PI)) * 2.0 * limbSwingAmount * 0.5 / f;

            pitch *= 0.5 - (Math.PI / 10.0);
            if (player.isSneaking())
            {
                pitch += 0.4;
                posY -= 0.4;
            }

            pitch += (rightHand ? -1 : 1) * Math.sin((player.totalExperience + partialTicks) * 0.067) * 0.05;

            double roll = Math.PI;
            double yaw = bodyRotation + Math.cos((player.age + partialTicks) * 0.09) * 0.05 + 0.05;

            posX += -1 * Math.sin(roll) * Math.cos(yaw) - Math.cos(roll) * Math.sin(pitch) * Math.sin(yaw);
            posY += Math.cos(pitch) * Math.cos(roll);
            posZ += Math.sin(roll) * Math.sin(yaw) - Math.cos(roll) * Math.sin(pitch) * Math.cos(yaw);

            handPos = new Vec3d(posX, posY, posZ);
        }
        else
        {
            double posX = interpolateValue(thrower.prevX, thrower.x, (double) partialTicks);
            double posY = interpolateValue(thrower.prevY, thrower.y, (double) partialTicks) + 1.1;
            double posZ = interpolateValue(thrower.prevZ, thrower.z, (double) partialTicks);

            double rotationYaw = Math.toRadians(interpolateValue(player.prevYaw, player.yaw, partialTicks));
            double rotationPitch = Math.toRadians(interpolateValue(player.prevPitch, player.pitch, partialTicks));

            double mirror = (rightHand ? -1 : 1);
            double radius = 0.1;

            double v = -Math.sin(rotationPitch) * mirror * mirror * radius;
            double angle = rotationYaw + Math.PI * 0.5;
            posX += Math.cos(rotationYaw) * mirror * radius + Math.cos(angle) * v;
            posY += Math.sin(-rotationPitch) * radius;
            posZ += Math.sin(rotationYaw) * mirror * radius + Math.sin(angle) * v;

            handPos = new Vec3d(posX, posY, posZ);
        }

        double yoyoPosX = interpolateValue(entity.prevX, entity.x, (double) partialTicks);
        double yoyoPosY = interpolateValue(entity.prevY, entity.y, (double) partialTicks) - entity.getHeight();
        double yoyoPosZ = interpolateValue(entity.prevZ, entity.z, (double) partialTicks);

        double xDiff = handPos.x - yoyoPosX;
        double yDiff = handPos.y - yoyoPosY;
        double zDiff = handPos.z - yoyoPosZ;

        GlStateManager.disableTexture();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        
        int color = 0xDDDDDD;
        IYoyo yoyo = entity.getYoyo();
        if (yoyo != null)
        {
            color = yoyo.getCordColor(entity.getYoyoStack(), entity.age + partialTicks);
        }
    
        float stringR = ((color >> 16) & 255) / 255F;
        float stringG = ((color >> 8) & 255) / 255F;
        float stringB = (color & 255) / 255F;

        for (int i = 0; i < 2; i++)
        {
            bufferBuilder.begin(5, VertexFormats.POSITION_COLOR);
            for (int j = 0; j <= 24; ++j)
            {
                float r = stringR;
                float g = stringG;
                float b = stringB;
        
                if (j % 2 == 0)
                {
                    r *= 0.7F;
                    g *= 0.7F;
                    b *= 0.7F;
                }
        
                double segment = j / 24.0;
                double zag = 0.0125 * ((i % 2) * 2 - 1);
                double x1 = x + xDiff * segment;
                double y1 = y + yDiff * (segment * segment + segment) * 0.5;
                double z1 = z + zDiff * segment;

                bufferBuilder.vertex(x1 - 0.0125, y1, z1 + zag).color(r, g, b, 1.0F).next();
                bufferBuilder.vertex(x1 + 0.0125, y1, z1 - zag).color(r, g, b, 1.0F).next();
            }
            tessellator.draw();
        }

        GlStateManager.enableLighting();
        GlStateManager.enableTexture();
        GlStateManager.enableCull();
    }
    
    private int transformModelCount(YoyoEntity yoyo, ItemStack itemStack, float partialTicks, BakedModel model)
    {
        boolean gui3d = model.hasDepthInGui();
        int count = this.getModelCount(itemStack);
        
        double bob = Math.sin((random.nextDouble() + yoyo.age + partialTicks) / 10.0 + random.nextDouble() * Math.PI * 2.0) * 0.1 + 0.1;
        
        double scale = model.getTransformation().getTransformation(ModelTransformation.Type.GROUND).scale.y();
        GlStateManager.translated(0,  bob + 0.25 * scale, 0);
        
        if (gui3d || this.renderManager.gameOptions != null)
        {
            double angle = ((random.nextDouble() + yoyo.age + partialTicks) / 20.0 + random.nextDouble() * Math.PI * 2.0) * (180.0 / Math.PI);
            GlStateManager.rotated((float) angle, 0.0F, 1.0F, 0.0F);
        }
        
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        return count;
    }
    
    protected int getModelCount(ItemStack stack)
    {
        int i = 1;
        
        if (stack.getCount() > 48)
        {
            i = 5;
        }
        else if (stack.getCount() > 32)
        {
            i = 4;
        }
        else if (stack.getCount() > 16)
        {
            i = 3;
        }
        else if (stack.getCount() > 1)
        {
            i = 2;
        }
        
        return i;
    }
    
    public void doRenderItem(int i, YoyoEntity yoyo, ItemStack itemStack, float partialTicks)
    {
        GlStateManager.pushMatrix();

        long seed = (Item.getRawId(itemStack.getItem()) * 31 + i) * 31 + itemStack.getCount();

        this.random.setSeed(seed);
        
        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, yoyo.world, null);
        int modelCount = this.transformModelCount(yoyo, itemStack, partialTicks, bakedModel);
        boolean gui3d = bakedModel.hasDepthInGui();
        
        if (!gui3d)
        {
            float f3 = -0.0F * (float)(modelCount - 1) * 0.5F;
            float f4 = -0.0F * (float)(modelCount - 1) * 0.5F;
            float f5 = -0.09375F * (float)(modelCount - 1) * 0.5F;
            GlStateManager.translated(f3, f4, f5);
        }
        
        for (int k = 0; k < modelCount; ++k)
        {
            if (gui3d)
            {
                GlStateManager.pushMatrix();
                
                if (k > 0)
                {
                    float f7 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f9 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f6 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    GlStateManager.translated(f7, f9, f6);
                }
                
                this.itemRenderer.renderItem(itemStack, ModelTransformation.Type.GROUND);
                GlStateManager.popMatrix();
            }
            else
            {
                GlStateManager.pushMatrix();
                
                if (k > 0)
                {
                    float f8 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    GlStateManager.translated(f8, f10, 0.0F);
                }
                
                this.itemRenderer.renderItem(itemStack, ModelTransformation.Type.GROUND);
                GlStateManager.popMatrix();
                GlStateManager.translated(0.0F, 0.0F, 0.09375F);
            }
        }
        
        GlStateManager.popMatrix();
    }
    
    private static double interpolateValue(double start, double end, double pct)
    {
        return start + (end - start) * pct;
    }

    private static float interpolateValue(float start, float end, float pct)
    {
        return start + (end - start) * pct;
    }


    @Override
    protected Identifier getTexture(YoyoEntity yoyoEntity)
    {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEX;
    }
}
