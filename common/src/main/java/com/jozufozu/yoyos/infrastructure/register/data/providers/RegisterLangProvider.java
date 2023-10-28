package com.jozufozu.yoyos.infrastructure.register.data.providers;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.jozufozu.yoyos.infrastructure.register.Register;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class RegisterLangProvider extends LanguageProvider {
    private final Register register;

    public RegisterLangProvider(Register register, PackOutput packOutput) {
        super(packOutput, register.modId, "en_us");
        this.register = register;
    }

    public static String toEnglishName(ResourceLocation name) {
        return toEnglishName(name.getPath());
    }

    public static String toEnglishName(String internalName) {
        return Arrays.stream(internalName.toLowerCase(Locale.ROOT).split("_"))
            .map(StringUtils::capitalize)
            .collect(Collectors.joining(" "));
    }

    public <R, T extends R> String getAutomaticName(Register.Promise<R, T> sup) {
        return toEnglishName(sup.name.location());
    }

    @Override
    protected void addTranslations() {
        register.runData(ProviderType.LANG, this);
    }
}
