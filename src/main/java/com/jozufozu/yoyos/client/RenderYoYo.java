package com.jozufozu.yoyos.client;

import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.common.IYoyo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class RenderYoYo extends Render<EntityYoyo>
{
    private RenderItem itemRenderer;
    private Random random = new Random();
    
    public RenderYoYo(RenderManager renderManager)
    {
        super(renderManager);
        itemRenderer = Minecraft.getMinecraft().getRenderItem();
    }
    
    @Override
    public boolean shouldRender(EntityYoyo livingEntity, ICamera camera, double camX, double camY, double camZ)
    {
        return true;
    }
    
    @Override
    public void doRender(EntityYoyo entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        Profiler mcProfiler = Minecraft.getMinecraft().mcProfiler;
        mcProfiler.startSection("renderYoyo");
        
        float ageInTicks = entity.ticksExisted + partialTicks;
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height / 2, z);
        GlStateManager.scale(.5, .5, .5);
        
        Vec3d pointer = entity.getPlayerHandPos(partialTicks).subtract(entity.posX, entity.posY + entity.height / 2, entity.posZ).normalize();
        
        float yaw = (float) (Math.atan2(pointer.x, pointer.z) * -180 / Math.PI);
        float multiplier = 35;
        
        if (entity.getDuration() != -1)
        {
            multiplier *= 2 - ageInTicks / ((float) entity.getDuration());
        }
        
        float pitch = ageInTicks * multiplier;
       
        GlStateManager.pushMatrix();
        
        if (entity.isGardening())
            GlStateManager.rotate(90, 1, 0, 0);         //be flat, like a lawnmower
        else
            GlStateManager.rotate(90 - yaw, 0, 1, 0);   //face away from player
        
        GlStateManager.rotate(180 - pitch, 0, 0, 1);    //spin around
        
        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }
        
        itemRenderer.renderItem(entity.getYoyoStack(), ItemCameraTransforms.TransformType.NONE);
        
        GlStateManager.popMatrix();
    
        if (entity.isCollecting() && !entity.collectedDrops.isEmpty())
        {
            boolean boundTexture = false;
            if (this.bindEntityTexture(entity))
            {
                this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);
                boundTexture = true;
            }
    
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            int i = 0;
            for (ItemStack drop : entity.collectedDrops)
                doRenderItem(i++, entity, drop, partialTicks);
    
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            this.bindEntityTexture(entity);
    
            if (boundTexture)
            {
                this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
            }
        }
    
        GlStateManager.popMatrix();
        
        renderChord(entity, x, y, z, partialTicks);
    
        if (this.renderOutlines)
        {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }
        
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        
        mcProfiler.endSection();
    }
    
    public static void renderChord(EntityYoyo entity, double x, double y, double z, float partialTicks)
    {
        Entity thrower = entity.getThrower();
        if (!(thrower instanceof EntityPlayer))
            return;
        
        y = y - (1.6D - (double) thrower.height) * 0.5D;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
    
        Vec3d handPos = entity.getPlayerHandPos(partialTicks);
        double yoyoPosX = interpolateValue(entity.prevPosX, entity.posX, (double) partialTicks);
        double yoyoPosY = interpolateValue(entity.prevPosY, entity.posY, (double) partialTicks) - entity.height;
        double yoyoPosZ = interpolateValue(entity.prevPosZ, entity.posZ, (double) partialTicks);
    
        double xDiff = (double) ((float) (handPos.x - yoyoPosX));
        double yDiff = (double) ((float) (handPos.y - yoyoPosY));
        double zDiff = (double) ((float) (handPos.z - yoyoPosZ));
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        
        int color = 0xDDDDDD;
        if (entity.getYoyoStack().getItem() instanceof IYoyo)
        {
            color = ((IYoyo) entity.getYoyoStack().getItem()).getCordColor(entity.getYoyoStack());
        }
    
        float stringR = ((color >> 16) & 255) / 255F;
        float stringG = ((color >> 8) & 255) / 255F;
        float stringB = (color & 255) / 255F;
    
        for (int i = 0; i < 2; i++)
        {
            bufferBuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
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
        
                float segment = (float) j / 24.0F;
                bufferBuilder.pos(x + xDiff * (double) segment,          y + yDiff * (double) (segment * segment + segment) * 0.5D, z + zDiff * (double) segment + 0.025D * (i % 2)      ).color(r, g, b, 1.0F).endVertex();
                bufferBuilder.pos(x + xDiff * (double) segment + 0.025D, y + yDiff * (double) (segment * segment + segment) * 0.5D, z + zDiff * (double) segment + 0.025D * ((i + 1) % 2)).color(r, g, b, 1.0F).endVertex();
            }
            tessellator.draw();
        }
        
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
    }
    
    private int transformModelCount(EntityYoyo yoyo, ItemStack itemStack, float partialTicks, IBakedModel model)
    {
        boolean gui3d = model.isGui3d();
        int count = this.getModelCount(itemStack);
        
        double bob = Math.sin((random.nextDouble() + yoyo.ticksExisted + partialTicks) / 10.0 + random.nextDouble() * Math.PI * 2.0) * 0.1 + 0.1;
        
        double scale = model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;
        GlStateManager.translate(0,  bob + 0.25 * scale, 0);
        
        if (gui3d || this.renderManager.options != null)
        {
            double angle = ((random.nextDouble() + yoyo.ticksExisted + partialTicks) / 20.0 + random.nextDouble() * Math.PI * 2.0) * (180.0 / Math.PI);
            GlStateManager.rotate((float) angle, 0.0F, 1.0F, 0.0F);
        }
        
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
    
    public void doRenderItem(int i, EntityYoyo yoyo, ItemStack itemStack, float partialTicks)
    {
        GlStateManager.pushMatrix();

        long seed = Item.getIdFromItem(itemStack.getItem()) << 16;
        seed |= itemStack.getMetadata() << 12;
        seed |= i;

        this.random.setSeed(seed);
        
        IBakedModel ibakedmodel = this.itemRenderer.getItemModelWithOverrides(itemStack, yoyo.world, null);
        int modelCount = this.transformModelCount(yoyo, itemStack, partialTicks, ibakedmodel);
        boolean gui3d = ibakedmodel.isGui3d();
        
        if (!gui3d)
        {
            float f3 = -0.0F * (float)(modelCount - 1) * 0.5F;
            float f4 = -0.0F * (float)(modelCount - 1) * 0.5F;
            float f5 = -0.09375F * (float)(modelCount - 1) * 0.5F;
            GlStateManager.translate(f3, f4, f5);
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
                    GlStateManager.translate(f7, f9, f6);
                }
                
                IBakedModel transformedModel = ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
                this.itemRenderer.renderItem(itemStack, transformedModel);
                GlStateManager.popMatrix();
            }
            else
            {
                GlStateManager.pushMatrix();
                
                if (k > 0)
                {
                    float f8 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    GlStateManager.translate(f8, f10, 0.0F);
                }
                
                IBakedModel transformedModel = ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
                this.itemRenderer.renderItem(itemStack, transformedModel);
                GlStateManager.popMatrix();
                GlStateManager.translate(0.0F, 0.0F, 0.09375F);
            }
        }
        
        GlStateManager.popMatrix();
    }
    
    private static double interpolateValue(double start, double end, double pct)
    {
        return start + (end - start) * pct;
    }
    
    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(EntityYoyo entity)
    {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
