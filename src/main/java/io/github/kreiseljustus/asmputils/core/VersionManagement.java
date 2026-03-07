package io.github.kreiseljustus.asmputils.core;

import io.github.kreiseljustus.asmputils.Asmputils;
import io.github.kreiseljustus.asmputils.Constants;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//This has to be reworked to treat the shop sending as an extra "module" and only disable that
//when breaking changes happened

public class VersionManagement {
    public static boolean s_UsingLatestVersion;
    private static boolean s_WarningGiven = false;

    public static void checkAndWarnVersion(PlayerEntity player) {
        if(!isOldVersion()) {s_UsingLatestVersion = true; Utils.debug("Using latest version!"); return;}
        if(s_WarningGiven) return;
        player.sendMessage(Text.of(Text.literal("[ASMP Utils] Your version is outdated! You won't contribute any data until the mod is updated.")
                .formatted(Formatting.RED)), false);

        s_UsingLatestVersion = false;
        s_WarningGiven = true;
    }

    public static boolean isOldVersion() {
        try {
            String versionString = fetchVersionFromUrl(Constants.VERSION_URL);

            if(versionString == null || versionString.isEmpty()) {
                Utils.debug("Failed to retrieve latest version. Please report this");
                return false;
            }

            int[] latestVersionParts = parseVersion(versionString);
            int[] currentParts = parseVersion(Constants.VERSION);

            for(int i = 0; i < latestVersionParts.length; i++) {
                Utils.debug("Remote: " + String.valueOf(latestVersionParts[i]) + " Current:" + String.valueOf(currentParts[i]));
                if(latestVersionParts[i] > currentParts[i]) {
                    return true;
                } else if(latestVersionParts[i] < currentParts[i]) {
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse asmpshopget version. Make sure youre up-to date");
        }
    }

    private static String fetchVersionFromUrl(String versionUrl) throws Exception {
        try {
            URL url = new URL(versionUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String version = reader.readLine();
                Utils.debug("Received remote version string: " + version);
                return version;
            }
        } catch(Exception e) {
            Utils.debug("Failed to fetch version! Please report this to Crisel on discord");
            return null;
        }
    }

    private static int[] parseVersion(String version) {
        Matcher matcher = Pattern.compile("\\d+").matcher(version);
        int[] parts = new int[3];
        int index = 0;

        while(matcher.find() && index < 3) {
            parts[index++] = Integer.parseInt(matcher.group());
        }
        return parts;
    }
}
