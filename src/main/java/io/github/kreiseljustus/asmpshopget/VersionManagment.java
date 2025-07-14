package io.github.kreiseljustus.asmpshopget;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionManagment {
    public static boolean s_UsingLatestVersion;
    private static boolean s_WarningGiven = false;

    public static void checkAndWarnVersion(PlayerEntity player) {
        if(!isOldVersion()) {s_UsingLatestVersion = true; Utils.debug("Using latest version!"); return;}
        if(s_WarningGiven) return;
        player.sendMessage(Text.of("Your version is outdated! You wont contribute any data until the mod is updated."), false);

        s_UsingLatestVersion = false;
        s_WarningGiven = true;
    }

    public static boolean isOldVersion() {
        try {
            int[] latestVersionParts = parseVersion(fetchVersionFromUrl(Asmpshopget.VERSION_URL));
            int[] currentParts = parseVersion(Asmpshopget.VERSION);

            for(int i = 0; i < latestVersionParts.length; i++) {
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
        URL url = new URL(versionUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.readLine();
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
