package io.github.kreiseljustus.asmputils.core.data;

import java.util.List;

public class WaystoneUploadPacket {
    public List<WaystoneDataHolder> waystones;

    public WaystoneUploadPacket(List<WaystoneDataHolder> waystones) {
        this.waystones = waystones;
    }
}
