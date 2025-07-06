package io.github.kreiseljustus.asmpshopget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Utils {
    public static boolean onASMP() {
        MinecraftClient client = MinecraftClient.getInstance();

        ServerInfo server = client.getCurrentServerEntry();
        if(server == null) return false;
        return server.address.equals("asmp.cc");
    }

    private static String fetchVersionFromUrl(String versionUrl) throws Exception {
        URL url = new URL(versionUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.readLine();
        }
    }
}
