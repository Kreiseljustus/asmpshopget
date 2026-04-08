package io.github.kreiseljustus.asmputils.core.modules.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface ICommand {
    String getCommandName();
    LiteralArgumentBuilder<FabricClientCommandSource> build(LiteralArgumentBuilder<FabricClientCommandSource> builder);
    int execute(CommandContext<FabricClientCommandSource> context);

    default int executeChecked(CommandContext<FabricClientCommandSource> context) {
        if (!CommandsModule.enabled) {
            context.getSource().sendFeedback(
                    Text.literal("The commands module is disabled! Enable it in the config")
                            .formatted(Formatting.RED));
            return 1;
        }
        return execute(context);
    }
}
