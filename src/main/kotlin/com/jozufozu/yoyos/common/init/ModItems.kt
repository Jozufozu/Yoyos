package com.jozufozu.yoyos.common.init

import com.jozufozu.yoyos.Yoyos
import com.jozufozu.yoyos.common.Interactions
import com.jozufozu.yoyos.common.ItemStickyYoyo
import com.jozufozu.yoyos.common.ItemYoyo
import com.jozufozu.yoyos.common.YoyosConfig
import com.jozufozu.yoyos.common.api.RenderOrientation
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTier
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

    val YOYOS_TAB: ItemGroup = object : ItemGroup("yoyos") {
        override fun createIcon(): ItemStack {
            return ItemStack(CREATIVE_YOYO)
        }
    }

    private fun name(name: String) = registryName(ForgeRegistries.ITEMS, name)

    fun onItemsRegistry(event: RegistryEvent.Register<Item>) {

        if (!YoyosConfig.vanillaYoyos.enabled.get()) return

        event.registry.register(Item(Item.Properties().group(YOYOS_TAB)).setRegistryName(Yoyos.MODID, "cord"))

        if (YoyosConfig.vanillaYoyos.creativeYoyo.enabled.get())
            event.registry.register(ItemYoyo("creative_yoyo", ItemTier.GOLD).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity))
        if (YoyosConfig.vanillaYoyos.woodenYoyo.enabled.get())
            event.registry.register(ItemYoyo("wooden_yoyo", ItemTier.WOOD).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity))
        if (YoyosConfig.vanillaYoyos.stoneYoyo.enabled.get())
            event.registry.register(ItemYoyo("stone_yoyo", ItemTier.STONE).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity))
        if (YoyosConfig.vanillaYoyos.ironYoyo.enabled.get())
            event.registry.register(ItemYoyo("iron_yoyo", ItemTier.IRON).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity))
        if (YoyosConfig.vanillaYoyos.diamondYoyo.enabled.get())
            event.registry.register(ItemYoyo("diamond_yoyo", ItemTier.DIAMOND).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity))
        if (YoyosConfig.vanillaYoyos.goldYoyo.enabled.get())
            event.registry.register(ItemYoyo("gold_yoyo", ItemTier.GOLD).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity))

        if (YoyosConfig.vanillaYoyos.shearYoyo.enabled.get())
            event.registry.register(ItemYoyo("shear_yoyo", ItemTier.IRON)
                        .addEntityInteraction(Interactions::shearEntity, Interactions::collectItem, Interactions::attackEntity)
                        .addBlockInteraction(Interactions::garden)
                        .setRenderOrientation(RenderOrientation.Horizontal))

        if (YoyosConfig.vanillaYoyos.hoeYoyo.enabled.get())
            event.registry.register(ItemYoyo("hoe_yoyo", ItemTier.DIAMOND)
                        .addEntityInteraction(Interactions::collectItem, Interactions::attackEntity)
                        .addBlockInteraction(Interactions::farm, Interactions::till))
        if (YoyosConfig.vanillaYoyos.stickyYoyo.enabled.get())
            event.registry.register(ItemStickyYoyo("sticky_yoyo", ItemTier.DIAMOND).addEntityInteraction(Interactions::collectItem, Interactions::attackEntity))
    }
}
