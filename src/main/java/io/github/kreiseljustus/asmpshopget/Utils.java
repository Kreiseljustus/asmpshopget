package io.github.kreiseljustus.asmpshopget;

import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Utils {
    public static boolean onASMP() {
        MinecraftClient client = MinecraftClient.getInstance();

        ServerInfo server = client.getCurrentServerEntry();
        if(server == null) return false;
        return server.address.equals("asmp.cc");
    }

    public static void debug(String message) {
        if(!Asmpshopget.s_Config.enableDebugMode || !Asmpshopget.s_Config.enable) return;
        Asmpshopget.s_Player.sendMessage(Text.of(message), false);
    }
}
