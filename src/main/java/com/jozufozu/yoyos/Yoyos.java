package com.jozufozu.yoyos;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import slimeknights.mantle.pulsar.config.ForgeCFG;
import slimeknights.mantle.pulsar.control.PulseManager;

@Mod(name = Yoyos.NAME, modid = Yoyos.MODID, version = Yoyos.VERSION, dependencies = "required-after:tconstruct")
public class Yoyos {

    @Mod.Instance(value = Yoyos.MODID)
    public static Yoyos INSTANCE;

    public static final String MODID = "yoyos";
    public static final String NAME = "Yoyos";
    public static final String VERSION = "@VERSION@";

    public static ForgeCFG pulseConfig = new ForgeCFG("TinkerYoyos", "Yoyos");
    public static PulseManager pulsar = new PulseManager(pulseConfig);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        pulsar.registerPulse(new TinkersYoyos());
    }
}
