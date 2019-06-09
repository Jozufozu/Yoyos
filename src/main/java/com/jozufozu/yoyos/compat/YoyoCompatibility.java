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

package com.jozufozu.yoyos.compat;

import com.jozufozu.yoyos.Yoyos;
import com.jozufozu.yoyos.client.ClientProxy;
import com.jozufozu.yoyos.common.EntityYoyo;
import com.jozufozu.yoyos.common.ItemYoyo;
import com.jozufozu.yoyos.common.ModConfig;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.lexicon.LexiconPage;
import vazkii.botania.api.lexicon.LexiconRecipeMappings;
import vazkii.botania.common.item.equipment.tool.elementium.ItemElementiumSword;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelSword;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lexicon.page.PageCraftingRecipe;
import vazkii.botania.common.lexicon.page.PageText;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class YoyoCompatibility
{
    public static final ArrayList<Item> compatItems = new ArrayList<>();

    private static Item registerItem(IForgeRegistry<Item> registry, Item item)
    {
        registry.register(item);
        compatItems.add(item);
        return item;
    }

    @Optional.Method(modid = "botania")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void loadBotaniaCompatibility(RegistryEvent.Register<Item> event)
    {
        BotaniaAPI.blacklistEntityFromGravityRod(EntityYoyo.class);

        if (!ModConfig.botaniaYoyos.enable) return;

        IForgeRegistry<Item> registry = event.getRegistry();
        Item mana_cord = registerItem(registry, new Item().setCreativeTab(CreativeTabs.MATERIALS).setRegistryName(Yoyos.MODID, "mana_cord").setUnlocalizedName("yoyos.mana_cord"));

        registerItem(registry, new ItemManaYoyo("manasteel_yoyo", BotaniaAPI.manasteelToolMaterial, ItemManasteelSword.MANA_PER_DAMAGE).addBlockInteraction(ItemYoyo::garden));
        registerItem(registry, new ItemElementiumYoyo("elementium_yoyo", BotaniaAPI.elementiumToolMaterial, ItemElementiumSword.MANA_PER_DAMAGE));
        registerItem(registry, new ItemTerraYoyo("terrasteel_yoyo", BotaniaAPI.terrasteelToolMaterial, 100));

        BotaniaAPI.registerManaInfusionRecipe(new ItemStack(mana_cord), new ItemStack(Yoyos.CORD), 5000 * 8);
    }

    @Optional.Method(modid = "botania")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void loadBotaniaRecipes(RegistryEvent.Register<IRecipe> event)
    {
        if (ModConfig.botaniaYoyos.enable)
            event.getRegistry().register(new TerraYoyoLiningRecipe().setRegistryName(Yoyos.MODID, "terra_lining"));
    }

    public static void initBotania()
    {
        actuallyInitBotania();
    }

    @Optional.Method(modid = "botania")
    private static void actuallyInitBotania()
    {
        LexiconEntry terraBlade = new YoyoLexiconEntry("terra_chaser", BotaniaAPI.categoryTools);
        terraBlade.setLexiconPages(new PageText("0"), new PageCraftingRecipe("1", new ResourceLocation(Yoyos.MODID, "terrasteel_yoyo")));

        if (ModConfig.botaniaYoyos.lexiconHackery)
        {
            hackPagesIntoExistingEntries();
        }
        else
        {
            LexiconEntry manasteelYoyo = new YoyoLexiconEntry("mana_yoyo", BotaniaAPI.categoryTools);
            manasteelYoyo.setLexiconPages(new PageText("0"), new PageCraftingRecipe("1", new ResourceLocation(Yoyos.MODID, "manasteel_yoyo")));

            LexiconEntry elementiumYoyo = new YoyoLexiconEntry("elf_yoyo", BotaniaAPI.categoryTools).setKnowledgeType(BotaniaAPI.elvenKnowledge);
            elementiumYoyo.setLexiconPages(new PageText("0"), new PageCraftingRecipe("1", new ResourceLocation(Yoyos.MODID, "elementium_yoyo")));
        }
    }

    @Optional.Method(modid = "botania")
    private static void hackPagesIntoExistingEntries()
    {
        // I'm so sorry

        List<LexiconPage> manasteel = LexiconData.manasteelGear.pages;

        for (int i = 0; i < manasteel.size(); i++)
        {
            LexiconPage page = manasteel.get(i);

            if ("botania.page.manaGear3".equals(page.unlocalizedName))
            {
                ResourceLocation recipeLocation = new ResourceLocation(Yoyos.MODID, "manasteel_yoyo");
                manasteel.add(i + 1, new PageCraftingRecipe("yoyos.lexicon.page.mana_yoyo1", recipeLocation));

                IRecipe iRecipe = ForgeRegistries.RECIPES.getValue(recipeLocation);

                if (iRecipe != null)
                    LexiconRecipeMappings.map(iRecipe.getRecipeOutput(), LexiconData.manasteelGear, i + 1);
                break;
            }
        }

        List<LexiconPage> elementium = LexiconData.elfGear.pages;

        for (int i = 0; i < elementium.size(); i++)
        {
            LexiconPage page = elementium.get(i);

            if ("botania.page.elfGear7".equals(page.unlocalizedName))
            {
                elementium.add(i + 1, new PageText("yoyos.lexicon.page.elf_yoyo00"));

                ResourceLocation recipeLocation = new ResourceLocation(Yoyos.MODID, "elementium_yoyo");
                elementium.add(i + 2, new PageCraftingRecipe("yoyos.lexicon.page.elf_yoyo1", recipeLocation));

                IRecipe iRecipe = ForgeRegistries.RECIPES.getValue(recipeLocation);

                if (iRecipe != null)
                    LexiconRecipeMappings.map(iRecipe.getRecipeOutput(), LexiconData.elfGear, i + 2);

                break;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event)
    {
        compatItems.forEach(ClientProxy::registerModel);
    }
}
