package io.github.kreiseljustus.asmputils.core.modules.commands;

import io.github.kreiseljustus.asmputils.core.IModule;
import io.github.kreiseljustus.asmputils.core.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;

import java.util.concurrent.TimeUnit;

import static io.github.kreiseljustus.asmputils.Asmputils.tickDelay;

public class CommandsModule implements IModule {

    boolean enabled = true;

    @Override
    public String getModuleName() {
        return "CommandsModule";
    }

    @Override
    public void onInitClient() {

        //This will be changed to allow for better command creation

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("shopsite")
                    .executes(context -> {
                        if(!enabled) {
                            context.getSource().sendFeedback(Text.literal("The commands module is disabled!").formatted(Formatting.RED));
                            return 1;
                        }
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
    public void onTick(boolean enabled) {
        this.enabled = enabled;
        if(!enabled) return;
    }

    @Override
    public void onChunkEnter(ChunkPos chunkPos) {
        if(!enabled) return;
    }

    @Override
    public void onStop() {

    }
}
