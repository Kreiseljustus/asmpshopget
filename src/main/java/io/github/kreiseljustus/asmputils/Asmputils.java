package io.github.kreiseljustus.asmputils;

import com.nimbusds.common.contenttype.ContentType;
import io.github.kreiseljustus.asmputils.core.*;
import io.github.kreiseljustus.asmputils.core.data.ShopDataManager;
import io.github.kreiseljustus.asmputils.core.modules.commands.CommandsModule;
import io.github.kreiseljustus.asmputils.core.modules.shop.ServerValidator;
import io.github.kreiseljustus.asmputils.core.modules.shop.ShopModule;
import io.github.kreiseljustus.asmputils.core.modules.WaystoneModule;
import io.github.kreiseljustus.asmputils.core.modules.waypoints.WaypointModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;

import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Asmputils implements ClientModInitializer {
    public static ModConfig s_Config;
    public static PlayerEntity s_Player;

    Timer timer = new Timer();
    public static int s_TicksInASMPServer = 0;

    boolean checkedVersionOnStartup = false;

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

        if(!checkedVersionOnStartup) {
            VersionManagement.checkAndWarnVersion(client.player);

            checkedVersionOnStartup = true;
        }

        if(s_Config.ticksBetweenSends < 400) s_Config.ticksBetweenSends = 600;

        for(IModule module : modules) {
            boolean enabled = Utils.getModuleOn(module.getModuleName());
            module.onTick(enabled);
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
        }

        s_TicksInASMPServer++;
    }

    private void onClientStop(MinecraftClient client) {

        for(IModule module : modules) {
            module.onStop();
        }
    }
}
