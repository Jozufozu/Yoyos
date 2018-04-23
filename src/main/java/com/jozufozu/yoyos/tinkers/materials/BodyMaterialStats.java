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

        float attack = JsonUtils.getFloat(body, "attack");
        float weight = JsonUtils.getFloat(body, "weight");
        int durability = JsonUtils.getInt(body, "durability");

        return new BodyMaterialStats(attack, weight, durability);
    }
}
