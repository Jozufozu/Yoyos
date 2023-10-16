package com.jozufozu.yoyos.infrastructure.util;

import java.text.DecimalFormat;

import org.joml.Vector3d;

/**
 * Formatting functions for use in IntelliJ's debugger.
 */
public class DebugRenderers {

    private static final DecimalFormat D3 = new DecimalFormat("#.0##");

    public static String vector3d(Vector3d self) {
        return "(%s, %s, %s) - |%s|".formatted(D3.format(self.x), D3.format(self.y), D3.format(self.z), D3.format(self.length()));
    }
}
