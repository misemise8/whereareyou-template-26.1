package net.misemise.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.misemise.WhereAreYou;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class WhereAreYouClientConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("whereareyou-client.json");
	private static FileData data = new FileData();
	private static String activeServerKey = "global";

	private WhereAreYouClientConfig() {
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				FileData loaded = GSON.fromJson(reader, FileData.class);
				if (loaded != null) {
					data = loaded;
				}
			} catch (IOException exception) {
				WhereAreYou.LOGGER.warn("Failed to load client config", exception);
			}
		}
		active().clamp();
		save();
	}

	public static void save() {
		active().clamp();
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(data, writer);
			}
		} catch (IOException exception) {
			WhereAreYou.LOGGER.warn("Failed to save client config", exception);
		}
	}

	public static ClientSettings active() {
		return data.servers.computeIfAbsent(activeServerKey, ignored -> new ClientSettings());
	}

	public static void setActiveServerKey(String serverKey) {
		activeServerKey = serverKey == null || serverKey.isBlank() ? "global" : serverKey;
		active().clamp();
	}

	public static String currentServerKey(Minecraft client) {
		ServerData server = client.getCurrentServer();
		if (server != null && server.ip != null && !server.ip.isBlank()) {
			return server.ip;
		}
		if (client.hasSingleplayerServer()) {
			return "singleplayer";
		}
		return "global";
	}

	private static class FileData {
		Map<String, ClientSettings> servers = new HashMap<>();
	}
}
