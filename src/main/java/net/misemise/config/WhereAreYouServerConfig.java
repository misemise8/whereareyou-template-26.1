package net.misemise.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.misemise.WhereAreYou;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WhereAreYouServerConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("whereareyou-server.json");
	private static ServerRules rules = new ServerRules();

	private WhereAreYouServerConfig() {
	}

	public static ServerRules rules() {
		return rules;
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader reader = Files.newBufferedReader(PATH)) {
				ServerRules loaded = GSON.fromJson(reader, ServerRules.class);
				if (loaded != null) {
					rules = loaded;
				}
			} catch (IOException exception) {
				WhereAreYou.LOGGER.warn("Failed to load server config", exception);
			}
		}
		rules.clamp();
		save();
	}

	public static void update(ServerRules next) {
		next.clamp();
		rules = next;
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(rules, writer);
			}
		} catch (IOException exception) {
			WhereAreYou.LOGGER.warn("Failed to save server config", exception);
		}
	}
}
