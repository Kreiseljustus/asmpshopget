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

public class WikiCommand implements ICommand{
    @Override
    public String getCommandName() {
        return "wiki";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> build(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.executes(this::executeChecked);
    }

    @Override
    public int execute(CommandContext<FabricClientCommandSource> context) {
        Utils.openLinkWithConfirm("https://atriocsmp.gitbook.io/wiki");
        return 0;
    }
}
