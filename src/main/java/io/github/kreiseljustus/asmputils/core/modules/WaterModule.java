package io.github.kreiseljustus.asmputils.core.modules;

import io.github.kreiseljustus.asmputils.core.IModule;
import io.github.kreiseljustus.asmputils.core.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;

public class WaterModule implements IModule {

    public static boolean s_Enabled = false;

    @Override
    public String getModuleName() {
        return "WaterModule";
    }

    @Override
    public void onInitClient() {

    }

    private boolean lastEnabled = false;

    @Override
    public void onTick(boolean moduleEnabled) {
        if (moduleEnabled != lastEnabled) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.worldRenderer != null) {
                client.worldRenderer.reload();
            }
            lastEnabled = moduleEnabled;
        }
        s_Enabled = moduleEnabled;
    }

    @Override
    public void onChunkEnter(ChunkPos chunkPos) {
        if(!s_Enabled) return;

        Utils.debug("Water module is enabled!!!");
    }

    @Override
    public void onStop() {

    }
}
