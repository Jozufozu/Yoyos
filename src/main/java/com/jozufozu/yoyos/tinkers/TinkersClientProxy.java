package com.jozufozu.yoyos.tinkers;

import com.jozufozu.yoyos.Yoyos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.BookTransformer;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.tconstruct.common.ModelRegisterUtil;
import slimeknights.tconstruct.library.TinkerRegistryClient;
import slimeknights.tconstruct.library.book.sectiontransformer.MaterialSectionTransformer;
import slimeknights.tconstruct.library.book.sectiontransformer.ModifierSectionTransformer;
import slimeknights.tconstruct.library.client.CustomFontRenderer;
import slimeknights.tconstruct.library.client.ToolBuildGuiInfo;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.util.ArrayList;
import java.util.List;

public class TinkersClientProxy extends TinkersProxy
{
    public final static BookData INSTANCE = BookLoader.registerBook("yoyos", false, false);
    
    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event)
    {
        TinkersYoyos.toolParts.forEach(ModelRegisterUtil::registerPartModel);
        ModelRegisterUtil.registerToolModel(TinkersYoyos.YOYO);
        ModelRegisterUtil.registerItemModel(TinkersYoyos.BOOK);
        
        List<IModifier> yoyoMods = new ArrayList<>();
        
        yoyoMods.add(TinkerModifiers.modMendingMoss);
        yoyoMods.add(TinkerModifiers.modSharpness);
        yoyoMods.add(TinkerModifiers.modShulking);
        yoyoMods.add(TinkerModifiers.modSoulbound);
        yoyoMods.add(TinkerModifiers.modLuck);
        yoyoMods.add(TinkerModifiers.modReinforced);
        yoyoMods.add(TinkerModifiers.modNecrotic);
        yoyoMods.add(TinkerModifiers.modEmerald);
        yoyoMods.add(TinkersYoyos.EXTENSION);
        yoyoMods.add(TinkersYoyos.LUBRICATED);
        yoyoMods.add(TinkersYoyos.FLOATING);
        yoyoMods.add(TinkersYoyos.GARDENING);
        yoyoMods.add(TinkersYoyos.GLUEY);
        
        for (IModifier modifier : yoyoMods)
            ModelRegisterUtil.registerModifierModel(modifier, new ResourceLocation(Yoyos.MODID, "models/item/modifiers/" + modifier.getIdentifier()));
    }
    
    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer bookRenderer = new CustomFontRenderer(mc.gameSettings,
                new ResourceLocation("textures/font/ascii.png"),
                mc.renderEngine);
        bookRenderer.setUnicodeFlag(true);
        if(mc.gameSettings.language != null) {
            bookRenderer.setBidiFlag(mc.getLanguageManager().isCurrentLanguageBidirectional());
        }
        INSTANCE.fontRenderer = bookRenderer;
        
        INSTANCE.addRepository(new FileRepository(String.format("%s:%s", Yoyos.MODID, "book")));
        INSTANCE.addTransformer(new MaterialSectionTransformer());
        INSTANCE.addTransformer(new ModifierSectionTransformer());
        INSTANCE.addTransformer(BookTransformer.IndexTranformer());
        INSTANCE.addTransformer(new YoyoMaterialSectionTransformer());
        
        
        ToolBuildGuiInfo info = new ToolBuildGuiInfo(TinkersYoyos.YOYO);
        info.addSlotPosition(28, 62);
        info.addSlotPosition(8, 30);
        info.addSlotPosition(50, 48);
        info.addSlotPosition(29, 38);
        TinkerRegistryClient.addToolBuilding(info);
    }
}