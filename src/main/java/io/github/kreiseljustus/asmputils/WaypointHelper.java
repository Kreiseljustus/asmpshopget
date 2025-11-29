package io.github.kreiseljustus.asmputils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.container.MinimapWorldContainer;
import xaero.hud.path.XaeroPath;

public class WaypointHelper {

    /**
     * Creates a Xaero's Minimap Waypoint
     * @param pos position of the Waypoint
     * @param name name of the Waypoint
     * @param initials short acronym of the Waypoint name
     * @param color color of the Waypoint
     * @param purpose description of the waypoint
     * @param temporary decides whether a Waypoint should be permanent or temporary
     * @param requestedDimensionRoot dimension in which the Waypoint is located
     */
    public void createWaypoint(BlockPos pos, String name, String initials,
                               WaypointColor color, WaypointPurpose purpose,
                               boolean temporary, String requestedDimensionRoot) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        // Avoid creating while Xaero's Waypoints GUI is open (can cause race conditions)
        if (isWaypointsMenuOpen(client)) {
            notifyPlayer(client, "[ASMP Utils] Waypoint \"" + name + "\" not created because Waypoints menu is open.");
            return;
        }

        Waypoint waypoint = new Waypoint(pos.getX(), pos.getY(), pos.getZ(), name, initials, color, purpose, temporary);

        // Schedule safely on the Minecraft client thread
        client.execute(() -> addWaypoint(client, waypoint, requestedDimensionRoot, pos, name));
    }

    /**
     * Add Waypoint to the world
     * @param client user client
     * @param waypoint waypoint
     * @param dimensionRoot dimension in which the Waypoint is located
     * @param pos position of the Waypoint
     * @param name name of the Waypoint
     */
    private void addWaypoint(MinecraftClient client, Waypoint waypoint, String dimensionRoot, BlockPos pos, String name) {
        try {
            MinimapSession session = (MinimapSession) BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session == null || session.getWorldManager() == null) return;

            MinimapWorld world = resolveWorld(session, dimensionRoot);
            if (world == null) return;

            WaypointSet set = world.getCurrentWaypointSet();
            if (set == null) return;

            set.add(waypoint);
            markSetChanged(session);
            saveWorld(session, world);
            notifyWaypointCreated(client, dimensionRoot, name, pos);
        } catch (Throwable t) {
            notifyPlayer(client, "[ASMP Utils] Failed to create waypoint: " + t.getMessage());
        }
    }

    // Resolves the Xaero Minimap world from a dimension identifier (e.g. "overworld", "the_nether").
    private MinimapWorld resolveWorld(MinimapSession session, String root) {
        MinimapWorld current = session.getWorldManager().getCurrentWorld();
        if (current == null || root == null || root.isEmpty()) return current;

        try {
            String dirNode = switch (root.toLowerCase()) {
                case "overworld", "minecraft:overworld" -> "dim%0";
                case "the_nether", "nether", "minecraft:the_nether" -> "dim%-1";
                case "the_end", "end", "minecraft:the_end" -> "dim%1";
                default -> root;
            };

            XaeroPath containerPath = session.getWorldState().getAutoRootContainerPath().resolve(dirNode);
            MinimapWorldContainer container = session.getWorldManager().getWorldContainer(containerPath);
            if (container == null) return current;

            MinimapWorld autoWorld = session.getWorldManager().getAutoWorld();
            if (container == autoWorld.getContainer()) return autoWorld;

            MinimapWorld candidate = container.getFirstWorldConnectedTo(autoWorld);
            return candidate != null ? candidate : container.getFirstWorld();
        } catch (Throwable ignored) {
            return current;
        }
    }

    // Marks the waypoint set as changed so Xaero updates its state.
    private void markSetChanged(MinimapSession session) {
        try {
            session.getWaypointSession().setSetChangedTime(System.currentTimeMillis());
        } catch (Throwable ignored) {}
    }

    // Attempts to persist the world file to disk.
    private void saveWorld(MinimapSession session, MinimapWorld world) {
        try {
            session.getWorldManagerIO().saveWorld(world);
        } catch (Throwable ignored) {}
    }

    // Sends a success message to the player.
    private void notifyWaypointCreated(MinecraftClient client, String dimension, String name, BlockPos pos) {
        if (client.player == null) return;
        notifyPlayer(client, String.format(
            "[ASMP Utils] Waypoint created: %s at (%d, %d, %d) in %s",
            name, pos.getX(), pos.getY(), pos.getZ(), dimension
        ));
    }

    // Checks if Xaero's Waypoints GUI is currently open.
    private boolean isWaypointsMenuOpen(MinecraftClient client) {
        return client.currentScreen != null &&
               "xaero.common.gui.GuiWaypoints".equals(client.currentScreen.getClass().getName());
    }

    // Sends a chat message to the player.
    private void notifyPlayer(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.of(message), false);
        }
    }
}
