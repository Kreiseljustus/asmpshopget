package io.github.kreiseljustus.asmputils.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.kreiseljustus.asmputils.Asmputils;
import io.github.kreiseljustus.asmputils.config.Constants;
import net.minecraft.client.MinecraftClient;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class ModrinthVersionManagement {
    private static final String API_URL = "https://api.modrinth.com/v2/project/" + Constants.PROJECT_ID + "/version";

    public static String latestVersion = null;
    public static String downloadUrl = null;
    public static String downloadFilename = null;
    public static boolean updateAvailable = false;
    public static String changeLog = "";

    public static CompletableFuture<Boolean> checkUpdateAvailable() {
        return getLatestVersion().thenApply(success -> {
            if (!success) return false;
            return !latestVersion.equals(Constants.VERSION);
        });
    }

    private static CompletableFuture<Boolean> getLatestVersion() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = API_URL + "?game_versions=fabric";

                HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection();
                conn.setRequestProperty("User-Agent", "kreiseljustus/asmputils/" + Constants.VERSION);
                String json = new String(conn.getInputStream().readAllBytes());

                JsonArray versions = JsonParser.parseString(json).getAsJsonArray();
                if (versions.isEmpty()) return false;

                JsonObject latest = versions.get(0).getAsJsonObject();
                latestVersion = latest.get("version_number").getAsString();

                JsonArray files = latest.getAsJsonArray("files");
                for (JsonElement file : files) {
                    JsonObject fileObj = file.getAsJsonObject();
                    if (fileObj.get("primary").getAsBoolean()) {
                        downloadUrl = fileObj.get("url").getAsString();
                        downloadFilename = fileObj.get("filename").getAsString();
                        break;
                    }
                }
                changeLog = latest.get("changelog").getAsString();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Utils.debug("Failed to getLatestVersion from Modrinth");
                return false;
            }
        });
    }
}
