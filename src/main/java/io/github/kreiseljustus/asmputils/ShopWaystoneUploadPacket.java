package io.github.kreiseljustus.asmputils;

import java.util.List;

public class ShopWaystoneUploadPacket {
    public List<ShopDataHolder> shops;
    public List<WaystoneDataHolder> waystones;

    public ShopWaystoneUploadPacket(List<ShopDataHolder> shops, List<WaystoneDataHolder> waystones) {
        this.shops = shops;
        this.waystones = waystones;
    }
}
