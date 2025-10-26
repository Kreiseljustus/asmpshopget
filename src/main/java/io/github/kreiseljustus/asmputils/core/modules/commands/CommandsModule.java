package io.github.kreiseljustus.asmputils.core.modules.commands;

import io.github.kreiseljustus.asmputils.core.IModule;
import io.github.kreiseljustus.asmputils.core.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.TimeUnit;

import static io.github.kreiseljustus.asmputils.Asmputils.tickDelay;

public class CommandsModule implements IModule {

    @Override
    public void onInitClient() {

        //This will be changed to allow for better command creation

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

    @Override
    public void onTick() {

    }

    @Override
    public void onChunkEnter(ChunkPos chunkPos) {

    }

    @Override
    public void onStop() {

    }
}
