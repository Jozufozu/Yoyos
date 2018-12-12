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

import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.common.EntityYoyo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class RenderModelYoYo extends RenderYoYo
{
    private static final ResourceLocation TEXTURE_YOYO = new ResourceLocation(Yoyos.MODID, "textures/entity/yoyo.png");
    private RenderItem itemRenderer;
    private Random random = new Random();

    private final ModelYoyo model = new ModelYoyo();

    public RenderModelYoYo(RenderManager renderManager)
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
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height / 2, z);
        GlStateManager.scale(0.0625, -0.0625, 0.0625);

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        if (this.renderOutlines)
        {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        Vec3d pointTo = entity.getPlayerHandPos(partialTicks).subtract(entity.posX, entity.posY + entity.height / 2, entity.posZ).normalize();

        float orientation = (float) (Math.atan2(pointTo.x, pointTo.z) * 180 / Math.PI);

        float spin = entity.getRotation(entity.ticksExisted, partialTicks);

        this.bindTexture(TEXTURE_YOYO);
        model.render(entity, orientation, spin, entity.ticksExisted + partialTicks, 0, 0, 1);
        
        GlStateManager.popMatrix();
    
        if (entity.isCollecting() && !entity.collectedDrops.isEmpty())
            renderCollectedItems(entity, partialTicks);
    
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
    
    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(EntityYoyo entity)
    {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
