package com.jozufozu.yoyos.client;

import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.common.IYoyo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
    
    @SubscribeEvent
    public void onRenderSpecificHand(RenderSpecificHandEvent event)
    {
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
        //face away from player
        GlStateManager.rotate(90 - yaw, 0, 1, 0);
        //spin around
        GlStateManager.rotate(180 - pitch, 0, 0, 1);
        
        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }
        
        itemRenderer.renderItem(entity.getYoyoStack(), ItemCameraTransforms.TransformType.NONE);
        
        GlStateManager.popMatrix();
    
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
    
        for (EntityItem drop : entity.collectedDrops)
        {
            GlStateManager.pushMatrix();
            doRenderItem(drop, partialTicks);
            GlStateManager.popMatrix();
        }
    
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        this.bindEntityTexture(entity);
    
        if (boundTexture)
        {
            this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
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
        
        if (thrower instanceof EntityPlayer)
        {
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
            
            bufferBuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
            
            int color = 0xDDDDDD;
            if (entity.getYoyoStack().getItem() instanceof IYoyo)
            {
                color = ((IYoyo) entity.getYoyoStack().getItem()).getCordColor(entity.getYoyoStack());
            }
            
            float stringR = ((color >> 16) & 255) / 255F;
            float stringG = ((color >> 8) & 255) / 255F;
            float stringB = (color & 255) / 255F;
            
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
                bufferBuilder.pos(x + xDiff * (double) segment + 0.0D, y + yDiff * (double) (segment * segment + segment) * 0.5D, z + zDiff * (double) segment).color(r, g, b, 1.0F).endVertex();
                bufferBuilder.pos(x + xDiff * (double) segment + 0.025D, y + yDiff * (double) (segment * segment + segment) * 0.5D, z + zDiff * (double) segment).color(r, g, b, 1.0F).endVertex();
            }
            
            tessellator.draw();
            bufferBuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
            
            for (int k = 0; k <= 24; ++k)
            {
                float r = stringR;
                float g = stringG;
                float b = stringB;
                
                if (k % 2 == 0)
                {
                    r *= 0.7F;
                    g *= 0.7F;
                    b *= 0.7F;
                }
                
                float segment = (float) k / 24.0F;
                bufferBuilder.pos(x + xDiff * (double) segment + 0.0D, y + yDiff * (double) (segment * segment + segment) * 0.5D, z + zDiff * (double) segment).color(r, g, b, 1.0F).endVertex();
                bufferBuilder.pos(x + xDiff * (double) segment + 0.025D, y + yDiff * (double) (segment * segment + segment) * 0.5D, z + zDiff * (double) segment + 0.025D).color(r, g, b, 1.0F).endVertex();
            }
            
            tessellator.draw();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.enableCull();
        }
    }
    
    private int transformModelCount(EntityItem itemIn, float partialTicks, IBakedModel model)
    {
        ItemStack itemstack = itemIn.getItem();

        boolean gui3d = model.isGui3d();
        int count = this.getModelCount(itemstack);
        float bob = MathHelper.sin(((float)itemIn.getAge() + partialTicks) / 10.0F + itemIn.hoverStart) * 0.1F + 0.1F;
        float scale = model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;
        GlStateManager.translate(0,  bob + 0.25F * scale, 0);
        
        if (gui3d || this.renderManager.options != null)
        {
            float f3 = (((float)itemIn.getAge() + partialTicks) / 20.0F + itemIn.hoverStart) * (180F / (float)Math.PI);
            GlStateManager.rotate(f3, 0.0F, 1.0F, 0.0F);
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
    
    public void doRenderItem(EntityItem entity, float partialTicks)
    {
        ItemStack itemstack = entity.getItem();
        this.random.setSeed(itemstack.isEmpty() ? 187 : Item.getIdFromItem(itemstack.getItem()) + itemstack.getMetadata());
        
        IBakedModel ibakedmodel = this.itemRenderer.getItemModelWithOverrides(itemstack, entity.world, null);
        int modelCount = this.transformModelCount(entity, partialTicks, ibakedmodel);
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
                
                IBakedModel transformedModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
                this.itemRenderer.renderItem(itemstack, transformedModel);
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
                
                IBakedModel transformedModel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
                this.itemRenderer.renderItem(itemstack, transformedModel);
                GlStateManager.popMatrix();
                GlStateManager.translate(0.0F, 0.0F, 0.09375F);
            }
        }
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
