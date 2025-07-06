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
    ModConfig config;

    @Override
    public void onInitialize() {
        ModConfig.register();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    public void onClientTick(MinecraftClient client) {
        config = ModConfig.get();
        if(!config.enable) return;
        if(!config.allowOnAllServers && !Utils.onASMP()) return;

        ClientPlayerEntity p = client.player;
    }
}
