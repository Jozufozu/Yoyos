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
import slimeknights.tconstruct.library.materials.HeadMaterialStats;

import javax.annotation.Nullable;
import java.util.List;

public class BodyMaterialStats extends AbstractMaterialStats
{
    
    public static final String LOC_Weight = "stat.body.weight.name";
    public static final String LOC_WeightDesc = "stat.body.weight.desc";
    
    public static final String COLOR_Weight = CustomFontColor.encodeColor(0, 255, 217);
    
    public final float attack;
    public final float weight;
    public final int durability;
    
    public BodyMaterialStats(float attack, float weight, int durability)
    {
        super(YoyoMaterialTypes.BODY);
        this.attack = attack;
        this.weight = weight;
        this.durability = durability;
    }
    
    @Override
    public List<String> getLocalizedInfo()
    {
        return ImmutableList.of(HeadMaterialStats.formatAttack(this.attack), formatWeight(this.weight), HeadMaterialStats.formatDurability(this.durability));
    }
    
    @Override
    public List<String> getLocalizedDesc()
    {
        return ImmutableList.of(Util.translate(HeadMaterialStats.LOC_AttackDesc), Util.translate(LOC_WeightDesc), Util.translate(HeadMaterialStats.LOC_DurabilityDesc));
    }
    
    public static String formatWeight(float weight)
    {
        return formatNumber(LOC_Weight, COLOR_Weight, weight);
    }

    @Nullable
    public static BodyMaterialStats deserialize(JsonObject material) throws JsonParseException
    {
        if (!JsonUtils.hasField(material, "body"))
            return null;

        JsonObject body = JsonUtils.getJsonObject(material, "body");

        float attack = JsonUtils.getFloat(body, "entityInteraction");
        float weight = JsonUtils.getFloat(body, "weight");
        int durability = JsonUtils.getInt(body, "durability");

        return new BodyMaterialStats(attack, weight, durability);
    }
}
