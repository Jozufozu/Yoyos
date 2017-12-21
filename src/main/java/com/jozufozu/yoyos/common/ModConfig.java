package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.Yoyos;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Yoyos.MODID)
@Mod.EventBusSubscriber(modid = Yoyos.MODID)
public class ModConfig
{
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (Yoyos.MODID.equals(event.getModID()))
        {
            ConfigManager.sync(Yoyos.MODID, Config.Type.INSTANCE);
        }
    }
    
    @Config.LangKey("yoyos.config.swing")
    @Config.Comment("Allows you to swing from yoyos when they get caught")
    public static boolean yoyoSwing = true;
    
    @Config.LangKey("yoyos.config.vanilla")
    public static VanillaYoyos vanillaYoyos = new VanillaYoyos();
    
    @Config.LangKey("yoyos.config.tinkers")
    @Config.Comment("If enabled and Tinkers' Construct is installed, adds customizable yoyos")
    @Config.RequiresMcRestart
    public static boolean tinkersYoyos = true;
    
    @Config.LangKey("yoyos.config.materials")
    @Config.Comment({"If enabled, saves materials into a config folder.", "Allows for editing, addition, and removal of stats for yoyos."})
    @Config.RequiresMcRestart
    public static boolean configMaterials = false;
    
    public static class VanillaYoyos
    {
        @Config.LangKey("yoyos.config.enable")
        @Config.Comment("Whether or not the default yoyos are added")
        @Config.RequiresMcRestart
        public boolean enable = true;
        
        @Config.LangKey("item.yoyos.wooden_yoyo.name")
        public YoyoSettings woodenYoyo = new YoyoSettings(2.2f, 6.0f, 100, 3.0f);
        @Config.LangKey("item.yoyos.stone_yoyo.name")
        public YoyoSettings stoneYoyo = new YoyoSettings(4.0f, 7.0f, 200, 4.0f);
        @Config.LangKey("item.yoyos.iron_yoyo.name")
        public YoyoSettings ironYoyo = new YoyoSettings(5.0f, 8.0f, 300, 5.0f);
        @Config.LangKey("item.yoyos.shear_yoyo.name")
        public YoyoSettings shearYoyo = new YoyoSettings(5.1f, 8.0f, 300, 5.5f);
        @Config.LangKey("item.yoyos.gold_yoyo.name")
        public YoyoSettings goldYoyo = new YoyoSettings(5.5f, 11.0f, 600, 3.0f);
        @Config.LangKey("item.yoyos.diamond_yoyo.name")
        public YoyoSettings diamondYoyo = new YoyoSettings(1.7f, 9.0f, 400, 6.0f);
        @Config.LangKey("item.yoyos.sticky_yoyo.name")
        public YoyoSettings stickyYoyo = new YoyoSettings(1.7f, 9.0f, 400, 0.0f);
        @Config.LangKey("item.yoyos.creative_yoyo.name")
        public YoyoSettings creativeYoyo = new YoyoSettings(0.9f, 24.0f, -1, 9001.0f);
        
        public static class YoyoSettings
        {
            @Config.LangKey("yoyos.config.weight")
            @Config.Comment("Affects how fast the yoyo moves")
            @Config.RangeDouble(min = 0)
            public float weight;
    
            @Config.LangKey("yoyos.config.length")
            @Config.Comment("How far away the yoyo can get (in blocks)")
            @Config.RangeDouble(min = 0)
            public float length;
    
            @Config.LangKey("yoyos.config.duration")
            @Config.Comment("How long the yoyo can stay out (in ticks)")
            @Config.RangeInt(min = 0)
            public int duration;
    
            @Config.LangKey("yoyos.config.damage")
            @Config.Comment("How much damage the yoyo does (in hearts)")
            @Config.RangeDouble(min = 0)
            public float damage;
    
            public YoyoSettings(float weight, float length, int duration, float damage)
            {
                this.weight = weight;
                this.length = length;
                this.duration = duration;
                this.damage = damage;
            }
        }
    }
}
