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
