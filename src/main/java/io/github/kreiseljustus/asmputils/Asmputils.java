package io.github.kreiseljustus.asmputils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Asmputils implements ModInitializer {
    public static ModConfig s_Config;
    public static PlayerEntity s_Player;

    private static LocalWaypointServer waypointServer = null;

    Timer timer = new Timer();
    int tickInServer = 0;

    boolean checkedVersionOnStartup = false;

    ChunkPos lastChunkPosition = null;

    @Override
    public void onInitialize() {
        ModConfig.register();

        s_Config = ModConfig.get();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        ClientTickEvents.END_CLIENT_TICK.register(WaystoneManager::waystoneTick);

        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStop);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client == null || client.player == null) return;
                if (!s_Config.enable) return;
                if (!s_Config.allowOnAllServers && !Utils.onASMP()) return;

                VersionManagement.checkAndWarnVersion(client.player);
            }
        },0,300_000);

        Thread fetcherThread = ServerValidator.getFetcherThread();
        fetcherThread.start();

        try {
            if(s_Config.enableWaypointFeature) {
                waypointServer =  new LocalWaypointServer();
                waypointServer.start();
            }
        } catch (Exception e) {
            Utils.debug("Failed to start LocalWaypointServer: " + e.getMessage());
            waypointServer.stop();
        }
    }

    public void onClientTick(MinecraftClient client) {
        s_Config = ModConfig.get();
        if(!s_Config.enable) return;
        if(client.player == null) return;
        if(!s_Config.allowOnAllServers && !Utils.onASMP()) return;

        s_Player = client.player;

        if(!checkedVersionOnStartup) {
            VersionManagement.checkAndWarnVersion(client.player);

            checkedVersionOnStartup = true;
        }

        if(s_Config.ticksBetweenSends < 400) s_Config.ticksBetweenSends = 600;

        ChunkPos currentChunkPosition = new ChunkPos(s_Player.getBlockPos());

        if(lastChunkPosition == null || !lastChunkPosition.equals(currentChunkPosition)) {
            onEnterNewChunk(currentChunkPosition);
            lastChunkPosition = currentChunkPosition;
        }

        //We should send our data because our timer is done
        if(tickInServer % s_Config.ticksBetweenSends == 0) {
            if(!VersionManagement.s_UsingLatestVersion) {Utils.debug("Discarding- not up-to date!"); tickInServer++; return;}
            Utils.debug("Attempting to send cached shops");

            Sender.sendCachedData();
            ShopDataManager.s_CachedShops.clear();
            WaystoneManager.s_CachedWaystones.clear();
        }

        tickInServer++;
    }

    private void onEnterNewChunk(ChunkPos currentChunk) {
        Utils.debug("Entered new chunk");

        if(s_Config.trackShops) {
            ShopManager.handleShopDetection(currentChunk);
        }
    }

    private void onClientStop(MinecraftClient client) {
        waypointServer.stop();
    }
}
