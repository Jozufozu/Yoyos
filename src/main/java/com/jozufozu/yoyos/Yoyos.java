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

package com.jozufozu.yoyos;

import com.jozufozu.yoyos.client.NetworkHandlers;
import com.jozufozu.yoyos.client.YoyoRenderer;
import com.jozufozu.yoyos.common.ItemYoyo;
import com.jozufozu.yoyos.common.ModItems;
import com.jozufozu.yoyos.common.StickyYoyoEntity;
import com.jozufozu.yoyos.common.YoyoEntity;
import com.jozufozu.yoyos.network.YoyoNetwork;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Yoyos.MODID)
public class Yoyos
{
    public static final String MODID = "yoyos";
    public static final String NAME = "Yoyos";
    public static final String VERSION = "@VERSION@";
    public static final ItemGroup YOYOS_TAB = new ItemGroup("yoyos")
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(ModItems.INSTANCE.getCREATIVE_YOYO());
        }
    };

    public static Logger LOG = LogManager.getLogger(MODID);

    public Yoyos()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, ModItems::onItemsRegistry);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Enchantment.class, RegistryEvents::registerEnchantment);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(EntityType.class, RegistryEvents::registerEntityTypes);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(SoundEvent.class, RegistryEvents::registerSounds);

        MinecraftForge.EVENT_BUS.addListener(YoyoEntity::onLivingDrops);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        YoyoNetwork.initialize();
    }

    private void doClientStuff(final FMLClientSetupEvent event)
    {
        RenderingRegistry.registerEntityRenderingHandler(YoyoEntity.class, YoyoRenderer::new);

        MinecraftForge.EVENT_BUS.addListener(NetworkHandlers::onTickWorldTick);
        MinecraftForge.EVENT_BUS.addListener(NetworkHandlers::onPlayerInteractRightClickItem);
    }

    @ObjectHolder("yoyos")
    public static class Sounds
    {
        public static final SoundEvent YOYO_THROW = null;
        public static final SoundEvent YOYO_STICK = null;
        // public static final SoundEvent YOYO_CHASE = null;
    }

    public static class Enchantments
    {
        @ObjectHolder("yoyos:collecting")
        public static final Enchantment COLLECTING = null;
        public static final EnchantmentType YOYO_ENCHANTMENT_TYPE = EnchantmentType.create("yoyo", item -> item instanceof ItemYoyo);
    }

    @ObjectHolder("yoyos")
    public static class EntityTypes
    {
        public static final EntityType<YoyoEntity> YOYO = null;
        public static final EntityType<StickyYoyoEntity> STICKY_YOYO = null;
    }
}
