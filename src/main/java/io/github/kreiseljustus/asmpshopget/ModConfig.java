package io.github.kreiseljustus.asmpshopget;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "asmpshopget")
public class ModConfig implements ConfigData {
    public boolean enable = true;
    public boolean enableDebugMode = false;
    public String postUrl = "https://kreiseljustus.com/asmp/post";
    public int ticksBetweenSends = 600;
    public boolean createNewThreadPerSend = false;

    public boolean allowOnAllServers = false;

    public static void register() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }

    public static ModConfig get() {return AutoConfig.getConfigHolder(ModConfig.class).getConfig();}
}
