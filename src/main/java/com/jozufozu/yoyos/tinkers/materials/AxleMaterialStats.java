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
import slimeknights.tconstruct.library.materials.HandleMaterialStats;

import javax.annotation.Nullable;
import java.util.List;

public class AxleMaterialStats extends AbstractMaterialStats
{
    public static final String LOC_Friction = "stat.axle.friction.name";
    
    public static final String LOC_FrictionDesc = "stat.axle.friction.desc";
    
    public static final String COLOR_Friction = CustomFontColor.encodeColor(74, 232, 128);
    
    public final float friction;
    public final float modifier;
    
    public AxleMaterialStats(float friction, float modifier)
    {
        super(YoyoMaterialTypes.AXLE);
        this.friction = friction;
        this.modifier = modifier;
    }
    
    @Override
    public List<String> getLocalizedInfo()
    {
        return ImmutableList.of(formatFriction(friction), HandleMaterialStats.formatModifier(modifier));
    }
    
    @Override
    public List<String> getLocalizedDesc()
    {
        return ImmutableList.of(Util.translate(LOC_FrictionDesc), Util.translate(HandleMaterialStats.LOC_MultiplierDesc));
    }
    
    public static String formatFriction(float friction)
    {
        return formatNumber(LOC_Friction, COLOR_Friction, friction);
    }

    @Nullable
    public static AxleMaterialStats deserialize(JsonObject material) throws JsonParseException
    {
        if (!JsonUtils.hasField(material, "axle"))
            return null;

        JsonObject axle = JsonUtils.getJsonObject(material, "axle");

        float friction = JsonUtils.getFloat(axle, "friction");
        float modifier = JsonUtils.getFloat(axle, "modifier");

        return new AxleMaterialStats(friction, modifier);
    }
}
