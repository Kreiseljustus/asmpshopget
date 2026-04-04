package io.github.kreiseljustus.asmputils.core.modules.commands;

import io.github.kreiseljustus.asmputils.core.Utils;
import io.github.kreiseljustus.asmputils.core.modules.IModule;
import io.github.kreiseljustus.asmputils.core.modules.commands.browser.BrowserCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ChunkPos;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class CommandsModule implements IModule {

    boolean enabled = true;

    public static List<ICommand> commands = new LinkedList<>();

    private static KeyBinding openShopBrowser = null;
    private static KeyBinding openShopSite = null;

    @Override
    public String getModuleName() {
        return "CommandsModule";
    }

    @Override
    public void onInitClient() {

        //Reflection?
        commands.add(new ShopsiteCommand());
        commands.add(new EvalCommand());
        commands.add(new BrowserCommand());

        openShopBrowser = Utils.registerKeyBind("openShopBrowser", GLFW.GLFW_KEY_UNKNOWN);
        openShopSite = Utils.registerKeyBind("openShopsite", GLFW.GLFW_KEY_UNKNOWN);

        for(ICommand command : commands) {
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                dispatcher.register(command.build(ClientCommandManager.literal(command.getCommandName()))
                        .executes(context -> {

                            if(!enabled) {
                                context.getSource().sendFeedback(Text.literal("The commands module is disabled! Enable it in the config").formatted(Formatting.RED));
                                return 1;
                            }

                            return command.execute(context);
                        }));
            });
        }
    }

    @Override
    public void onTick(boolean enabled) {
        this.enabled = enabled;
        if(!enabled) return;

        while(openShopBrowser.wasPressed()) {
            new BrowserCommand().execute(null);
        }

        while(openShopSite.wasPressed()) {
            new ShopsiteCommand().execute(null);
        }
    }

    @Override
    public void onChunkEnter(ChunkPos chunkPos) {
        if(!enabled) return;
    }

    @Override
    public void onStop() {

    }
}
