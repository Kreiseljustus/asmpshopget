package io.github.kreiseljustus.asmpshopget;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Asmpshopget implements ModInitializer {

    static final String VERSION = "1.0.18";
    static final String VERSION_URL = "https://kreiseljustus.com/asmp_version.txt";

    public static ModConfig s_Config;
    public static PlayerEntity s_Player;

    Timer timer = new Timer();

    int tickInServer = 0;

    boolean checkedVersionOnStartup = false;

    boolean tempDisable = false;

    ChunkPos lastChunkPosition = null;

    @Override
    public void onInitialize() {

        ModConfig.register();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client == null || client.player == null) return;
                if (!s_Config.enable || tempDisable) return;
                if (!s_Config.allowOnAllServers && !Utils.onASMP()) return;

                VersionManagment.checkAndWarnVersion(client.player);
            }
        },0,300_000);
    }

    public void onClientTick(MinecraftClient client) {
        s_Config = ModConfig.get();
        if(!s_Config.enable) return;
        if(tempDisable) return;
        if(client.player == null) return;
        if(!s_Config.allowOnAllServers && !Utils.onASMP()) return;

        s_Player = client.player;

        if(!checkedVersionOnStartup) {
            VersionManagment.checkAndWarnVersion(client.player);
            checkedVersionOnStartup = true;
        }

        if(s_Config.ticksBetweenSends < 400) s_Config.ticksBetweenSends = 600;

        //We should send our data because our timer is done
        if(tickInServer % s_Config.ticksBetweenSends == 0) {
            if(!VersionManagment.s_UsingLatestVersion) {Utils.debug("Discarding- not up-to date!"); tickInServer++; return;}
            Utils.debug("Attempting to send cached shops");
            List<ShopDataHolder> dataToSend = new ArrayList<>(ShopDataManager.s_CachedShops);

            Sender.sendShopDataCached(dataToSend);
            ShopDataManager.s_CachedShops.clear();
        }

        ChunkPos currentChunkPosition = new ChunkPos(s_Player.getBlockPos());

        if(lastChunkPosition == null || !lastChunkPosition.equals(currentChunkPosition)) {
            onEnterNewChunk(currentChunkPosition);
            lastChunkPosition = currentChunkPosition;
        }

        tickInServer++;
    }

    public void onEnterNewChunk(ChunkPos currentChunk) {
        Utils.debug("Entered new chunk");
        World world = s_Player.getWorld();

        Chunk chunk = world.getChunk(currentChunk.getStartPos());

        for (BlockPos pos : chunk.getBlockEntityPositions()) {
            BlockEntity entity = world.getBlockEntity(pos);
            Utils.debug("Entity: " + entity.getType().getRegistryEntry());
            Utils.debug("EntityPos: " + entity.getPos());

            if (!(entity instanceof SignBlockEntity)) {
                Utils.debug("No SignBlockEntity here");
                continue;
            }

            SignBlockEntity sign = (SignBlockEntity) entity;
            BlockState blockState = world.getBlockState(pos);

            Utils.debug("Block at pos: " + pos + " is " + blockState.getBlock().getTranslationKey());

            if (!(blockState.getBlock() instanceof SignBlock || blockState.getBlock() instanceof WallSignBlock)) {
                Utils.debug("No SignBlock here");
                continue;
            }

            SignText text = sign.getFrontText();

            String[] lines = Arrays.stream(text.getMessages(false)).map(Text::getString).toArray(String[]::new);

            if (lines.length != 4) continue;

            String owner = lines[0];
            if(owner.isEmpty()) continue;
            String sellBuyOOS = lines[1];
            if(sellBuyOOS.isEmpty()) continue;

            if (!(sellBuyOOS.contains("Selling") || sellBuyOOS.contains("Buying") || sellBuyOOS.contains("Out of Stock"))) {
                Utils.debug("not selling, buying, oos");
                continue;
            }

            String item = lines[2];
            if(item.isEmpty()) continue;
            String price = lines[3];
            if(price.isEmpty()) continue;

            //Utils.debug(owner + " is " + sellBuyOOS + " " + item + " for " + price);

            int[] position = {
                    pos.getX(), pos.getY(), pos.getZ()
            };

            int action = 0;
            if (sellBuyOOS.contains("Selling")) action = 1;
            else if (sellBuyOOS.contains("Out of Stock")) action = 2;

            Matcher matcher = Pattern.compile("(Selling|Buying)\\s(\\d+)").matcher(sellBuyOOS);

            int amount = 0;
            try {
                amount = matcher.find() ? Integer.parseInt(matcher.group(2)) : 0;
            } catch(Exception e) {
                Utils.debug(e.getMessage());
            }

            int dimension = switch (world.getDimensionEntry().toString()) {
                case "minecraft:the_nether" -> 1;
                case "minecraft:the_end" -> 2;
                default -> 0;
            };

            if(!price.contains(" each")) continue;

            //Utils.debug("Dimension is " + dimension);
            ShopDataHolder shop = null;
            try {
                shop = new ShopDataHolder(owner, position, Float.parseFloat(price.substring(1).replace(" each", "").replace(",", "")), item, action, amount, dimension);
            } catch (ShopException e) {
                throw new RuntimeException(e);
            }

            ShopDataManager.addShop(shop);
        }
    }
}
