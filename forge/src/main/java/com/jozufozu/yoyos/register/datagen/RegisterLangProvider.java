package com.jozufozu.yoyos.register.datagen;

import com.jozufozu.yoyos.infrastructure.register.Register;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class RegisterLangProvider extends LanguageProvider {
    private final Register register;

    public RegisterLangProvider(Register register, PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.register = register;
    }

    @Override
    protected void addTranslations() {
        register.collectLang(langEntry -> add(langEntry.first(), langEntry.second()));
    }
}
