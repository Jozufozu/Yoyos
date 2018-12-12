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

import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.common.IYoyo;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * yoyo - jozufozu
 * Created using Tabula 7.0.0
 *
 * I gave this a try, but much prefer the original aesthetic
 * Leaving it here for the future
 */
public class ModelYoyo extends ModelBase
{
    public ModelRenderer puffer;
    public ModelRenderer core;
    public ModelRenderer leftBlade;
    public ModelRenderer rightBlade;
    public ModelRenderer leftSide;
    public ModelRenderer rightSide;

    public ModelYoyo()
    {
        this.textureWidth = 64;
        this.textureHeight = 32;
        this.leftSide = new ModelRenderer(this, 0, 0);
        this.leftSide.setRotationPoint(0.5F, 0.0F, 0.0F);
        this.leftSide.addBox(0.0F, -2.0F, -2.0F, 1, 4, 4, 0.0F);
        this.puffer = new ModelRenderer(this, 0, 12);
        this.puffer.setRotationPoint(0.0F, -5.8F, 3.2F);
        this.puffer.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
        this.core = new ModelRenderer(this, 0, 0);
        this.core.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.core.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
        this.rightBlade = new ModelRenderer(this, 4, 8);
        this.rightBlade.setRotationPoint(-0.5F, 0.0F, 0.0F);
        this.rightBlade.addBox(-1.5F, -5.1F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(rightBlade, 2.276432943376204F, 0.0F, 0.0F);
        this.leftBlade = new ModelRenderer(this, 0, 8);
        this.leftBlade.setRotationPoint(0.5F, 0.0F, 0.0F);
        this.leftBlade.addBox(1.0F, -5.1F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(leftBlade, -0.7853981633974483F, 0.0F, 0.0F);
        this.rightSide = new ModelRenderer(this, 10, 0);
        this.rightSide.setRotationPoint(-0.5F, 0.0F, 0.0F);
        this.rightSide.addBox(-1.0F, -2.0F, -2.0F, 1, 4, 4, 0.0F);
    }

    @Override
    public void render(Entity entity, float orientation, float spin, float ageInTicks, float f3, float f4, float scale)
    {
        if (!(entity instanceof EntityYoyo)) return;

        EntityYoyo entityYoyo = (EntityYoyo) entity;

        IYoyo yoyo = entityYoyo.getYoyo();

        int leftColor = 0xFFFFFF;
        int rightColor = 0xFFFFFF;
        int coreColor = 0xFFFFFF;

        if (yoyo != null)
        {
            leftColor = yoyo.getLeftColor(entityYoyo.getYoyoStack(), ageInTicks);
            rightColor = yoyo.getRightColor(entityYoyo.getYoyoStack(), ageInTicks);
            coreColor = yoyo.getAxleColor(entityYoyo.getYoyoStack(), ageInTicks);
        }

        GlStateManager.disableCull();
        //GlStateManager.disableLighting();
        GlStateManager.rotate(orientation, 0, 1, 0);

        if (false) // has a puffer
        {
            GlStateManager.pushMatrix();

            float cos = MathHelper.cos(ageInTicks * 0.003f);

            GlStateManager.rotate(cos * cos * 8 - 4, 0, 0, 1);

            GlStateManager.translate(MathHelper.sin(ageInTicks * 0.03f) * 0.8, MathHelper.sin(ageInTicks * 0.08f) * 0.6, MathHelper.sin(ageInTicks * 0.05f));

            this.puffer.render(scale);

            GlStateManager.popMatrix();
        }

        color(coreColor);
        this.core.render(scale);

        GlStateManager.pushMatrix();

        if (entityYoyo.isGardening()) GlStateManager.rotate(90, 0, 0, 1);

        GlStateManager.rotate(spin, 1, 0, 0);

        color(rightColor);
        this.rightSide.render(scale);

        color(leftColor);
        this.leftSide.render(scale);

        if (entityYoyo.isGardening())
        {
            color(0xFFFFFF);
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.rightBlade.offsetX, this.rightBlade.offsetY, this.rightBlade.offsetZ);
            GlStateManager.translate(this.rightBlade.rotationPointX * scale, this.rightBlade.rotationPointY * scale, this.rightBlade.rotationPointZ * scale);
            GlStateManager.scale(0.4D, 1.0D, 1.0D);
            GlStateManager.translate(-this.rightBlade.offsetX, -this.rightBlade.offsetY, -this.rightBlade.offsetZ);
            GlStateManager.translate(-this.rightBlade.rotationPointX * scale, -this.rightBlade.rotationPointY * scale, -this.rightBlade.rotationPointZ * scale);
            this.rightBlade.render(scale);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            GlStateManager.translate(this.leftBlade.offsetX, this.leftBlade.offsetY, this.leftBlade.offsetZ);
            GlStateManager.translate(this.leftBlade.rotationPointX * scale, this.leftBlade.rotationPointY * scale, this.leftBlade.rotationPointZ * scale);
            GlStateManager.scale(0.4D, 1.0D, 1.0D);
            GlStateManager.translate(-this.leftBlade.offsetX, -this.leftBlade.offsetY, -this.leftBlade.offsetZ);
            GlStateManager.translate(-this.leftBlade.rotationPointX * scale, -this.leftBlade.rotationPointY * scale, -this.leftBlade.rotationPointZ * scale);
            this.leftBlade.render(scale);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    private void color(int color)
    {
        GlStateManager.color(((color >> 16) & 255) / 255F, ((color >> 8) & 255) / 255F, (color & 255) / 255F);
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
