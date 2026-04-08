package io.github.kreiseljustus.asmputils.core.modules.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.kreiseljustus.asmputils.core.Utils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class MapCommand implements ICommand{
    @Override
    public String getCommandName() {
        return "map";
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> build(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.executes(this::executeChecked);
    }

    @Override
    public int execute(CommandContext<FabricClientCommandSource> context) {
        Utils.openLinkWithConfirm("https://map.asmp.cc/");
        return 0;
    }
}
