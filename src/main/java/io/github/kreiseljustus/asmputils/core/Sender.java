package io.github.kreiseljustus.asmputils.core;

import com.google.gson.Gson;
import io.github.kreiseljustus.asmputils.*;
import io.github.kreiseljustus.asmputils.core.data.ShopDataHolder;
import io.github.kreiseljustus.asmputils.core.data.ShopDataManager;
import io.github.kreiseljustus.asmputils.core.data.ShopWaystoneUploadPacket;
import io.github.kreiseljustus.asmputils.core.data.WaystoneDataHolder;
import io.github.kreiseljustus.asmputils.core.modules.WaystoneModule;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.ArrayList;
import java.util.List;

public class Sender {
    static Gson gson = new Gson();

    public static void sendDeleteRequest(ShopDataHolder shop) {
        sendDeleteRequest(shop, null);
    }

    public static void sendDeleteRequest(ShopDataHolder shop, WaystoneDataHolder waystone) {
        if(shop != null && waystone != null) return;

        String dataJson = shop == null ? gson.toJson(waystone) : gson.toJson(shop);

        String requestBody = String.format("{\"type\":\"shop\",\"data\":%s}", dataJson);

        HttpPost post = new HttpPost(Asmputils.s_Config.deleteRoute);
        new Thread(() -> {
            try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
                StringEntity postString = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
                post.setEntity(postString);
                post.setHeader("Content-Type", "application/json");

                client.execute(post);
                Utils.debug("Sent delete request for " + requestBody);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.debug(e.getMessage());
            }
        }).start();
    }

    public static void sendCachedData() {
        List<ShopDataHolder> shops = new ArrayList<>(ShopDataManager.s_CachedShops);
        List<WaystoneDataHolder> waystones = new ArrayList<>(WaystoneModule.s_CachedWaystones);

        if(shops.isEmpty() && waystones.isEmpty()) {
            Utils.debug("No cached data to send");
            return;
        }

        ModConfig config = Asmputils.s_Config;
        if(config.postUrl == null || config.postUrl.isEmpty()) return;

        HttpPost post = new HttpPost(config.postUrl);
        new Thread(() -> {
            try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
                ShopWaystoneUploadPacket packet = new ShopWaystoneUploadPacket(shops,waystones);
                StringEntity postString = new StringEntity(gson.toJson(packet), ContentType.APPLICATION_JSON);
                Utils.debug(gson.toJson(packet));
                post.setEntity(postString);
                post.setHeader("Content-Type", "application/json");

                client.execute(post);
                Utils.debug("Sent cached shops & waystone data.");
            } catch (Exception e) {
                e.printStackTrace();
                Utils.debug(e.getMessage());
            }
        }).start();
    }
}
