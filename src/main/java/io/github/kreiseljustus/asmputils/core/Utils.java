package io.github.kreiseljustus.asmputils.core;

import io.github.kreiseljustus.asmputils.Asmputils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;


public class Utils {
    public static boolean onASMP() {
        MinecraftClient client = MinecraftClient.getInstance();

        ServerInfo server = client.getCurrentServerEntry();
        if(server == null) return false;
        return server.address.equals("asmp.cc");
    }

    public static void debug(String message) {
        System.out.println(message);
        if(!Asmputils.s_Config.enableDebugMode || !Asmputils.s_Config.enable || Asmputils.s_Player == null) return;
        Asmputils.s_Player.sendMessage(Text.of("[ASMP Utils] " + message), false);
    }

    public static boolean getModuleOn(String moduleName) {
        try {
            String fieldName = "enable" + moduleName;

            var field = Asmputils.s_Config.getClass().getDeclaredField(fieldName);

            return field.getBoolean(Asmputils.s_Config);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String dimensionFromInt(int dim) {
        return switch (dim) {
            case 1 -> "the_nether";
            case 2 -> "the_end";
            default -> "overworld";
        };
    }
}
