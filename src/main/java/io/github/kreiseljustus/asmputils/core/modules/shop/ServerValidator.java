package io.github.kreiseljustus.asmputils.core.modules.shop;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import io.github.kreiseljustus.asmputils.Asmputils;
import io.github.kreiseljustus.asmputils.config.ModConfig;
import io.github.kreiseljustus.asmputils.core.Utils;
import io.github.kreiseljustus.asmputils.core.data.ShopDataHolder;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages HTTP requests to receive known shops
 */
public class ServerValidator {

    //Put in Utils?
    static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                    (json, typeOfT, context) -> {
                        String str = json.getAsString();
                        if (str.endsWith("Z")) {
                            return OffsetDateTime.parse(str).toLocalDateTime();
                        }
                        return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS]"));
                    })
            .create();

    //Might move this?
    public static List<ShopDataHolder> s_ServerShops = new ArrayList<>();
    public static boolean forceRefresh = false;

    /**
     *
     * @param chunkX chunkX coordinate
     * @param chunkZ chunkZ coordinate
     * @param dimension dimension id
     * @return List of {@code ShopDataHolder} that are currently saved on the server but might have been removed
     * since the last sent shop update
     */
    protected static List<ShopDataHolder> getExpectedShopsInChunk(int chunkX, int chunkZ, int dimension) {
        List<ShopDataHolder> result = new ArrayList<>();

        if(s_ServerShops == null || s_ServerShops.isEmpty()) return result;

        for(ShopDataHolder shop : s_ServerShops) {
            int[] pos = shop.position;
            if(pos == null || pos.length < 3) continue;

            int shopChunkX = pos[0] >> 4;
            int shopChunkZ = pos[2] >> 4;

            if(shopChunkX == chunkX && shopChunkZ == chunkZ && shop.dimension == dimension) {
                result.add(shop);
            }
        }
        return result;
    }

    /**
     *
     * @return thread that tries to receive the current server data every {@code s_Config.fetcherThreadInterval} milliseconds
     */
    public static @NotNull Thread getFetcherThread() {
        Thread fetcherThread = new Thread(() -> {
            while (true) {
                try {
                    if(!ModConfig.get().enable) Thread.sleep(Asmputils.s_Config.fetcherThreadInterval);
                    ServerValidator.getServerData();
                } catch (Exception e) {
                    Utils.debug("This will crash minecraft");
                    e.printStackTrace();
                }

                try {
                    long waited = 0;
                    long interval = Asmputils.s_Config.fetcherThreadInterval;
                    while (waited < interval && !forceRefresh) {
                        Thread.sleep(100);
                        waited += 100;
                    }
                    forceRefresh = false;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        fetcherThread.setDaemon(true);
        return fetcherThread;
    }

    /**
     * Downloads the servers shops and saves them in {@code s_ServerShops}
     */
    private static void getServerData() {
        String shopJson = downloadUrl(Asmputils.s_Config.shopRoute);

        Type shopListType = new TypeToken<List<ShopDataHolder>>() {}.getType();
        s_ServerShops = gson.fromJson(shopJson, shopListType);
    }

    /**
     *
     * @param urlString url
     * @return String downloaded from url
     */
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
