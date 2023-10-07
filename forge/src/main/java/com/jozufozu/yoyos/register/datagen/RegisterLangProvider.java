package com.jozufozu.yoyos.register.datagen;

import com.jozufozu.yoyos.infrastructure.register.Register;
import com.jozufozu.yoyos.infrastructure.types.Pair;

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
        for (Pair<String, String> langEntry : register.getLangEntries()) {
            add(langEntry.first(), langEntry.second());
        }
    }
}
