package io.github.kreiseljustus.asmputils.core.modules.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public interface ICommand {
    public String getCommandName();
    LiteralArgumentBuilder<FabricClientCommandSource> build(LiteralArgumentBuilder<FabricClientCommandSource> builder);
    public int execute(CommandContext<FabricClientCommandSource> context);
}
