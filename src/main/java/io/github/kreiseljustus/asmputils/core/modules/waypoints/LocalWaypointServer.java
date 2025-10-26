package io.github.kreiseljustus.asmputils.core.modules.waypoints;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class LocalWaypointServer {
    private HttpServer server;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(52629), 0);

        // Root endpoint for simple health check
        server.createContext("/", exchange -> sendText(exchange, 200, "LocalWaypointServer running"));

        // /waypoint endpoint to receive waypoint creation requests
        server.createContext("/waypoint", this::handleWaypointRequest);

        new Thread(() -> server.start()).start();
    }

    // Stops the server
    public void stop() {
        if (server != null) server.stop(0);
    }

    // Handles POST /waypoint requests and creates a waypoint
    private void handleWaypointRequest(HttpExchange exchange) throws IOException {
        addCORS(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        JsonObject json = parseJson(exchange);
        if (json == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            createWaypointIfAllowed(
                    client,
                    json.get("name").getAsString(),
                    json.get("x").getAsInt(),
                    json.get("y").getAsInt(),
                    json.get("z").getAsInt(),
                    dimensionFromInt(json.get("dimension").getAsInt())
            );
        }

        sendText(exchange, 200, "Waypoint received");
    }

    // Adds CORS headers for browser requests
    private void addCORS(HttpExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (origin != null && (origin.equals("https://kreiseljustus.com") || origin.startsWith("http://localhost"))) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", origin);
        }
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    // Reads and parses a JSON body from the request
    private JsonObject parseJson(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception e) {
            sendText(exchange, 400, "Invalid JSON: " + e.getMessage());
            return null;
        }
    }

    // Sends a plain text response
    private void sendText(HttpExchange exchange, int status, String text) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    // Converts an integer ID from the frontend into a Minecraft dimension name
    private String dimensionFromInt(int dim) {
        return switch (dim) {
            case 1 -> "the_nether";
            case 2 -> "the_end";
            default -> "overworld";
        };
    }

    // Validates settings and creates the waypoint using WaypointHelper class
    private void createWaypointIfAllowed(MinecraftClient client, String name, int x, int y, int z, String dimension) {
        ModConfig cfg = ModConfig.get();

        // Check if feature is enabled
        if (!cfg.enableWaypointFeature) {
            client.player.sendMessage(Text.of("[ASMP Utils] Waypoint feature disabled in config."), false);
            return;
        }

        // Ensure Xaero’s Minimap is installed
        if (!FabricLoader.getInstance().isModLoaded("xaerominimap")) {
            client.player.sendMessage(Text.of("[ASMP Utils] Xaero Minimap not found. Waypoint not created."), false);
            return;
        }

        // Determine waypoint initial and create it
        String initial = cfg.waypointInitial.isBlank()
                ? (name.isEmpty() ? "N" : name.substring(0, 1))
                : cfg.waypointInitial;

        WaypointHelper helper = new WaypointHelper();

        try {
            helper.createWaypoint(
                    new BlockPos(x, y, z),
                    name,
                    initial,
                    xaero.hud.minimap.waypoint.WaypointColor.valueOf(cfg.waypointColor.name()),
                    (cfg.waypointType.toString().equalsIgnoreCase("DESTINATION")
                        ? xaero.hud.minimap.waypoint.WaypointPurpose.DESTINATION
                        : xaero.hud.minimap.waypoint.WaypointPurpose.NORMAL),
                    cfg.waypointTemporary,
                    dimension
            );
        } catch (Throwable t) {
            client.player.sendMessage(Text.of("[ASMP Utils] Failed to create waypoint: " + t.getMessage()), false);
        }
    }
}
