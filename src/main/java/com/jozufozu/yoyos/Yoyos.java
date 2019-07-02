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

import com.google.common.collect.Lists;
import com.jozufozu.yoyos.common.*;
import com.jozufozu.yoyos.compat.YoyoCompatibility;
import com.jozufozu.yoyos.tinkers.TinkersYoyos;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.client.sound.Sound;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;

public class Yoyos implements ModInitializer
{
    public static final String MODID = "yoyos";
    public static final String NAME = "Yoyos";
    public static final String VERSION = "@VERSION@";

    public static Logger LOG = LogManager.getLogger(MODID);

    public static File CONFIG_DIR;

    public static Item CORD;
    public static Item CREATIVE_YOYO;
    public static Item WOODEN_YOYO;
    public static Item STONE_YOYO;
    public static Item IRON_YOYO;
    public static Item DIAMOND_YOYO;
    public static Item GOLD_YOYO;
    public static Item SHEAR_YOYO;
    public static Item STICKY_YOYO;
    public static Item HOE_YOYO;

    public static Enchantment COLLECTING;
    public static EnchantmentTarget YOYO_ENCHANTMENT_TYPE = new EnchantmentTarget() {
        @Override
        public boolean isAcceptableItem(Item item)
        {
            return item instanceof IYoyo;
        }
    };

    public static Sound YOYO_THROW;
    public static SoundEvent YOYO_STICK;
    public static SoundEvent YOYO_CHASE;

    public static ItemGroup YOYOS_TAB = FabricItemGroupBuilder.create(new Identifier(MODID, "yoyos"))
                                                              .icon(() -> new ItemStack(CREATIVE_YOYO))
                                                              .build();

    public static final EntityType<YoyoEntity> YOYO_ENTITY_TYPE =
            Registry.register(
                    Registry.ENTITY_TYPE,
                    new Identifier(MODID, "yoyo"),
                    FabricEntityTypeBuilder.<YoyoEntity>create(EntityCategory.MISC, YoyoEntity::new)
                            .disableSummon()
                            .size(EntityDimensions.fixed(0.25f, 0.25f))
                            .trackable(64, 2, true)
                            .build()
            );
    public static final EntityType<StickyYoyoEntity> STICKY_YOYO_ENTITY_TYPE =
            Registry.register(
                    Registry.ENTITY_TYPE,
                    new Identifier(MODID, "yoyo"),
                    FabricEntityTypeBuilder.<StickyYoyoEntity>create(EntityCategory.MISC, StickyYoyoEntity::new)
                            .disableSummon()
                            .size(EntityDimensions.fixed(0.25f, 0.25f))
                            .trackable(64, 2, true)
                            .build()
            );

    @Override
    public void onInitialize()
    {
        FabricEntityTypeBuilder.registerModEntity(new ResourceLocation(Yoyos.MODID, "yoyo"), YoyoEntity.class, "YoYo", 0, Yoyos.INSTANCE, 64, 4, false);
        EntityRegistry.registerModEntity(new ResourceLocation(Yoyos.MODID, "yoyo_sticky"), StickyYoyoEntity.class, "Sticky_YoYo", 1, Yoyos.INSTANCE, 64, 4, true);



        Registry.register(Registry.ITEM, new Identifier(MODID, "cord"), CORD);

        registry.register(CREATIVE_YOYO = new ItemYoyo("creative_yoyo", Item.ToolMaterial.GOLD).setMaxDamage(0));
        registry.register(WOODEN_YOYO = new ItemYoyo("wooden_yoyo", Item.ToolMaterial.WOOD));
        registry.register(STONE_YOYO = new ItemYoyo("stone_yoyo", Item.ToolMaterial.STONE));
        registry.register(IRON_YOYO = new ItemYoyo("iron_yoyo", Item.ToolMaterial.IRON));
        registry.register(DIAMOND_YOYO = new ItemYoyo("diamond_yoyo", Item.ToolMaterial.DIAMOND));
        registry.register(GOLD_YOYO = new ItemYoyo("gold_yoyo", Item.ToolMaterial.GOLD));
        registry.register(SHEAR_YOYO = new ItemYoyo("shear_yoyo",
                                                    Item.ToolMaterial.IRON,
                                                    YoyoEntity::new,
                                                    Lists.newArrayList((yoyo, player, hand, yoyoEntity, targetEntity) -> ItemYoyo.shearEntity(yoyo, player, hand, yoyoEntity, targetEntity), (yoyo1, player1, hand1, yoyoEntity1, targetEntity1) -> ItemYoyo.collectItem(yoyo1, player1, hand1, yoyoEntity1, targetEntity1), (yoyo2, player2, hand2, yoyoEntity2, targetEntity2) -> ItemYoyo.attackEntity(yoyo2, player2, hand2, yoyoEntity2, targetEntity2)),
                                                    Lists.newArrayList((yoyo3, player3, pos, state, block, yoyoEntity3) -> ItemYoyo.garden(yoyo3, player3, pos, state, block, yoyoEntity3)))
                .setRenderOrientation(RenderOrientation.Horizontal));
        registry.register(HOE_YOYO = new ItemYoyo("hoe_yoyo", Item.ToolMaterial.DIAMOND)
                .addBlockInteraction((yoyo, player, pos, state, block, yoyoEntity) -> ItemYoyo.farm(yoyo, player, pos, state, block, yoyoEntity), (yoyo1, player1, pos1, state1, block1, yoyoEntity1) -> ItemYoyo.till(yoyo1, player1, pos1, state1, block1, yoyoEntity1)));
        registry.register(STICKY_YOYO = new ItemStickyYoyo());
    }

    @SubscribeEvent
    public void registerEnchantment(RegistryEvent.Register<Enchantment> event)
    {
        YOYOS_TAB.setRelevantEnchantmentTypes(YOYO_ENCHANTMENT_TYPE);
        COLLECTING = new EnchantmentCollecting(Enchantment.Rarity.UNCOMMON, YOYO_ENCHANTMENT_TYPE, new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND});

        event.getRegistry().register(COLLECTING);
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event)
    {
        ResourceLocation name = new ResourceLocation(MODID, "entity.yoyo.throw");
        YOYO_THROW = new SoundEvent(name);
        YOYO_THROW.setRegistryName(name);
        event.getRegistry().register(YOYO_THROW);

        name = new ResourceLocation(MODID, "entity.yoyo.stick");
        YOYO_STICK = new SoundEvent(name);
        YOYO_STICK.setRegistryName(name);
        event.getRegistry().register(YOYO_STICK);

        name = new ResourceLocation(MODID, "entity.yoyo.chase");
        YOYO_CHASE = new SoundEvent(name);
        YOYO_CHASE.setRegistryName(name);
        event.getRegistry().register(YOYO_CHASE);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        CONFIG_DIR = new File(event.getModConfigurationDirectory(), "/" + MODID);

        MinecraftForge.EVENT_BUS.register(proxy);
        proxy.preInit(event);

        if (Loader.isModLoaded("tconstruct") && ModConfig.tinkersYoyos)
        {
            TinkersYoyos.preInit(event);
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);

        if (Loader.isModLoaded("tconstruct") && ModConfig.tinkersYoyos)
        {
            TinkersYoyos.init(event);
        }

        if (Loader.isModLoaded("botania") & ModConfig.botaniaYoyos.enable)
        {
            YoyoCompatibility.initBotania();
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        if (Loader.isModLoaded("tconstruct") && ModConfig.tinkersYoyos)
        {
            TinkersYoyos.postInit(event);
        }
    }
}
