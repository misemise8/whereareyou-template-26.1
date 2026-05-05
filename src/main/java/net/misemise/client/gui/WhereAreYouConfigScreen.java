package net.misemise.client.gui;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.misemise.client.WhereAreYouClient;
import net.misemise.client.config.ClientSettings;
import net.misemise.client.config.WhereAreYouClientConfig;
import net.misemise.client.gui.entry.BulkDisplayEntry;
import net.misemise.client.gui.entry.DisplayPlayer;
import net.misemise.client.gui.entry.LiveIntSliderEntry;
import net.misemise.client.gui.entry.LocationEntry;
import net.misemise.client.gui.entry.LocationTableHeaderEntry;
import net.misemise.client.gui.entry.PlayerDisplayEntry;
import net.misemise.client.gui.entry.PlayerTableHeaderEntry;
import net.misemise.client.state.ClientLocationState;
import net.misemise.config.ServerRules;
import net.misemise.network.PlayerLocation;
import net.misemise.network.payload.AdminRulesPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

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
		List<DisplayPlayer> onlinePlayers = collectOnlinePlayers(client);

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable("config.whereareyou.title"));
		ConfigEntryBuilder entries = builder.entryBuilder();

		addSharing(builder, entries, settings);
		addHud(builder, entries, settings);
		addOverlay(builder, entries, settings);
		addPlayers(builder, entries, settings, onlinePlayers);
		addLocations(builder, entries, onlinePlayers);
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
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.whereareyou.category.sharing"));
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.sharing.share_location"), settings.shareLocation)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.shareLocation = value)
				.build());
		category.addEntry(entries.fillKeybindingField(Component.translatable("config.whereareyou.sharing.config_key"), WhereAreYouClient.CONFIG_KEY)
				.setTooltip(Component.translatable("config.whereareyou.sharing.key_tooltip"))
				.build());
		category.addEntry(entries.fillKeybindingField(Component.translatable("config.whereareyou.sharing.display_key"), WhereAreYouClient.DISPLAY_KEY)
				.setTooltip(Component.translatable("config.whereareyou.sharing.display_key_tooltip"))
				.build());
		category.addEntry(entries.startEnumSelector(Component.translatable("config.whereareyou.sharing.display_key_mode"), ClientSettings.DisplayKeyMode.class, settings.displayKeyMode)
				.setDefaultValue(ClientSettings.DisplayKeyMode.TOGGLE)
				.setEnumNameProvider(value -> switch ((ClientSettings.DisplayKeyMode) value) {
					case TOGGLE -> Component.translatable("config.whereareyou.display_mode.toggle");
					case HOLD -> Component.translatable("config.whereareyou.display_mode.hold");
				})
				.setSaveConsumer(value -> settings.displayKeyMode = value)
				.build());
	}

	private static void addHud(ConfigBuilder builder, ConfigEntryBuilder entries, ClientSettings settings) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.whereareyou.category.hud"));
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.hud.enabled"), settings.hudEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.hudEnabled = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.hud.show_icons"), settings.showIcon)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.showIcon = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.hud.show_mcid"), settings.showName)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.showName = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.hud.show_distance"), settings.showDistance)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.showDistance = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.hud.show_coordinates"), settings.showCoordinates)
				.setDefaultValue(false)
				.setSaveConsumer(value -> settings.showCoordinates = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.hud.show_dimension"), settings.showDimension)
				.setDefaultValue(false)
				.setSaveConsumer(value -> settings.showDimension = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.hud.group_by_dimension"), settings.groupByDimension)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.groupByDimension = value)
				.build());
		category.addEntry(entries.startEnumSelector(Component.translatable("config.whereareyou.hud.position"), ClientSettings.HudPosition.class, settings.hudPosition)
				.setDefaultValue(ClientSettings.HudPosition.TOP_LEFT)
				.setEnumNameProvider(value -> switch ((ClientSettings.HudPosition) value) {
					case TOP_LEFT -> Component.translatable("config.whereareyou.hud.position.top_left");
					case TOP_RIGHT -> Component.translatable("config.whereareyou.hud.position.top_right");
					case BOTTOM_LEFT -> Component.translatable("config.whereareyou.hud.position.bottom_left");
					case BOTTOM_RIGHT -> Component.translatable("config.whereareyou.hud.position.bottom_right");
				})
				.setSaveConsumer(value -> settings.hudPosition = value)
				.build());
		category.addEntry(new LiveIntSliderEntry(Component.translatable("config.whereareyou.hud.x_offset"), settings.hudXOffset, -200, 200, Component.translatable("config.whereareyou.reset"),
				() -> 4,
				value -> settings.hudXOffset = value,
				value -> settings.hudXOffset = value));
		category.addEntry(new LiveIntSliderEntry(Component.translatable("config.whereareyou.hud.y_offset"), settings.hudYOffset, -200, 200, Component.translatable("config.whereareyou.reset"),
				() -> 4,
				value -> settings.hudYOffset = value,
				value -> settings.hudYOffset = value));
		category.addEntry(entries.startIntSlider(Component.translatable("config.whereareyou.hud.scale"), settings.hudScale, 50, 150)
				.setDefaultValue(100)
				.setTextGetter(value -> Component.translatable("config.whereareyou.hud.scale.value", value))
				.setSaveConsumer(value -> settings.hudScale = value)
				.build());
		category.addEntry(entries.startIntSlider(Component.translatable("config.whereareyou.hud.max_players"), settings.maxPlayers, 1, 32)
				.setDefaultValue(8)
				.setSaveConsumer(value -> settings.maxPlayers = value)
				.build());
		category.addEntry(entries.startEnumSelector(Component.translatable("config.whereareyou.hud.sort_mode"), ClientSettings.SortMode.class, settings.sortMode)
				.setDefaultValue(ClientSettings.SortMode.MCID)
				.setEnumNameProvider(value -> switch ((ClientSettings.SortMode) value) {
					case MCID -> Component.translatable("config.whereareyou.sort.mcid");
					case DISTANCE -> Component.translatable("config.whereareyou.sort.distance");
				})
				.setSaveConsumer(value -> settings.sortMode = value)
				.build());
	}

	private static void addOverlay(ConfigBuilder builder, ConfigEntryBuilder entries, ClientSettings settings) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.whereareyou.category.overlay"));
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.overlay.enabled"), settings.overlayEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.overlayEnabled = value)
				.build());
		category.addEntry(entries.startEnumSelector(Component.translatable("config.whereareyou.overlay.content"), ClientSettings.OverlayContentMode.class, settings.overlayContentMode)
				.setDefaultValue(ClientSettings.OverlayContentMode.ICON_NAME_DISTANCE)
				.setEnumNameProvider(value -> switch ((ClientSettings.OverlayContentMode) value) {
					case ICON_NAME_DISTANCE -> Component.translatable("config.whereareyou.overlay.content.icon_name_distance");
					case NAME_DISTANCE -> Component.translatable("config.whereareyou.overlay.content.name_distance");
					case DISTANCE -> Component.translatable("config.whereareyou.overlay.content.distance");
				})
				.setSaveConsumer(value -> settings.overlayContentMode = value)
				.build());
		category.addEntry(entries.startIntSlider(Component.translatable("config.whereareyou.overlay.scale"), settings.overlayScale, 50, 150)
				.setDefaultValue(100)
				.setTextGetter(value -> Component.translatable("config.whereareyou.overlay.scale.value", value))
				.setSaveConsumer(value -> settings.overlayScale = value)
				.build());
		category.addEntry(entries.startIntSlider(Component.translatable("config.whereareyou.overlay.background_opacity"), settings.overlayBackgroundOpacity, 10, 100)
				.setDefaultValue(50)
				.setTextGetter(value -> Component.translatable("config.whereareyou.overlay.background_opacity.value", value))
				.setSaveConsumer(value -> settings.overlayBackgroundOpacity = value)
				.build());
		category.addEntry(entries.startTextDescription(Component.translatable("config.whereareyou.overlay.description")).build());
	}

	private static void addPlayers(ConfigBuilder builder, ConfigEntryBuilder entries, ClientSettings settings, List<DisplayPlayer> players) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.whereareyou.category.players"));
		category.addEntry(new BulkDisplayEntry(settings, players));
		category.addEntry(new PlayerTableHeaderEntry());
		if (players.isEmpty()) {
			category.addEntry(entries.startTextDescription(Component.translatable("config.whereareyou.players.none")).build());
			return;
		}
		for (DisplayPlayer player : players) {
			category.addEntry(new PlayerDisplayEntry(player, settings.displayFor(player.uuid(), player.localPlayer())));
		}
	}

	private static void addLocations(ConfigBuilder builder, ConfigEntryBuilder entries, List<DisplayPlayer> onlinePlayers) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.whereareyou.category.locations"));
		Map<UUID, PlayerLocation> locations = new LinkedHashMap<>();
		for (PlayerLocation location : ClientLocationState.locations()) {
			locations.put(location.uuid(), location);
		}
		List<DisplayPlayer> players = mergeLocationPlayers(onlinePlayers, locations);
		if (players.isEmpty()) {
			category.addEntry(entries.startTextDescription(Component.translatable("config.whereareyou.players.none")).build());
			return;
		}
		category.addEntry(new LocationTableHeaderEntry());
		for (DisplayPlayer player : players) {
			category.addEntry(new LocationEntry(player, locations.get(player.uuid())));
		}
	}

	private static void addAdmin(ConfigBuilder builder, ConfigEntryBuilder entries, ServerRules draft) {
		ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.whereareyou.category.admin"));
		if (!ClientLocationState.canAdmin()) {
			category.addEntry(entries.startTextDescription(Component.translatable("config.whereareyou.admin.op_only")).build());
			return;
		}
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.admin.enabled"), draft.enabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.enabled = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.admin.default_sharing"), draft.defaultSharing)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.defaultSharing = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.admin.allow_coordinates"), draft.allowCoordinates)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.allowCoordinates = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.admin.allow_distance"), draft.allowDistance)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.allowDistance = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.translatable("config.whereareyou.admin.allow_dimension"), draft.allowDimension)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.allowDimension = value)
				.build());
		category.addEntry(entries.startIntSlider(Component.translatable("config.whereareyou.admin.sync_interval"), draft.syncIntervalTicks, ServerRules.MIN_SYNC_INTERVAL_TICKS, ServerRules.MAX_SYNC_INTERVAL_TICKS)
				.setDefaultValue(ServerRules.DEFAULT_SYNC_INTERVAL_TICKS)
				.setTextGetter(value -> Component.translatable("config.whereareyou.admin.sync_interval.value", value, String.format("%.1f", value / 20.0D)))
				.setSaveConsumer(value -> draft.syncIntervalTicks = value)
				.build());
	}

	private static List<DisplayPlayer> collectOnlinePlayers(Minecraft client) {
		Map<UUID, DisplayPlayer> players = new LinkedHashMap<>();
		Map<UUID, DisplayPlayer> discovered = new LinkedHashMap<>();
		UUID localUuid = client.player == null ? null : client.player.getUUID();
		if (client.player != null) {
			if (client.player.getGameProfile().id() != null) {
				addPlayer(players, client.player.getGameProfile().id(), client.player.getGameProfile().name(), localUuid);
			}
		}
		if (client.level != null) {
			for (AbstractClientPlayer player : client.level.players()) {
				addPlayer(discovered, player.getGameProfile().id(), player.getGameProfile().name(), localUuid);
			}
		}
		if (client.getConnection() != null) {
			List<PlayerInfo> online = new ArrayList<>(client.getConnection().getOnlinePlayers());
			online.sort(Comparator.comparing(info -> info.getProfile().name(), String.CASE_INSENSITIVE_ORDER));
			for (PlayerInfo info : online) {
				addPlayer(discovered, info.getProfile().id(), info.getProfile().name(), localUuid);
			}
		}
		MinecraftServer server = client.getSingleplayerServer();
		if (server != null) {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				addPlayer(discovered, player.getGameProfile().id(), player.getGameProfile().name(), localUuid);
			}
		}
		for (PlayerLocation location : ClientLocationState.locations()) {
			addPlayer(discovered, location.uuid(), location.name(), localUuid);
		}
		List<DisplayPlayer> sortedDiscovered = new ArrayList<>(discovered.values());
		sortedDiscovered.sort(Comparator.comparing(DisplayPlayer::name, String.CASE_INSENSITIVE_ORDER));
		for (DisplayPlayer player : sortedDiscovered) {
			players.putIfAbsent(player.uuid(), player);
		}
		return new ArrayList<>(players.values());
	}

	private static List<DisplayPlayer> mergeLocationPlayers(List<DisplayPlayer> onlinePlayers, Map<UUID, PlayerLocation> locations) {
		Map<UUID, DisplayPlayer> players = new LinkedHashMap<>();
		for (DisplayPlayer player : onlinePlayers) {
			players.put(player.uuid(), player);
		}
		for (PlayerLocation location : locations.values()) {
			players.putIfAbsent(location.uuid(), new DisplayPlayer(location.uuid(), location.name(), isLocalPlayer(location.uuid())));
		}
		return new ArrayList<>(players.values());
	}

	private static void addPlayer(Map<UUID, DisplayPlayer> players, UUID uuid, String name, UUID localUuid) {
		if (uuid == null || name == null || name.isBlank()) {
			return;
		}
		players.putIfAbsent(uuid, new DisplayPlayer(uuid, name, uuid.equals(localUuid)));
	}

	private static boolean isLocalPlayer(UUID uuid) {
		Minecraft client = Minecraft.getInstance();
		return client.player != null && client.player.getUUID().equals(uuid);
	}
}
