package io.github.kreiseljustus.asmpshopget;

import java.util.LinkedList;

public class ShopDataManager {
    public static LinkedList<ShopDataHolder> s_CachedShops = new LinkedList<>();

    public static void addShop(ShopDataHolder shop) {
        if(!s_CachedShops.contains(shop)) {
            s_CachedShops.add(shop);
        }
    }
}
