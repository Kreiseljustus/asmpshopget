package io.github.kreiseljustus.asmputils.core.modules.shop;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.github.kreiseljustus.asmputils.Asmputils;
import io.github.kreiseljustus.asmputils.ModConfig;
import io.github.kreiseljustus.asmputils.core.Utils;
import io.github.kreiseljustus.asmputils.core.data.ShopDataHolder;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//needs documentation
public class ServerValidator {

    private static final Gson gson = new Gson();

    private static List<ShopDataHolder> s_ServerShops = new ArrayList<>();

    public static List<ShopDataHolder> getExpectedShopsInChunk(int chunkX, int chunkZ) {
        List<ShopDataHolder> result = new ArrayList<>();

        if(s_ServerShops == null || s_ServerShops.isEmpty()) return result;

        for(ShopDataHolder shop : s_ServerShops) {
            int[] pos = shop.position;
            if(pos == null || pos.length < 3) continue;

            int shopChunkX = pos[0] >> 4;
            int shopChunkZ = pos[2] >> 4;

            if(shopChunkX == chunkX && shopChunkZ == chunkZ) {
                result.add(shop);
            }
        }
        return result;
    }

    public static @NotNull Thread getFetcherThread() {
        Thread fetcherThread = new Thread(() -> {
            while (true) {
                try {
                    if(!ModConfig.get().enable) Thread.sleep(Asmputils.s_Config.fetcherThreadInterval);
                    ServerValidator.getServerData();
                } catch (Exception e) {
                    Utils.debug("This will crash minecraft");
                }

                try {
                    Thread.sleep(Asmputils.s_Config.fetcherThreadInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        fetcherThread.setDaemon(true);
        return fetcherThread;
    }

    public static void getServerData() {
        String shopJson = downloadUrl(Asmputils.s_Config.shopRoute);

        Type shopListType = new TypeToken<List<ShopDataHolder>>() {}.getType();
        s_ServerShops = gson.fromJson(shopJson, shopListType);
    }

    private static String downloadUrl(String urlString) {
        try {
            URL url = new URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            return reader.lines().collect(Collectors.joining());
        } catch (Exception e) {
            Utils.debug(e.getMessage());
        }

        return null;
    }
}
