package net.misemise.client.gui;

import com.mojang.authlib.GameProfile;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.misemise.client.WhereAreYouClient;
import net.misemise.client.config.ClientSettings;
import net.misemise.client.config.WhereAreYouClientConfig;
import net.misemise.client.gui.entry.BulkDisplayEntry;
import net.misemise.client.gui.entry.LiveIntSliderEntry;
import net.misemise.client.gui.entry.LocationEntry;
import net.misemise.client.gui.entry.PlayerDisplayEntry;
import net.misemise.client.state.ClientLocationState;
import net.misemise.config.ServerRules;
import net.misemise.network.PlayerLocation;
import net.misemise.network.payload.AdminRulesPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WhereAreYouConfigScreen {
	private WhereAreYouConfigScreen() {
	}

	public static Screen create(Screen parent) {
		Minecraft client = Minecraft.getInstance();
		ClientSettings settings = WhereAreYouClientConfig.active();
		ServerRules adminDraft = ClientLocationState.serverRules();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.literal("WhereAreYou"));
		ConfigEntryBuilder entries = builder.entryBuilder();

		addSharing(builder, entries, settings);
		addHud(builder, entries, settings);
		addOverlay(builder, entries, settings);
		addPlayers(builder, entries, settings, collectOnlinePlayers(client));
		addLocations(builder, entries);
		addAdmin(builder, entries, adminDraft);

		builder.setSavingRunnable(() -> {
			WhereAreYouClientConfig.save();
			client.options.save();
			WhereAreYouClient.sendSharePreference();
			if (ClientLocationState.canAdmin()) {
				WhereAreYouClient.sendAdminRules(AdminRulesPayload.from(adminDraft));
			}
		});
		return builder.build();
	}

	private static void addSharing(ConfigBuilder builder, ConfigEntryBuilder entries, ClientSettings settings) {
		ConfigCategory category = builder.getOrCreateCategory(Component.literal("Sharing"));
		category.addEntry(entries.startBooleanToggle(Component.literal("Share my location"), settings.shareLocation)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.shareLocation = value)
				.build());
		category.addEntry(entries.fillKeybindingField(Component.literal("Open config key"), WhereAreYouClient.CONFIG_KEY)
				.setTooltip(Component.literal("Also changes the Minecraft key binding."))
				.build());
		category.addEntry(entries.fillKeybindingField(Component.literal("Temporary display key"), WhereAreYouClient.DISPLAY_KEY)
				.setTooltip(Component.literal("Temporarily hides/shows the HUD and overlay together."))
				.build());
		category.addEntry(entries.startEnumSelector(Component.literal("Temporary display mode"), ClientSettings.DisplayKeyMode.class, settings.displayKeyMode)
				.setDefaultValue(ClientSettings.DisplayKeyMode.TOGGLE)
				.setEnumNameProvider(value -> switch ((ClientSettings.DisplayKeyMode) value) {
					case TOGGLE -> Component.literal("Toggle");
					case HOLD -> Component.literal("Hold to hide");
				})
				.setSaveConsumer(value -> settings.displayKeyMode = value)
				.build());
	}

	private static void addHud(ConfigBuilder builder, ConfigEntryBuilder entries, ClientSettings settings) {
		ConfigCategory category = builder.getOrCreateCategory(Component.literal("HUD"));
		category.addEntry(entries.startBooleanToggle(Component.literal("HUD enabled"), settings.hudEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.hudEnabled = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("Show icons"), settings.showIcon)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.showIcon = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("Show MCID"), settings.showName)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.showName = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("Show distance"), settings.showDistance)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.showDistance = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("Show coordinates"), settings.showCoordinates)
				.setDefaultValue(false)
				.setSaveConsumer(value -> settings.showCoordinates = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("Show dimension"), settings.showDimension)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.showDimension = value)
				.build());
		category.addEntry(entries.startEnumSelector(Component.literal("HUD position"), ClientSettings.HudPosition.class, settings.hudPosition)
				.setDefaultValue(ClientSettings.HudPosition.TOP_LEFT)
				.setEnumNameProvider(value -> switch ((ClientSettings.HudPosition) value) {
					case TOP_LEFT -> Component.literal("Top left");
					case TOP_RIGHT -> Component.literal("Top right");
					case BOTTOM_LEFT -> Component.literal("Bottom left");
					case BOTTOM_RIGHT -> Component.literal("Bottom right");
				})
				.setSaveConsumer(value -> settings.hudPosition = value)
				.build());
		category.addEntry(new LiveIntSliderEntry(Component.literal("X offset"), settings.hudXOffset, -200, 200, Component.literal("Reset"),
				() -> 4,
				value -> settings.hudXOffset = value,
				value -> settings.hudXOffset = value));
		category.addEntry(new LiveIntSliderEntry(Component.literal("Y offset"), settings.hudYOffset, -200, 200, Component.literal("Reset"),
				() -> 4,
				value -> settings.hudYOffset = value,
				value -> settings.hudYOffset = value));
		category.addEntry(entries.startIntSlider(Component.literal("HUD scale"), settings.hudScale, 50, 150)
				.setDefaultValue(100)
				.setTextGetter(value -> Component.literal(value + "%"))
				.setSaveConsumer(value -> settings.hudScale = value)
				.build());
		category.addEntry(entries.startIntSlider(Component.literal("Max visible players"), settings.maxPlayers, 1, 32)
				.setDefaultValue(8)
				.setSaveConsumer(value -> settings.maxPlayers = value)
				.build());
		category.addEntry(entries.startEnumSelector(Component.literal("Sort mode"), ClientSettings.SortMode.class, settings.sortMode)
				.setDefaultValue(ClientSettings.SortMode.MCID)
				.setEnumNameProvider(value -> switch ((ClientSettings.SortMode) value) {
					case MCID -> Component.literal("MCID");
					case DISTANCE -> Component.literal("Distance");
				})
				.setSaveConsumer(value -> settings.sortMode = value)
				.build());
	}

	private static void addOverlay(ConfigBuilder builder, ConfigEntryBuilder entries, ClientSettings settings) {
		ConfigCategory category = builder.getOrCreateCategory(Component.literal("Overlay"));
		category.addEntry(entries.startBooleanToggle(Component.literal("Overlay enabled"), settings.overlayEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.overlayEnabled = value)
				.build());
		category.addEntry(entries.startTextDescription(Component.literal("Loaded players are labeled above their nameplate. Unloaded players fall back to a screen-edge marker.")).build());
	}

	private static void addPlayers(ConfigBuilder builder, ConfigEntryBuilder entries, ClientSettings settings, List<GameProfile> profiles) {
		ConfigCategory category = builder.getOrCreateCategory(Component.literal("Players"));
		category.addEntry(new BulkDisplayEntry(settings, profiles));
		category.addEntry(entries.startTextDescription(Component.literal("Player                         HUD       Overlay")).build());
		for (GameProfile profile : profiles) {
			if (profile.id() != null) {
				boolean local = isLocalPlayer(profile.id());
				category.addEntry(new PlayerDisplayEntry(profile, settings.displayFor(profile.id(), local), local));
			}
		}
	}

	private static void addLocations(ConfigBuilder builder, ConfigEntryBuilder entries) {
		ConfigCategory category = builder.getOrCreateCategory(Component.literal("Locations"));
		if (ClientLocationState.locations().isEmpty()) {
			category.addEntry(entries.startTextDescription(Component.literal("No shared online players yet.")).build());
			return;
		}
		category.addEntry(entries.startTextDescription(Component.literal("Player                         Location")).build());
		for (PlayerLocation location : ClientLocationState.sortedLocations(Comparator.comparing(PlayerLocation::name, String.CASE_INSENSITIVE_ORDER))) {
			category.addEntry(new LocationEntry(location));
		}
	}

	private static void addAdmin(ConfigBuilder builder, ConfigEntryBuilder entries, ServerRules draft) {
		ConfigCategory category = builder.getOrCreateCategory(Component.literal("Admin"));
		if (!ClientLocationState.canAdmin()) {
			category.addEntry(entries.startTextDescription(Component.literal("Only Minecraft OPs can change server settings.")).build());
			return;
		}
		category.addEntry(entries.startBooleanToggle(Component.literal("Location sharing system"), draft.enabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.enabled = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("Default sharing"), draft.defaultSharing)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.defaultSharing = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("Allow coordinates"), draft.allowCoordinates)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.allowCoordinates = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("Allow distance"), draft.allowDistance)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.allowDistance = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("Allow dimension"), draft.allowDimension)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.allowDimension = value)
				.build());
		category.addEntry(entries.startIntSlider(Component.literal("Position update interval"), draft.syncIntervalTicks, ServerRules.MIN_SYNC_INTERVAL_TICKS, ServerRules.MAX_SYNC_INTERVAL_TICKS)
				.setDefaultValue(ServerRules.DEFAULT_SYNC_INTERVAL_TICKS)
				.setTextGetter(value -> Component.literal(value + " ticks (" + String.format("%.1f", value / 20.0D) + "s)"))
				.setSaveConsumer(value -> draft.syncIntervalTicks = value)
				.build());
	}

	private static List<GameProfile> collectOnlinePlayers(Minecraft client) {
		Map<UUID, GameProfile> profiles = new LinkedHashMap<>();
		if (client.player != null) {
			GameProfile self = client.player.getGameProfile();
			if (self.id() != null) {
				profiles.put(self.id(), self);
			}
		}
		if (client.getConnection() != null) {
			List<PlayerInfo> online = new ArrayList<>(client.getConnection().getOnlinePlayers());
			online.sort(Comparator.comparing(info -> info.getProfile().name(), String.CASE_INSENSITIVE_ORDER));
			for (PlayerInfo info : online) {
				GameProfile profile = info.getProfile();
				if (profile.id() != null) {
					profiles.putIfAbsent(profile.id(), profile);
				}
			}
		}
		return new ArrayList<>(profiles.values());
	}

	private static boolean isLocalPlayer(UUID uuid) {
		Minecraft client = Minecraft.getInstance();
		return client.player != null && client.player.getUUID().equals(uuid);
	}
}
