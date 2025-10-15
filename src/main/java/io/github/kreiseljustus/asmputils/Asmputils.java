package io.github.kreiseljustus.asmputils;

import io.github.kreiseljustus.asmputils.core.IModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Asmputils implements ClientModInitializer {
    public static ModConfig s_Config;
    public static PlayerEntity s_Player;

    private static LocalWaypointServer waypointServer = null;

    Timer timer = new Timer();
    static int s_TicksInASMPServer = 0;

    boolean checkedVersionOnStartup = false;

    ChunkPos lastChunkPosition = null;

    List<IModule> modules = new ArrayList<>();

    private static final ScheduledExecutorService tickDelay = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "mod-delay");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void onInitializeClient() {
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

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("shopsite")
                    .executes(context -> {
                        // delay ~1 tick (1 tick = 50 ms at 20 TPS)
                        tickDelay.schedule(() -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            client.execute(() -> {
                                client.setScreen(new ConfirmLinkScreen(confirmed -> {
                                    if (confirmed) {
                                        Util.getOperatingSystem().open("https://kreiseljustus.com");
                                    } else {
                                        Utils.debug("User cancelled");
                                    }
                                    client.setScreen(null);
                                }, "https://kreiseljustus.com", true));
                            });
                        }, 50, TimeUnit.MILLISECONDS);

                        return 1;
                    }));
        });
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
