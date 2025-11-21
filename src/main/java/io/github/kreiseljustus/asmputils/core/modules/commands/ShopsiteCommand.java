package io.github.kreiseljustus.asmputils.core.modules.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.kreiseljustus.asmputils.core.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.util.Util;

import java.util.concurrent.TimeUnit;

import static io.github.kreiseljustus.asmputils.Asmputils.tickDelay;

public class ShopsiteCommand implements ICommand{
    @Override
    public String getCommandName() {
        return "shopsite";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> build(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder;
    }

    @Override
    public int execute(CommandContext<FabricClientCommandSource> context) {
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

        return 0;
    }
}
