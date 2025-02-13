package io.github.kreiseljustus;

import com.google.gson.Gson;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class Asmpshopget implements ModInitializer {
	public static final String MOD_ID = "asmpshopget";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private ChunkPos lastChunkPos;

	Gson gson = new Gson();

	static Player p = null;

	List<ShopDataHolder> cachedData = new LinkedList<>();
	List<Thread> threads = new LinkedList<>();

	public static ServerData server = null;

	public static boolean onASMP() {
		Minecraft client = Minecraft.getInstance();

        server = client.getCurrentServer();
        if (server == null) return false;
        return server.ip.equals("aclu.r2f.co");
    }

	@Override
	public void onInitialize() {
		ModConfig.register();

		ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
	}


	int tickInWorld = 0;
	private void onClientTick(Minecraft client) {
		if(!ModConfig.get().enable) return;
		if(!ModConfig.get().ignoreServerRequirement && !onASMP()) return;
		Player p = client.player;
		if(p == null) return;
		this.p = p;

		tickInWorld++;

		if(ModConfig.get().ticksBetweenSends < 0) ModConfig.get().ticksBetweenSends = 600;

		if(tickInWorld % ModConfig.get().ticksBetweenSends == 0) {
			debug("clearing old threads");
			threads.clear();
			debug("Attempting sending cached shops");
			debug(cachedData.toString());
			sendPost(cachedData);
			cachedData.clear();
		}

		//Nether not supported yet
		if(p.level().dimension() == Level.NETHER) return;
		ChunkPos currentPos = new ChunkPos(p.getOnPos());
		//p.displayClientMessage(Component.literal(currentPos.x + " " + currentPos.z), false);

		if(lastChunkPos == null || !lastChunkPos.equals(currentPos)) {
			onEnterNewChunk(p, p.level(), currentPos);
			lastChunkPos = currentPos;
		}
	}

	private void debug(String message) {
		if(p == null) return;
		if(ModConfig.get().enableDebugMode) {
			p.displayClientMessage(Component.literal(message), false);
		}
	}

	private void onEnterNewChunk(Player p, Level world, ChunkPos pos) {
		debug("new chunk entered");
		Map<BlockPos, BlockEntity> blockEntities = world.getChunk(pos.x,pos.z).getBlockEntities();
		for(Map.Entry<BlockPos, BlockEntity> value : blockEntities.entrySet()) {
			BlockPos epos = value.getKey();
			BlockEntity bEntity = value.getValue();

			if(!(bEntity instanceof SignBlockEntity)) {debug("no SignBlockEntity here");continue;}

			SignBlockEntity sign = (SignBlockEntity) bEntity;
			BlockState blockState = world.getBlockState(epos);

			if(!(blockState.getBlock() instanceof SignBlock)) {debug("NoSignBlock here");continue;}

			SignText text = sign.getFrontText();
			if(!(text.getMessages(false).length == 4)) {debug("not 4 lines");continue;}

			String owner = text.getMessage(0,false).getString();
			String sellBuyOOS = text.getMessage(1,false).getString();

			if(!(sellBuyOOS.contains("Selling") || sellBuyOOS.contains("Buying") || sellBuyOOS.contains("Out of Stock"))) {debug("not selling, buying, oos");continue;}

			String item = text.getMessage(2,false).getString();
			String price = text.getMessage(3,false).getString();

			debug(owner + " is " + sellBuyOOS + " " + item + " for " + price);

			int[] position = {
				epos.getX(), epos.getY(), epos.getZ()
			};

			int action = 0;
			if(sellBuyOOS.contains("Selling")) action = 1;
			else if (sellBuyOOS.contains("Out of Stock")) action = 2;

			String regex = "(Selling|Buying)\\s(\\d+)";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(sellBuyOOS);
			int amount = 0;
			if(matcher.find()) amount = Integer.parseInt(matcher.group(2));

			int dimension = 0;

			debug(world.dimension().location().toString());
			ShopDataHolder data = new ShopDataHolder(owner, position, Float.parseFloat(price.substring(1).replace(" each", "").replace(",", "")), item, action, amount, dimension, server.ip);

			if(!cachedData.contains(data)) {
				cachedData.add(data);
			}
		}

	}

	private void sendPost(List<ShopDataHolder> data) {
		if(data.isEmpty()) return;

		if(ModConfig.get().createNewThreadPerSend) {
			Thread thread = new Thread(() -> {
				reallySendPost(data);
			});
			thread.start();
			threads.add(thread);
		} else {
			reallySendPost(data);
		}
    }

	private void reallySendPost(List<ShopDataHolder> data) {
			if(data.isEmpty()) return;
			if(ModConfig.get().postUrl == null) return;
			HttpPost post = new HttpPost(ModConfig.get().postUrl);
			try {
				CloseableHttpClient client = HttpClientBuilder.create().build();
				StringEntity postString = new StringEntity(gson.toJson(data.toArray()), ContentType.APPLICATION_JSON);
				post.setEntity(postString);
				post.setHeader("Content-Type", "application/json");
				client.execute(post);
				client.close();
			} catch (UnsupportedEncodingException e) {
				debug("Problem occured while parsing shop data");
			} catch (ClientProtocolException e) {
				debug("Wrong protocol (this will never happen)");
			} catch (IOException e) {
				debug("IOException: " + e.getMessage());
			}
	}
}