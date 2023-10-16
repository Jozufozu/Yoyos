package com.jozufozu.yoyos.infrastructure.util;

import java.text.DecimalFormat;

import org.joml.Vector3d;

public class DebugRenderers {

    public static String vector3d(Vector3d self) {
        var d3 = new DecimalFormat("#.0##");
        return "(%s, %s, %s) - |%s|".formatted(d3.format(self.x), d3.format(self.y), d3.format(self.z), d3.format(self.length()));
    }
}
