package io.github.kreiseljustus.asmputils.core.modules.shop;

import io.github.kreiseljustus.asmputils.*;
import io.github.kreiseljustus.asmputils.core.ModrinthVersionManagement;
import io.github.kreiseljustus.asmputils.core.modules.IModule;
import io.github.kreiseljustus.asmputils.core.Sender;
import io.github.kreiseljustus.asmputils.core.Utils;
import io.github.kreiseljustus.asmputils.core.data.ShopDataHolder;
import io.github.kreiseljustus.asmputils.core.data.ShopDataManager;
import io.github.kreiseljustus.asmputils.core.data.ShopException;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopModule implements IModule{

    boolean enabled = true;

    static List<ShopDataHolder> foundShops = new ArrayList<>();

    public static void handleShopDetection(ChunkPos currentChunk) {
        World world = Asmputils.s_Player.getWorld();

        Chunk chunk = world.getChunk(currentChunk.getStartPos());

        int dimension = switch (world.getDimensionEntry().getIdAsString()) {
            case "minecraft:the_nether" -> 1;
            case "minecraft:the_end" -> 2;
            default -> 0;
        };

        for (BlockPos pos : chunk.getBlockEntityPositions()) {
            BlockEntity entity = world.getBlockEntity(pos);

            if (!(entity instanceof SignBlockEntity)) {
                continue;
            }

            SignBlockEntity sign = (SignBlockEntity) entity;
            BlockState blockState = world.getBlockState(pos);

            Utils.debug("Block at pos: " + pos + " is " + blockState.getBlock().getTranslationKey());

            if (!(blockState.getBlock() instanceof SignBlock || blockState.getBlock() instanceof WallSignBlock)) {
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

            Utils.debug(owner + " is " + sellBuyOOS + " " + item + " for " + price);

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

            Utils.debug(world.getDimensionEntry().getIdAsString());
            Utils.debug(String.valueOf(dimension));

            if(!price.contains(" each")) continue;

            ShopDataHolder shop = null;
            try {
                shop = new ShopDataHolder(owner, position, Float.parseFloat(price.substring(1).replace(" each", "").replace(",", "")), item, action, amount, dimension, LocalDateTime.now());
            } catch (ShopException e) {
                throw new RuntimeException(e);
            }

            ShopDataManager.addShop(shop);
            foundShops.add(shop);
        }

        List<ShopDataHolder> shops = ServerValidator.getExpectedShopsInChunk(chunk.getPos().x, chunk.getPos().z, dimension);

        for(ShopDataHolder expectedShop : shops) {
            if(foundShops.contains(expectedShop)) continue;

            //Send update to server
            if(!ModrinthVersionManagement.updateAvailable) {
                Sender.sendDeleteRequest(expectedShop);
            }
        }

        foundShops.clear();
    }

    @Override
    public String getModuleName() {
        return "ShopModule";
    }

    @Override
    public void onInitClient() {

    }

    @Override
    public void onTick(boolean enabled, int totalTicks) {
        this.enabled = enabled;
        if(!enabled) return;

        if(Asmputils.s_TicksInASMPServer % Asmputils.s_Config.ticksBetweenSends == 0) {
            if(!ModrinthVersionManagement.updateAvailable) {
                Sender.sendCachedShopData();
            } else Utils.debug("Update available, not sending possibly invalid data!");
            ShopDataManager.s_CachedShops.clear();
        }
    }

    @Override
    public void onChunkEnter(ChunkPos chunkPos) {
        if(!enabled) return;
        if(Asmputils.s_Config.trackShops) {
            handleShopDetection(chunkPos);
        }
    }

    @Override
    public void onStop() {

    }
}
