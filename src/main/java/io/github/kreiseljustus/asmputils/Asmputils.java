package io.github.kreiseljustus.asmputils;

import io.github.kreiseljustus.asmputils.core.IModule;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

public class Asmputils implements ModInitializer {
    public static ModConfig s_Config;
    public static PlayerEntity s_Player;

    private static LocalWaypointServer waypointServer = null;

    Timer timer = new Timer();
    static int s_TicksInASMPServer = 0;

    boolean checkedVersionOnStartup = false;

    ChunkPos lastChunkPosition = null;

    List<IModule> modules = new ArrayList<>();

    @Override
    public void onInitialize() {
        ModConfig.register();

        s_Config = ModConfig.get();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
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

        if(s_Config.enableShopModule) {
            modules.add(new ShopModule());
        }
        if(s_Config.enableWaystoneModule) {
            modules.add(new WaystoneModule());
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

        for(IModule module : modules) {
            module.onTick();
        }

        ChunkPos currentChunkPosition = new ChunkPos(s_Player.getBlockPos());

        if(lastChunkPosition == null || !lastChunkPosition.equals(currentChunkPosition)) {

            for(IModule module : modules) {
                module.onChunkEnter(currentChunkPosition);
            }
            lastChunkPosition = currentChunkPosition;
        }

        if(s_TicksInASMPServer % s_Config.ticksBetweenSends == 0) {
            if(!VersionManagement.s_UsingLatestVersion) {Utils.debug("Discarding- not up-to date!"); s_TicksInASMPServer++; return;}
            Utils.debug("Attempting to send cached shops");

            //Gotta refactor sender to be able to send shops and waystones separately
            Sender.sendCachedData();
            ShopDataManager.s_CachedShops.clear();
            WaystoneModule.s_CachedWaystones.clear();
        }

        s_TicksInASMPServer++;
    }

    private void onClientStop(MinecraftClient client) {

        for(IModule module : modules) {
            module.onStop();
        }

        waypointServer.stop();
    }
}
