package io.github.kreiseljustus;

import com.google.gson.Gson;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
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
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
public class Asmpshopget implements ModInitializer {
	public static final String MOD_ID = "asmpshopget";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final String VERSION = "1.0.11";
	public static final String VERSION_URL = "https://kreiseljustus.com/asmp_version.txt";

	public static boolean using_latest;
	public static boolean warning_given = false;

	private ChunkPos lastChunkPos;

	Gson gson = new Gson();

	static Player p = null;

	List<ShopDataHolder> cachedData = new LinkedList<>();
	private final ExecutorService threadPool = Executors.newFixedThreadPool(4);

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

		try{
			String latestVersion = fetchVersionFromUrl(VERSION_URL);
			if (latestVersion != null && isNewerVersion(latestVersion, VERSION)) {
				System.out.println("A newer version is available: " + latestVersion);
				using_latest = false;
			} else {
				System.out.println("You are using the latest version: " + VERSION);
				using_latest = true;
			}
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


	int tickInWorld = 0;
	private void onClientTick(Minecraft client) {
		if(!ModConfig.get().enable) return;
		if(ModConfig.get().onlyEnableOnASMP && !onASMP()) return;
		Player p = client.player;
		if(p == null) return;
		this.p = p;

		if(!using_latest && !warning_given) {p.displayClientMessage(Component.literal("Your version is outdated! You wont send any data until the mod is updated!").withStyle(ChatFormatting.RED), false); warning_given = true; return; }

		tickInWorld++;

		if(ModConfig.get().ticksBetweenSends < 0) ModConfig.get().ticksBetweenSends = 600;

		if(tickInWorld % ModConfig.get().ticksBetweenSends == 0) {
			if(!using_latest) {debug("Discarding... not up-to date!"); return;}
			debug("Attempting sending cached shops");
			debug(cachedData.toString());
			List<ShopDataHolder> dataToSend = new ArrayList<>(cachedData);

			sendPost(dataToSend);
			cachedData.clear();
		}

		if(tickInWorld % 6000 == 0) {
			threadPool.submit(() -> {
				try {
					String latestVersion = fetchVersionFromUrl(VERSION_URL);
					if (latestVersion != null && isNewerVersion(latestVersion, VERSION)) {
						System.out.println("A newer version is available: " + latestVersion);
						using_latest = false;
					} else {
						System.out.println("You are using the latest version: " + VERSION);
						using_latest = true;
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				if (!using_latest && !warning_given) {
					p.displayClientMessage(Component.literal("Your version is outdated! You wont send any data until the mod is updated!").withStyle(ChatFormatting.RED), false);
					warning_given = true;
				}
			});
		}

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
			if(world.dimension().location().toString().equals("minecraft:the_nether")) dimension = 1;
			else if (world.dimension().location().toString().equals("minecraft:the_end")) dimension = 2;
			debug("Dimesnion is " + dimension);
			ShopDataHolder data = new ShopDataHolder(owner, position, Float.parseFloat(price.substring(1).replace(" each", "").replace(",", "")), item, action, amount, dimension, server == null ? "Singleplayer " : server.ip);

			if(!cachedData.contains(data)) {
				cachedData.add(data);
			}
		}

	}

	private void sendPost(List<ShopDataHolder> data) {
		if(data.isEmpty()) return;

		if (ModConfig.get().createNewThreadPerSend) {
			threadPool.submit(() -> reallySendPost(data));
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

	private static String fetchVersionFromUrl(String versionUrl) throws Exception {
		URL url = new URL(versionUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			return reader.readLine(); // Assuming the version is on the first line
		}
	}

	private static boolean isNewerVersion(String latest, String current) {
		int[] latestParts = parseVersion(latest);
		int[] currentParts = parseVersion(current);

		for (int i = 0; i < latestParts.length; i++) {
			if (latestParts[i] > currentParts[i]) {
				return true;
			} else if (latestParts[i] < currentParts[i]) {
				return false;
			}
		}
		return false;
	}

	private static int[] parseVersion(String version) {
		Matcher matcher = Pattern.compile("\\d+").matcher(version);
		int[] parts = new int[3]; // Assuming major.minor.patch format
		int index = 0;

		while (matcher.find() && index < 3) {
			parts[index++] = Integer.parseInt(matcher.group());
		}
		return parts;
	}
}