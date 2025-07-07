package io.github.kreiseljustus.asmpshopget;

import com.google.gson.Gson;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.List;

public class Sender {

    static Gson gson = new Gson();

    public static void sendShopDataCached(List<ShopDataHolder> shops) {
        if(shops.isEmpty()) {Utils.debug("No cached shops to send"); return;}

        ModConfig config = Asmpshopget.s_Config;

        if(config.postUrl == null || config.postUrl.isEmpty()) return;
        HttpPost post = new HttpPost(config.postUrl);
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            StringEntity postString = new StringEntity(gson.toJson(shops.toArray()), ContentType.APPLICATION_JSON);
            post.setEntity(postString);
            post.setHeader("Content-Type", "application/json");
            client.execute(post);
            client.close();
        } catch (IOException e) {
            Utils.debug("IOException: " + e.getMessage());
        }
    }
}
