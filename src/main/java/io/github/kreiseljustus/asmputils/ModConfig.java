package io.github.kreiseljustus.asmputils;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "asmpshopget")
public class ModConfig implements ConfigData {
    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.Tooltip
    public boolean enable = true;
    @ConfigEntry.Category("Tracking")
    @ConfigEntry.Gui.Tooltip
    public boolean trackShops = true;
    @ConfigEntry.Category("Tracking")
    @ConfigEntry.Gui.Tooltip
    public boolean trackWaystones = true;
    @ConfigEntry.Category("General")
    public String postUrl = "https://asmp.mia.jetzt/api/post";
    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.Tooltip
    public int ticksBetweenSends = 600;
    @ConfigEntry.Category("Dev")
    public int fetcherThreadInterval = 15 * 60 * 1000;

    public static enum WaypointColorOption {
        BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, PURPLE, YELLOW, WHITE
    }

    public static enum WaypointTypeOption {
        NORMAL, DESTINATION
    }

    @ConfigEntry.Category("Modules")
    @ConfigEntry.Gui.Tooltip
    public boolean enableShopModule = true;
    @ConfigEntry.Category("Modules")
    @ConfigEntry.Gui.Tooltip
    public boolean enableWaystoneModule = true;

    @ConfigEntry.Category("Modules")
    @ConfigEntry.Gui.Tooltip
    public boolean enableCommandsModule = true;

    @ConfigEntry.Category("Modules")
    @ConfigEntry.Gui.Tooltip
    public boolean enableWaypointModule = true;

    @ConfigEntry.Category("Waypoints")
    @ConfigEntry.Gui.Tooltip
    public WaypointColorOption waypointColor = WaypointColorOption.GOLD;

    @ConfigEntry.Category("Waypoints")
    @ConfigEntry.Gui.Tooltip
    public String waypointInitial = "💰";

    @ConfigEntry.Category("Waypoints")
    @ConfigEntry.Gui.Tooltip
    public WaypointTypeOption waypointType = WaypointTypeOption.DESTINATION;

    @ConfigEntry.Category("Waypoints")
    @ConfigEntry.Gui.Tooltip
    public boolean waypointTemporary = true;

    @ConfigEntry.Category("Dev")
    public boolean enableDebugMode = false;
    @ConfigEntry.Category("Dev")
    public boolean allowOnAllServers = false;
    @ConfigEntry.Category("Dev")
    public String shopRoute = "https://asmp.mia.jetzt/api/shops";
    @ConfigEntry.Category("Dev")
    public String deleteRoute = "https://asmp.mia.jetzt/api/delete";


    public static void register() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }

    public static ModConfig get() {return AutoConfig.getConfigHolder(ModConfig.class).getConfig();}
}
