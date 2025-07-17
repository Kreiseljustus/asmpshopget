package io.github.kreiseljustus.asmpshopget;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServerValidator {

    private static final Gson gson = new Gson();

    private static List<ShopDataHolder> s_ServerShops = new ArrayList<>();
    private static List<WaystoneDataHolder> s_ServerWaystones = new ArrayList<>();


    //We love duplicating code
    public static List<WaystoneDataHolder> getExpectedWaystonesInChunk(int chunkX, int chunkZ) {
        List<WaystoneDataHolder> result = new ArrayList<>();

        if(s_ServerShops.isEmpty()) return result;

        for(WaystoneDataHolder waystone : s_ServerWaystones) {
            int[] pos = waystone.position;
            if(pos == null || pos.length < 3) continue;

            int wChunkX = pos[0] >> 4;
            int wChunkZ = pos[2] >> 4;

            if(wChunkX == chunkX && wChunkZ == chunkZ) {
                result.add(waystone);
            }
        }
        return result;
    }

    public static List<ShopDataHolder> getExpectedShopsInChunk(int chunkX, int chunkZ) {
        List<ShopDataHolder> result = new ArrayList<>();

        if(s_ServerShops.isEmpty()) return result;

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

    public static void getServerData() {
        String shopJson = downloadUrl(Asmpshopget.s_Config.shopRoute);
        String waystoneJson = downloadUrl(Asmpshopget.s_Config.waystoneRoute);

        Type shopListType = new TypeToken<List<ShopDataHolder>>() {}.getType();
        s_ServerShops = gson.fromJson(shopJson, shopListType);

        Type waystoneListType = new TypeToken<List<WaystoneDataHolder>>() {}.getType();
        s_ServerWaystones = gson.fromJson(waystoneJson, waystoneListType);
    }

    private static String downloadUrl(String urlString) {
        try {
            URL url = new URL(urlString);
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
