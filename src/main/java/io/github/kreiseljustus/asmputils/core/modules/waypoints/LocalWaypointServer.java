package io.github.kreiseljustus.asmputils.core.modules.waypoints;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import io.github.kreiseljustus.asmputils.core.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class LocalWaypointServer {
    private HttpServer server;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(52629), 0);

        server.createContext("/waypoint", exchange -> {
            String origin = exchange.getRequestHeaders().getFirst("Origin");
            if (origin != null) {
                if (origin.equals("https://kreiseljustus.com") || origin.startsWith("http://localhost")) {
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", origin);
                }
            }

            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            // Handle CORS preflight
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream is = exchange.getRequestBody()) {
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                    String name = json.get("name").getAsString();
                    int x = json.get("x").getAsInt();
                    int y = json.get("y").getAsInt();
                    int z = json.get("z").getAsInt();

                    int dim = json.get("dimension").getAsInt();
                    String dimension = (dim == 1) ? "the_nether" : (dim == 2) ? "the_end" : "overworld";
                    Utils.debug("Received waypoint: " + name + " (" + x + ", " + y + ", " + z + ", " + dimension + ")");

                    // Send formatted chat message to the local player only
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client != null && client.player != null) {
                        String initial = name.isEmpty() ? "n" : name.substring(0, 1);
                        String chatMsg = "xaero-waypoint:" + name + ":" + initial + ":" + x + ":" + y + ":" + z + ":6:false:0:Internal-" + dimension;
                        client.player.sendMessage(Text.of(chatMsg), false);
                    }

                    String response = "Waypoint received";
                    exchange.sendResponseHeaders(200, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                } catch (Exception e) {
                    String response = "Invalid JSON: " + e.getMessage();
                    exchange.sendResponseHeaders(400, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
            exchange.close();
        });

        new Thread(() -> server.start()).start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
