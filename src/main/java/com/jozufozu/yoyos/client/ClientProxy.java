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
import com.jozufozu.yoyos.common.CommonProxy;
import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.common.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event)
    {
        registerModel(Yoyos.CORD);
        
        if (!ModConfig.vanillaYoyos.enable)
            return;
        
        registerModel(Yoyos.WOODEN_YOYO);
        registerModel(Yoyos.STONE_YOYO);
        registerModel(Yoyos.IRON_YOYO);
        registerModel(Yoyos.DIAMOND_YOYO);
        registerModel(Yoyos.GOLD_YOYO);
        registerModel(Yoyos.SHEAR_YOYO);
        registerModel(Yoyos.HARVEST_YOYO);
        registerModel(Yoyos.STICKY_YOYO);
        registerModel(Yoyos.CREATIVE_YOYO);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderHand(RenderSpecificHandEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();

        EntityPlayerSP player = mc.player;

        EntityYoyo yoyo = EntityYoyo.CASTERS.get(player);
        if (yoyo == null || yoyo.getHand() != event.getHand()) return;

        if (player.isInvisible()) return;

        EnumHandSide enumhandside = event.getHand() == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();

        float swingProgress = event.getSwingProgress();

        GlStateManager.pushMatrix();
        boolean rightHand = enumhandside != EnumHandSide.LEFT;
        float mirror = rightHand ? 1.0F : -1.0F;
        float f1 = MathHelper.sqrt(swingProgress);
        float f2 = -0.3F * MathHelper.sin(f1 * (float)Math.PI);
        float f3 = 0.4F * MathHelper.sin(f1 * ((float)Math.PI * 2F));
        float f4 = -0.4F * MathHelper.sin(swingProgress * (float)Math.PI);
        GlStateManager.translate(mirror * (f2 + 0.64000005F), f3 + -0.6F + event.getEquipProgress() * -0.6F, f4 + -0.71999997F);
        GlStateManager.rotate(mirror * 45.0F, 0.0F, 1.0F, 0.0F);
        float f5 = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
        float f6 = MathHelper.sin(f1 * (float)Math.PI);
        GlStateManager.rotate(mirror * f6 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mirror * f5 * -20.0F, 0.0F, 0.0F, 1.0F);
        mc.getTextureManager().bindTexture(player.getLocationSkin());
        GlStateManager.translate(mirror * -1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(mirror * 120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(mirror * -135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(mirror * 5.6F, 0.0F, 0.0F);
        RenderPlayer renderplayer = (RenderPlayer)mc.getRenderManager().<AbstractClientPlayer>getEntityRenderObject(player);
        GlStateManager.disableCull();

        if (rightHand)
        {
            renderplayer.renderRightArm(player);
        }
        else
        {
            renderplayer.renderLeftArm(player);
        }

        GlStateManager.enableCull();
        GlStateManager.popMatrix();

        event.setCanceled(true);
    }
    
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        
        RenderingRegistry.registerEntityRenderingHandler(EntityYoyo.class, RenderYoYo::new);
    }
    
    @Override
    public boolean runningOnClient()
    {
        return true;
    }
    
    public static void registerModel(Item item)
    {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
