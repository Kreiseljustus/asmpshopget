package io.github.kreiseljustus.asmputils.core;

import net.minecraft.util.math.ChunkPos;

public interface IModule {
    abstract void onTick();

    abstract void onChunkEnter(ChunkPos chunkPos);

    abstract void onStop();
}
