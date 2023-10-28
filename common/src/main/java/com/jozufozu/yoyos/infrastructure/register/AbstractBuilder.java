package com.jozufozu.yoyos.infrastructure.register;

import java.util.Objects;

import com.jozufozu.yoyos.infrastructure.notnull.NotNullBiFunction;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullConsumer;
import com.jozufozu.yoyos.infrastructure.notnull.NotNullFunction;
import com.jozufozu.yoyos.infrastructure.register.data.DataGen;
import com.jozufozu.yoyos.infrastructure.register.data.providers.ProviderType;
import com.jozufozu.yoyos.infrastructure.register.data.providers.RegisterLangProvider;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractBuilder<R, T extends R, Self extends AbstractBuilder<R, T, Self>> {
    protected final ResourceLocation name;
    protected final RegistrationCallback<R, T> registrationCallback;
    protected final DataGen<R, T> dataGen = new DataGen<>();
    private final ResourceKey<? extends Registry<R>> registryKey;
    protected NotNullConsumer<T> onRegister = NotNullConsumer.noop();

    protected AbstractBuilder(ResourceLocation name, RegistrationCallback<R, T> registrationCallback, ResourceKey<? extends Registry<R>> registryKey) {
        this.name = name;
        this.registrationCallback = registrationCallback;
        this.registryKey = registryKey;
    }

    public Entry<R, T> register() {
        return wrap(registrationCallback.markForRegistration(registryKey, name, this::create, dataGen, onRegister));
    }

    protected abstract Entry<R, T> wrap(Register.Promise<R, T> promise);

    protected abstract T create();

    public Self onRegister(NotNullConsumer<T> onRegister) {
        Objects.requireNonNull(onRegister);
        this.onRegister = this.onRegister.andThen(onRegister);
        return self();
    }

    /**
     * Set the lang key for this entry to the default value (specified by {@link RegisterLangProvider#getAutomaticName(Register.Promise)}).
     * Generally, specific helpers from concrete builders should be used instead.
     *
     * @param langKeyProvider
     *            A function to get the translation key from the entry
     * @return this {@link AbstractBuilder}
     */
    public Self lang(NotNullFunction<T, String> langKeyProvider) {
        return lang(langKeyProvider, RegisterLangProvider::getAutomaticName);
    }

    /**
     * Set the lang key for this entry to the specified name. Generally, specific helpers from concrete builders should be used instead.
     *
     * @param langKeyProvider
     *            A function to get the translation key from the entry
     * @param name
     *            The name to use
     * @return this {@link AbstractBuilder}
     */
    public Self lang(NotNullFunction<T, String> langKeyProvider, String name) {
        return lang(langKeyProvider, (p, s) -> name);
    }

    private Self lang(NotNullFunction<T, String> langKeyProvider, NotNullBiFunction<RegisterLangProvider, Register.Promise<R, T>, String> localizedNameProvider) {
        dataGen.setData(ProviderType.LANG, (ctx, prov) -> prov.add(langKeyProvider.apply(ctx.get()), localizedNameProvider.apply(prov, ctx)));
        return self();
    }

    @SuppressWarnings("unchecked")
    public Self self() {
        return (Self) this;
    }
}
