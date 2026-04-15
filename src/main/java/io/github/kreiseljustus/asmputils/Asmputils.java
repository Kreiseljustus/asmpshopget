package io.github.kreiseljustus.asmputils;

import io.github.kreiseljustus.asmputils.config.ModConfig;
import io.github.kreiseljustus.asmputils.core.*;
import io.github.kreiseljustus.asmputils.core.modules.IModule;
import io.github.kreiseljustus.asmputils.core.modules.commands.CommandsModule;
import io.github.kreiseljustus.asmputils.core.modules.shop.ServerValidator;
import io.github.kreiseljustus.asmputils.core.modules.shop.ShopModule;
import io.github.kreiseljustus.asmputils.core.modules.waystones.WaystoneModule;
import io.github.kreiseljustus.asmputils.core.modules.waypoints.WaypointModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Asmputils implements ClientModInitializer {
    public static ModConfig s_Config;
    public static PlayerEntity s_Player;

    //Change this to ticks since game start?
    public static int s_TicksInASMPServer = 0;

    ChunkPos lastChunkPosition = null;

    List<IModule> modules = new ArrayList<>();

    public static final ScheduledExecutorService tickDelay = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "mod-delay");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void onInitializeClient() {
        ModConfig.register();

        s_Config = ModConfig.get();
        ModConfig.validateAndUpdate();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(this::onClientStop);

        ModrinthVersionManagement.checkUpdateAvailable().thenAccept(updateAvailable -> {
           if(updateAvailable) {
               Utils.debug("Update available onInitializeClient()");
               MinecraftClient.getInstance().execute(() -> {
                   ModrinthVersionManagement.updateAvailable = true;
               });
           } else {
               Utils.debug("No update available on Modrinth");
           }
        });
        Thread fetcherThread = ServerValidator.getFetcherThread();
        fetcherThread.start();

        modules.add(new ShopModule());
        modules.add(new WaystoneModule());
        modules.add(new WaypointModule());


        //Special case.
        //This should always register command and the commands themself check if the module
        //is enabled or not
        modules.add(new CommandsModule());

        for(IModule module : modules) {
            module.onInitClient();
        }
    }

    public void onClientTick(MinecraftClient client) {
        s_Config = ModConfig.get();
        if(!s_Config.enable) return;
        if(client.player == null) return;
        if(!s_Config.allowOnAllServers && !Utils.onASMP()) return;

        s_Player = client.player;

        if(s_Config.ticksBetweenSends < 400) s_Config.ticksBetweenSends = 600;

        for(IModule module : modules) {
            boolean enabled = Utils.getModuleOn(module.getModuleName());
            module.onTick(enabled, s_TicksInASMPServer);
        }

        ChunkPos currentChunkPosition = new ChunkPos(s_Player.getBlockPos());

        if(lastChunkPosition == null || !lastChunkPosition.equals(currentChunkPosition)) {

            for(IModule module : modules) {
                module.onChunkEnter(currentChunkPosition);
            }
            lastChunkPosition = currentChunkPosition;
        }
        s_TicksInASMPServer++;
    }

    private void onClientStop(MinecraftClient client) {

        for(IModule module : modules) {
            module.onStop();
        }
    }
}
