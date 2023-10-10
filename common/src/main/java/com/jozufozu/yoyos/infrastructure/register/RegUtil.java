package com.jozufozu.yoyos.infrastructure.register;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.resources.ResourceLocation;

public class RegUtil {
    public static String toEnglishName(ResourceLocation name) {
        return toEnglishName(name.getPath());
    }

    public static String toEnglishName(String internalName) {
        return Arrays.stream(internalName.toLowerCase(Locale.ROOT).split("_"))
            .map(StringUtils::capitalize)
            .collect(Collectors.joining(" "));
    }
}
