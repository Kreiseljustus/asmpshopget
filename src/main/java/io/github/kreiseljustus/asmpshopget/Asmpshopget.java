package io.github.kreiseljustus.asmpshopget;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Asmpshopget implements ModInitializer {
    static final String VERSION = "1.0.11";
    static final String VERSION_URL = "https://kreiseljustus.com/asmp_version.txt";

    ModConfig m_Config;

    @Override
    public void onInitialize() {
        ModConfig.register();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    public void onClientTick(MinecraftClient client) {
        m_Config = ModConfig.get();
        if(!m_Config.enable) return;
        if(!m_Config.allowOnAllServers && !Utils.onASMP()) return;
        if(client.player == null) return;

        VersionManagment.checkAndWarnVersion(client.player);
    }
}
