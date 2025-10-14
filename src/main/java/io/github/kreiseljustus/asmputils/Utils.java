package io.github.kreiseljustus.asmputils;

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
        if(!Asmputils.s_Config.enableDebugMode || !Asmputils.s_Config.enable) return;
        System.out.println(message);
        Asmputils.s_Player.sendMessage(Text.of(message), false);
    }
}
