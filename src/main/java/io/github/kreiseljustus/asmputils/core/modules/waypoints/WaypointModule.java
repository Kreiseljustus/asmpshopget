package io.github.kreiseljustus.asmputils.core.modules.waypoints;

import io.github.kreiseljustus.asmputils.Asmputils;
import io.github.kreiseljustus.asmputils.core.IModule;
import io.github.kreiseljustus.asmputils.core.Sender;
import io.github.kreiseljustus.asmputils.core.Utils;
import io.github.kreiseljustus.asmputils.core.data.ShopDataManager;
import io.github.kreiseljustus.asmputils.core.modules.WaystoneModule;
import net.minecraft.util.math.ChunkPos;

import static io.github.kreiseljustus.asmputils.Asmputils.s_Config;

public class WaypointModule implements IModule {

    private static LocalWaypointServer waypointServer = null;

    @Override
    public void onInitClient() {
        try {
            if(s_Config.enableWaypointModule) {
                waypointServer =  new LocalWaypointServer();
                waypointServer.start();
            }
        } catch (Exception e) {
            Utils.debug("Failed to start LocalWaypointServer: " + e.getMessage());
            waypointServer.stop();
        }
    }

    @Override
    public void onTick() {
        if(Asmputils.s_TicksInASMPServer % Asmputils.s_Config.ticksBetweenSends == 0) {
            Sender.sendCachedWaystoneData();
            WaystoneModule.s_CachedWaystones.clear();
        }

    }

    @Override
    public void onChunkEnter(ChunkPos chunkPos) {

    }

    @Override
    public void onStop() {
        waypointServer.stop();
    }
}
