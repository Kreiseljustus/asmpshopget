package io.github.kreiseljustus.asmpshopget;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "asmpshopget")
public class ModConfig implements ConfigData {
    @ConfigEntry.Category("General")
    public boolean enable = true;
    @ConfigEntry.Category("Tracking")
    public boolean trackShops = true;
    @ConfigEntry.Category("Tracking")
    public boolean trackWaystones = true;
    @ConfigEntry.Category("General")
    public String postUrl = "https://kreiseljustus.com/asmp/post";
    @ConfigEntry.Category("General")
    public int ticksBetweenSends = 600;

    @ConfigEntry.Category("Dev")
    public boolean enableDebugMode = false;
    @ConfigEntry.Category("Dev")
    public boolean allowOnAllServers = false;

    public static void register() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }

    public static ModConfig get() {return AutoConfig.getConfigHolder(ModConfig.class).getConfig();}
}
