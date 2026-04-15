package io.github.kreiseljustus.asmputils.core.modules.stats;

import com.google.gson.JsonObject;
import io.github.kreiseljustus.asmputils.Asmputils;
import io.github.kreiseljustus.asmputils.core.Utils;
import io.github.kreiseljustus.asmputils.core.modules.IModule;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

import java.util.Objects;

public class StatModule implements IModule {

    boolean enabled = true;
    int currentTick = 0;
    int requestTimerTick = 20000;
    int requestTimer = 18000;

    @Override
    public String getModuleName() {
        return "StatsModule";
    }

    @Override
    public void onInitClient() {
    }

    @Override
    public void onTick(boolean moduleEnabled, int totalTicks) {
        this.enabled = moduleEnabled;
        if(!enabled) return;

        if (currentTick >= Asmputils.s_Config.ticksBetweenSends) {
            MinecraftClient client = MinecraftClient.getInstance();

            if(requestTimerTick >= requestTimer) {
                Objects.requireNonNull(client.getNetworkHandler()).sendPacket(
                        new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS)
                );
                requestTimer = 0;
            }

            StatHandler stats = client.player != null ? client.player.getStatHandler() : null;

            JsonObject root = new JsonObject();

            for (StatType<?> statType : Registries.STAT_TYPE) {
                processStatType(statType, stats, root);
            }

            Utils.debug(root.toString());
            currentTick = 0;
        }
        requestTimerTick++;
        currentTick++;
    }

    @Override
    public void onChunkEnter(ChunkPos chunkPos) {
        if(!enabled) return;
    }

    @Override
    public void onStop() {

    }

    private static <T> void processStatType(
            StatType<T> statType,
            StatHandler stats,
            JsonObject root
    ) {
        String typeId = Objects.requireNonNull(Registries.STAT_TYPE.getId(statType)).toString();
        JsonObject typeObj = new JsonObject();

        for (T value : statType.getRegistry()) {
            Stat<T> stat = statType.getOrCreateStat(value);
            int statValue = stats.getStat(stat);

            if (statValue == 0) continue;

            Identifier id = statType.getRegistry().getId(value);
            if (id != null) {
                typeObj.addProperty(id.toString(), statValue);
            }
        }

        if (!typeObj.isEmpty()) {
            root.add(typeId, typeObj);
        }
    }
}
