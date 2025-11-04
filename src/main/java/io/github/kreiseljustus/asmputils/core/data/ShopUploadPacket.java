package io.github.kreiseljustus.asmputils.core.data;

import java.util.List;

public class ShopUploadPacket {
    public List<ShopDataHolder> shops;

    public ShopUploadPacket(List<ShopDataHolder> shops) {
        this.shops = shops;
    }
}
