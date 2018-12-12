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

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.lexicon.*;
import vazkii.botania.common.lexicon.WelcomeLexiconEntry;

import javax.annotation.Nonnull;

public class YoyoLexiconEntry extends LexiconEntry implements IAddonEntry
{
    public YoyoLexiconEntry(String unlocalizedName, LexiconCategory category)
    {
        super(unlocalizedName, category);
        BotaniaAPI.addEntry(this, category);
    }

    @Override
    public LexiconEntry setLexiconPages(LexiconPage... pages)
    {
        for (LexiconPage page : pages)
        {
            page.unlocalizedName = "yoyos.lexicon.page." + this.getLazyUnlocalizedName() + page.unlocalizedName;

            if (page instanceof ITwoNamedPage)
            {
                ITwoNamedPage dou = (ITwoNamedPage) page;
                dou.setSecondUnlocalizedName("yoyos.lexicon.page." + this.getLazyUnlocalizedName() + dou.getSecondUnlocalizedName());
            }
        }

        return super.setLexiconPages(pages);
    }

    @Override
    public String getUnlocalizedName()
    {
        return "yoyos.lexicon.entry." + super.getUnlocalizedName();
    }

    @Override
    public String getTagline()
    {
        return "yoyos.lexicon.tagline." + super.getUnlocalizedName();
    }

    public String getLazyUnlocalizedName()
    {
        return super.getUnlocalizedName();
    }

    @Override
    public int compareTo(@Nonnull LexiconEntry o)
    {
        return o instanceof WelcomeLexiconEntry ? 1 : super.compareTo(o);
    }

    @Override
    public String getSubtitle()
    {
        return "[Botania x Yoyos]";
    }
}
