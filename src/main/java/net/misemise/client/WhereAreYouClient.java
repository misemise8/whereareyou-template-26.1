package net.misemise.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.misemise.WhereAreYou;
import net.misemise.client.config.ClientSettings;
import net.misemise.client.config.WhereAreYouClientConfig;
import net.misemise.client.gui.WhereAreYouConfigScreen;
import net.misemise.client.render.WhereAreYouHud;
import net.misemise.client.render.WhereAreYouWorldOverlay;
import net.misemise.client.state.ClientLocationState;
import net.misemise.network.payload.AdminRulesPayload;
import net.misemise.network.payload.LocationUpdatePayload;
import net.misemise.network.payload.RequestSyncPayload;
import net.misemise.network.payload.ServerRulesPayload;
import net.misemise.network.payload.SharePreferencePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class WhereAreYouClient implements ClientModInitializer {
	public static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(WhereAreYou.id("keys"));
	public static KeyMapping CONFIG_KEY;
	public static KeyMapping DISPLAY_KEY;

	@Override
	public void onInitializeClient() {
		WhereAreYouClientConfig.load();
		registerKeys();
		registerNetworking();
		HudElementRegistry.addLast(WhereAreYou.id("hud"), WhereAreYouHud::render);
		LevelRenderEvents.COLLECT_SUBMITS.register(WhereAreYouWorldOverlay::render);
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
	}

	public static void openConfigScreen(Minecraft client) {
		client.setScreen(WhereAreYouConfigScreen.create(client.screen));
	}

	public static void sendSharePreference() {
		if (ClientPlayNetworking.canSend(SharePreferencePayload.TYPE)) {
			ClientPlayNetworking.send(new SharePreferencePayload(WhereAreYouClientConfig.active().shareLocation));
		}
	}

	public static void sendAdminRules(AdminRulesPayload payload) {
		if (ClientPlayNetworking.canSend(AdminRulesPayload.TYPE)) {
			ClientPlayNetworking.send(payload);
		}
	}

	private void registerKeys() {
		CONFIG_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.whereareyou.config",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				KEY_CATEGORY
		));
		DISPLAY_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.whereareyou.display",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_B,
				KEY_CATEGORY
		));
	}

	private void registerNetworking() {
		ClientPlayNetworking.registerGlobalReceiver(LocationUpdatePayload.TYPE, (payload, context) -> ClientLocationState.updateLocations(payload));
		ClientPlayNetworking.registerGlobalReceiver(ServerRulesPayload.TYPE, (payload, context) -> ClientLocationState.updateServerRules(payload));
		ClientPlayConnectionEvents.JOIN.register((listener, sender, client) -> {
			WhereAreYouClientConfig.setActiveServerKey(WhereAreYouClientConfig.currentServerKey(client));
			ClientLocationState.clear();
			sendSharePreference();
			if (ClientPlayNetworking.canSend(RequestSyncPayload.TYPE)) {
				ClientPlayNetworking.send(new RequestSyncPayload());
			}
		});
		ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> {
			ClientLocationState.clear();
			WhereAreYouClientConfig.setActiveServerKey("global");
		});
	}

	private void tick(Minecraft client) {
		while (CONFIG_KEY.consumeClick()) {
			openConfigScreen(client);
		}

		ClientSettings settings = WhereAreYouClientConfig.active();
		if (settings.displayKeyMode == ClientSettings.DisplayKeyMode.HOLD) {
			ClientLocationState.setHoldHidden(DISPLAY_KEY.isDown());
			while (DISPLAY_KEY.consumeClick()) {
				// Consume clicks so switching modes later does not replay stale presses.
			}
		} else {
			ClientLocationState.setHoldHidden(false);
			while (DISPLAY_KEY.consumeClick()) {
				ClientLocationState.toggleHidden();
			}
		}
	}
}
