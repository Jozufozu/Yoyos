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
