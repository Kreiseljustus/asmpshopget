package io.github.kreiseljustus.asmputils.core.modules;

import net.minecraft.util.math.ChunkPos;

public interface IModule {
    String getModuleName();
    void onInitClient();
    void onTick(boolean moduleEnabled, int totalTicks);
    void onChunkEnter(ChunkPos chunkPos);
    void onStop();
}
