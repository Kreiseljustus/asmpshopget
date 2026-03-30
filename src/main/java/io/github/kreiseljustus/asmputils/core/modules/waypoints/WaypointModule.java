package io.github.kreiseljustus.asmputils.core.modules.waypoints;

import io.github.kreiseljustus.asmputils.core.IModule;
import io.github.kreiseljustus.asmputils.core.Utils;
import net.minecraft.util.math.ChunkPos;

import static io.github.kreiseljustus.asmputils.Asmputils.s_Config;

public class WaypointModule implements IModule {

    public static LocalWaypointServer getWaypointServer() {
        return waypointServer;
    }

    private static LocalWaypointServer waypointServer = null;

    @Override
    public String getModuleName() {
        return "WaypointModule";
    }

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
    public void onTick(boolean moduleEnabled) {

    }

    @Override
    public void onChunkEnter(ChunkPos chunkPos) {

    }

    @Override
    public void onStop() {
        waypointServer.stop();
    }
}
