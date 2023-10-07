package com.jozufozu.yoyos.infrastructure.register;

import java.util.Collection;

import com.jozufozu.yoyos.infrastructure.types.Pair;

public class DataGenComponent {
    private Pair<String, String> primaryLang;

    public void setLang(String key, String value) {
        primaryLang = new Pair<>(key, value);
    }

    public void _collectLang(Collection<Pair<String, String>> out) {
        if (primaryLang != null) {
            out.add(primaryLang);
        }
    }
}
