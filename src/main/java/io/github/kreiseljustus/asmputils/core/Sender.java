package io.github.kreiseljustus.asmputils.core;

import com.google.gson.*;
import io.github.kreiseljustus.asmputils.*;
import io.github.kreiseljustus.asmputils.core.data.*;
import io.github.kreiseljustus.asmputils.core.modules.waystones.WaystoneModule;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.ArrayList;
import java.util.List;

import static io.github.kreiseljustus.asmputils.Asmputils.s_Config;

public class Sender {
    public static void sendDeleteRequest(ShopDataHolder shop) {
        sendDeleteRequest(shop, null);
    }

    public static void sendDeleteRequest(ShopDataHolder shop, WaystoneDataHolder waystone) {
        if(shop != null && waystone != null) return;

        String dataJson = shop == null ? Utils.s_Gson.toJson(waystone) : Utils.s_Gson.toJson(shop);

        String requestBody = String.format("{\"type\":\"shop\",\"data\":%s}", dataJson);

        HttpPost post = new HttpPost(s_Config.deleteRoute);
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

    public static void sendCachedShopData() {
        List<ShopDataHolder> shops = new ArrayList<>(ShopDataManager.s_CachedShops);
        if(shops.isEmpty()) {
            Utils.debug("No cached data to send");
            return;
        }
        if(s_Config.postUrl == null || s_Config.postUrl.isEmpty()) return;

        ShopUploadPacket packet = new ShopUploadPacket(shops);
        sendDataFromClient(packet);
    }

    public static void sendCachedWaystoneData() {
        List<WaystoneDataHolder> waystones = new ArrayList<>(WaystoneModule.s_CachedWaystones);
        if(waystones.isEmpty()) {
            Utils.debug("No cached data to send");
            return;
        }
        if(s_Config.postUrl == null || s_Config.postUrl.isEmpty()) return;

        WaystoneUploadPacket packet = new WaystoneUploadPacket(waystones);
        sendDataFromClient(packet);
    }

    public static <T> void sendDataFromClient(T packet){
        HttpPost post = new HttpPost(s_Config.postUrl);
        new Thread(() -> {
            try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
                StringEntity postString = new StringEntity(Utils.s_Gson.toJson(packet), ContentType.APPLICATION_JSON);
                Utils.debug(Utils.s_Gson.toJson(packet));
                post.setEntity(postString);
                post.setHeader("Content-Type", "application/json");

                client.execute(post);
                Utils.debug("Sent data packet.");
            } catch (Exception e) {
                e.printStackTrace();
                Utils.debug(e.getMessage());
            }
        }).start();
    }
}
