package com.jozufozu.yoyos.common

import com.jozufozu.yoyos.Yoyos
import net.minecraft.item.Item
import net.minecraft.item.ItemTier
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.registries.ForgeRegistries

object ModItems {
    val CORD: Item by name("cord")
    val CREATIVE_YOYO: Item by name("creative_yoyo")
    val WOODEN_YOYO: Item by name("wooden_yoyo")
    val STONE_YOYO: Item by name("stone_yoyo")
    val IRON_YOYO: Item by name("iron_yoyo")
    val DIAMOND_YOYO: Item by name("diamond_yoyo")
    val GOLD_YOYO: Item by name("gold_yoyo")
    val SHEAR_YOYO: Item by name("shear_yoyo")
    val HOE_YOYO: Item by name("hoe_yoyo")
    val STICKY_YOYO: Item by name("sticky_yoyo")

    private fun name(name: String) = lazy { ForgeRegistries.ITEMS.getValue(ResourceLocation(Yoyos.MODID, name))?: throw Exception("yoyos:$name could not be found in the Item registry") }

    @JvmStatic fun onItemsRegistry(event: RegistryEvent.Register<Item>) {
        event.registry.registerAll(
                Item(Item.Properties().group(Yoyos.YOYOS_TAB)).setRegistryName(Yoyos.MODID, "cord"),
                ItemYoyo("creative_yoyo", ItemTier.GOLD).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity),
                ItemYoyo("wooden_yoyo", ItemTier.WOOD).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity),
                ItemYoyo("stone_yoyo", ItemTier.STONE).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity),
                ItemYoyo("iron_yoyo", ItemTier.IRON).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity),
                ItemYoyo("diamond_yoyo", ItemTier.DIAMOND).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity),
                ItemYoyo("gold_yoyo", ItemTier.GOLD).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity),

                ItemYoyo("shear_yoyo", ItemTier.IRON)
                        .addEntityInteraction(Interactions::shearEntity, Interactions::collectItem, Interactions::attackEntity)
                        .addBlockInteraction(Interactions::garden)
                        .setRenderOrientation(RenderOrientation.Horizontal),

                ItemYoyo("hoe_yoyo", ItemTier.DIAMOND)
                        .addEntityInteraction(Interactions::collectItem, Interactions::attackEntity)
                        .addBlockInteraction(Interactions::farm, Interactions::till),
                ItemStickyYoyo("sticky_yoyo", ItemTier.DIAMOND).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity)
        )
    }
}
