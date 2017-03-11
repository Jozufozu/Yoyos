package com.jozufozu.yoyos;

import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.lang3.tuple.Pair;
import slimeknights.tconstruct.common.TinkerPulse;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IPattern;
import slimeknights.tconstruct.library.tools.Pattern;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.tools.ToolPart;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.List;
import java.util.Locale;

public abstract class AbstractTinkerPulse extends TinkerPulse {

    protected static List<ToolCore> tools = Lists.newLinkedList();
    protected static List<ToolPart> toolParts = Lists.newLinkedList();
    protected static List<IModifier> modifiers = Lists.newLinkedList();
    protected static List<Pair<Item, ToolPart>> toolPartPatterns = Lists.newLinkedList();

    protected static <T extends ToolCore> T registerTool(T item, String name) {
        tools.add(item);
        return registerItem(item, name);
    }

    protected ToolPart registerToolPart(ToolPart part, String name) {
        return registerToolPart(part, name, TinkerTools.pattern);
    }

    protected <T extends Item & IPattern> ToolPart registerToolPart(ToolPart part, String name, T pattern) {
        ToolPart ret = registerItem(part, name);

        if(pattern != null) {
            toolPartPatterns.add(Pair.of(pattern, ret));
        }

        toolParts.add(ret);

        return ret;
    }

    protected void registerStencil(Item pattern, ToolPart toolPart) {
        for(ToolCore toolCore : TinkerRegistry.getTools()) {
            for(PartMaterialType partMaterialType : toolCore.getRequiredComponents()) {
                if(partMaterialType.getPossibleParts().contains(toolPart)) {
                    ItemStack stencil = new ItemStack(pattern);
                    Pattern.setTagForPart(stencil, toolPart);
                    TinkerRegistry.registerStencilTableCrafting(stencil);
                    return;
                }
            }
        }
    }

    protected static <T extends Item> T registerItem(T item, String name) {
        if(!name.equals(name.toLowerCase(Locale.US))) {
            throw new IllegalArgumentException(String.format("Unlocalized names need to be all lowercase! Item: %s", name));
        }

        item.setUnlocalizedName(name);
        item.setRegistryName(new ResourceLocation(Yoyos.MODID, name));
        GameRegistry.register(item);
        return item;
    }

    protected <T extends IModifier> T registerModifier(T modifier) {
        TinkerRegistry.registerModifier(modifier);
        modifiers.add(modifier);
        return modifier;
    }
}
