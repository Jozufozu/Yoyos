package com.jozufozu.yoyos.common.materials;

import com.google.common.collect.ImmutableList;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.CustomFontColor;
import slimeknights.tconstruct.library.materials.AbstractMaterialStats;

import java.util.List;

public class CordMaterialStats extends AbstractMaterialStats {
    public static final String LOC_Length = "stat.cord.length.name";

    public static final String LOC_LengthDesc = "stat.cord.length.desc";

    public static final String COLOR_Length = CustomFontColor.encodeColor(11, 60, 232);

    public final float friction;
    public final float length;

    public CordMaterialStats(float friction, float length) {
        super(YoyoMaterialTypes.CORD);
        this.friction = friction;
        this.length = length;
    }

    @Override
    public List<String> getLocalizedInfo() {
        return ImmutableList.of(formatFriction(friction), formatLength(length));
    }

    @Override
    public List<String> getLocalizedDesc() {
        return ImmutableList.of(Util.translate(AxleMaterialStats.LOC_FrictionDesc), Util.translate(LOC_LengthDesc));
    }

    public static String formatFriction(float friction) {
        return formatNumber(AxleMaterialStats.LOC_Friction, AxleMaterialStats.COLOR_Friction, friction);
    }

    public static String formatLength(float length) {
        return formatNumber(LOC_Length, COLOR_Length, length);
    }
}
