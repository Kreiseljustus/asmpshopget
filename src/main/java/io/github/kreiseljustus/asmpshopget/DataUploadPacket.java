package io.github.kreiseljustus.asmpshopget;

import java.util.List;

public class DataUploadPacket {
    public List<ShopDataHolder> shops;
    public List<WaystoneDataHolder> waystones;

    public DataUploadPacket(List<ShopDataHolder> shops, List<WaystoneDataHolder> waystones) {
        this.shops = shops;
        this.waystones = waystones;
    }
}
