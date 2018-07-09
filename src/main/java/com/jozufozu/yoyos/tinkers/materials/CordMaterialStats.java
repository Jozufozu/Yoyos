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

package com.jozufozu.yoyos.tinkers.materials;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.JsonUtils;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.CustomFontColor;
import slimeknights.tconstruct.library.materials.AbstractMaterialStats;

import javax.annotation.Nullable;
import java.util.List;

public class CordMaterialStats extends AbstractMaterialStats
{
    public static final String LOC_Length = "stat.cord.length.name";
    
    public static final String LOC_LengthDesc = "stat.cord.length.desc";
    
    public static final String COLOR_Length = CustomFontColor.encodeColor(11, 60, 232);
    
    public final float friction;
    public final float length;
    
    public CordMaterialStats(float friction, float length)
    {
        super(YoyoMaterialTypes.CORD);
        this.friction = friction;
        this.length = length;
    }
    
    @Override
    public List<String> getLocalizedInfo()
    {
        return ImmutableList.of(AxleMaterialStats.formatFriction(friction), formatLength(length));
    }
    
    @Override
    public List<String> getLocalizedDesc()
    {
        return ImmutableList.of(Util.translate(AxleMaterialStats.LOC_FrictionDesc), Util.translate(LOC_LengthDesc));
    }
    
    public static String formatLength(float length)
    {
        return formatNumber(LOC_Length, COLOR_Length, length);
    }

    @Nullable
    public static CordMaterialStats deserialize(JsonObject material) throws JsonParseException
    {
        if (!JsonUtils.hasField(material, "cord"))
            return null;

        JsonObject cord = JsonUtils.getJsonObject(material, "cord");

        float friction = JsonUtils.getFloat(cord, "friction");
        float length = JsonUtils.getFloat(cord, "length");

        return new CordMaterialStats(friction, length);
    }
}
