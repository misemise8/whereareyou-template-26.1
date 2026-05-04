package net.misemise;

import net.fabricmc.api.ModInitializer;
import net.misemise.config.WhereAreYouServerConfig;
import net.misemise.network.WhereAreYouNetworking;
import net.misemise.server.WhereAreYouServer;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhereAreYou implements ModInitializer {
	public static final String MOD_ID = "whereareyou";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		WhereAreYouServerConfig.load();
		WhereAreYouNetworking.registerPayloadTypes();
		WhereAreYouServer.init();
		LOGGER.info("WhereAreYou initialized");
	}
}
