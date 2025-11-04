package io.github.kreiseljustus.asmputils.core;

import net.minecraft.util.math.ChunkPos;

public interface IModule {
    abstract String getModuleName();

    abstract void onInitClient();

    abstract void onTick(boolean moduleEnabled);

    abstract void onChunkEnter(ChunkPos chunkPos);

    abstract void onStop();
}
