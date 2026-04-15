package io.github.kreiseljustus.asmputils.core.data;

import net.minecraft.client.MinecraftClient;

import java.util.UUID;

public class StatisticsPacket {
    public String username;
    public UUID userUUID;

    //Parsing done on server side
    public String statisticsJson;

    public StatisticsPacket(String username, UUID userUUID, String statisticsJson) {
        this.username = username;
        this.userUUID = userUUID;
        this.statisticsJson = statisticsJson;
    }
}
