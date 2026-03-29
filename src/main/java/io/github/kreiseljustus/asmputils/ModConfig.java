package io.github.kreiseljustus.asmputils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.kreiseljustus.asmputils.core.Utils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

@Config(name = "asmpshopget")
public class ModConfig implements ConfigData {
    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.Excluded
    public int configVersion = 1;
    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.Excluded
    public static final int CURRENT_CONFIG_VERSION = 2;

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

    @ConfigEntry.Category("Modules")
    @ConfigEntry.Gui.Tooltip
    public boolean enableWaterModule = false;

    @ConfigEntry.Category("Aprilfools")
    public float extraWaterHeight = 1.0f;

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

    public static ModConfig get() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static void validateAndUpdate() {
        var holder = AutoConfig.getConfigHolder(ModConfig.class);
        ModConfig config = holder.getConfig();

        //Dont change!!!
        String configUrl = "https://raw.githubusercontent.com/Kreiseljustus/asmp-utils/refs/heads/CONFIG/CONFIG.json";

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new URI(configUrl).toURL().openStream())
            );

            StringBuilder jsonText = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                jsonText.append(line);
            }

            Utils.debug(jsonText.toString());

            JsonObject json = JsonParser.parseString(jsonText.toString()).getAsJsonObject();

            int remoteConfigVersion = json.get("version").getAsInt();

            if(config.configVersion >= remoteConfigVersion) return;

            config.postUrl = json.get("postUrl").getAsString();
            config.shopRoute = json.get("shopRoute").getAsString();
            config.deleteRoute = json.get("deleteRoute").getAsString();

            config.configVersion = remoteConfigVersion;
            holder.save();

            Utils.debug("Saved config: " + json);

        } catch (Exception e) {
            Utils.debug("Something went wrong while trying to get the current config!");
            e.printStackTrace();
        }
    }
}
