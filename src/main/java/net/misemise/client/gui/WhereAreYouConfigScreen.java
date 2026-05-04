package net.misemise.client.gui;

import com.mojang.authlib.GameProfile;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.misemise.client.WhereAreYouClient;
import net.misemise.client.config.ClientSettings;
import net.misemise.client.config.WhereAreYouClientConfig;
import net.misemise.client.state.ClientLocationState;
import net.misemise.config.ServerRules;
import net.misemise.network.PlayerLocation;
import net.misemise.network.payload.AdminRulesPayload;

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
		ConfigCategory category = builder.getOrCreateCategory(Component.literal("共有"));
		category.addEntry(entries.startBooleanToggle(Component.literal("自分の位置を共有する"), settings.shareLocation)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.shareLocation = value)
				.build());
		category.addEntry(entries.fillKeybindingField(Component.literal("設定画面キー"), WhereAreYouClient.CONFIG_KEY)
				.setTooltip(Component.literal("Minecraftのキー設定にも反映されます。"))
				.build());
		category.addEntry(entries.fillKeybindingField(Component.literal("表示一時切替キー"), WhereAreYouClient.DISPLAY_KEY)
				.setTooltip(Component.literal("HUDとオーバーレイをまとめて一時ON/OFFします。"))
				.build());
		category.addEntry(entries.startEnumSelector(Component.literal("表示切替キーの動作"), ClientSettings.DisplayKeyMode.class, settings.displayKeyMode)
				.setDefaultValue(ClientSettings.DisplayKeyMode.TOGGLE)
				.setEnumNameProvider(value -> switch ((ClientSettings.DisplayKeyMode) value) {
					case TOGGLE -> Component.literal("トグル");
					case HOLD -> Component.literal("ホールド中だけ非表示");
				})
				.setSaveConsumer(value -> settings.displayKeyMode = value)
				.build());
	}

	private static void addHud(ConfigBuilder builder, ConfigEntryBuilder entries, ClientSettings settings) {
		ConfigCategory category = builder.getOrCreateCategory(Component.literal("HUD"));
		category.addEntry(entries.startBooleanToggle(Component.literal("HUD表示"), settings.hudEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.hudEnabled = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("アイコン表示"), settings.showIcon)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.showIcon = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("MCID表示"), settings.showName)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.showName = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("距離表示"), settings.showDistance)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.showDistance = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("座標表示"), settings.showCoordinates)
				.setDefaultValue(false)
				.setSaveConsumer(value -> settings.showCoordinates = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("ディメンション表示"), settings.showDimension)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.showDimension = value)
				.build());
		category.addEntry(entries.startEnumSelector(Component.literal("HUD位置"), ClientSettings.HudPosition.class, settings.hudPosition)
				.setDefaultValue(ClientSettings.HudPosition.TOP_LEFT)
				.setEnumNameProvider(value -> switch ((ClientSettings.HudPosition) value) {
					case TOP_LEFT -> Component.literal("左上");
					case TOP_RIGHT -> Component.literal("右上");
					case BOTTOM_LEFT -> Component.literal("左下");
					case BOTTOM_RIGHT -> Component.literal("右下");
				})
				.setSaveConsumer(value -> settings.hudPosition = value)
				.build());
		category.addEntry(entries.startIntSlider(Component.literal("Xオフセット"), settings.hudXOffset, -200, 200)
				.setDefaultValue(4)
				.setSaveConsumer(value -> settings.hudXOffset = value)
				.build());
		category.addEntry(entries.startIntSlider(Component.literal("Yオフセット"), settings.hudYOffset, -200, 200)
				.setDefaultValue(4)
				.setSaveConsumer(value -> settings.hudYOffset = value)
				.build());
		category.addEntry(entries.startIntSlider(Component.literal("HUDスケール"), settings.hudScale, 50, 150)
				.setDefaultValue(100)
				.setTextGetter(value -> Component.literal(value + "%"))
				.setSaveConsumer(value -> settings.hudScale = value)
				.build());
		category.addEntry(entries.startIntSlider(Component.literal("最大表示人数"), settings.maxPlayers, 1, 32)
				.setDefaultValue(8)
				.setSaveConsumer(value -> settings.maxPlayers = value)
				.build());
		category.addEntry(entries.startEnumSelector(Component.literal("並び順"), ClientSettings.SortMode.class, settings.sortMode)
				.setDefaultValue(ClientSettings.SortMode.MCID)
				.setEnumNameProvider(value -> switch ((ClientSettings.SortMode) value) {
					case MCID -> Component.literal("MCID順");
					case DISTANCE -> Component.literal("距離順");
				})
				.setSaveConsumer(value -> settings.sortMode = value)
				.build());
	}

	private static void addOverlay(ConfigBuilder builder, ConfigEntryBuilder entries, ClientSettings settings) {
		ConfigCategory category = builder.getOrCreateCategory(Component.literal("Overlay"));
		category.addEntry(entries.startBooleanToggle(Component.literal("オーバーレイ表示"), settings.overlayEnabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> settings.overlayEnabled = value)
				.build());
		category.addEntry(entries.startTextDescription(Component.literal("オーバーレイは同じディメンションにいる表示ONプレイヤーだけを、画面端の小さな方向マーカーで示します。最大人数はHUDと同じ設定を使います。")).build());
	}

	private static void addPlayers(ConfigBuilder builder, ConfigEntryBuilder entries, ClientSettings settings, List<GameProfile> profiles) {
		ConfigCategory category = builder.getOrCreateCategory(Component.literal("Players"));
		category.addEntry(entries.startBooleanToggle(Component.literal("一括: 全員HUD ON"), false)
				.setSaveConsumer(value -> {
					if (value) {
						for (GameProfile profile : profiles) {
							settings.displayFor(profile.id(), isLocalPlayer(profile.id())).hud = true;
						}
					}
				})
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("一括: 全員HUD OFF"), false)
				.setSaveConsumer(value -> {
					if (value) {
						for (GameProfile profile : profiles) {
							settings.displayFor(profile.id(), isLocalPlayer(profile.id())).hud = false;
						}
					}
				})
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("一括: 全員Overlay ON"), false)
				.setSaveConsumer(value -> {
					if (value) {
						for (GameProfile profile : profiles) {
							settings.displayFor(profile.id(), isLocalPlayer(profile.id())).overlay = true;
						}
					}
				})
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("一括: 全員Overlay OFF"), false)
				.setSaveConsumer(value -> {
					if (value) {
						for (GameProfile profile : profiles) {
							settings.displayFor(profile.id(), isLocalPlayer(profile.id())).overlay = false;
						}
					}
				})
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("一括: 全員表示 ON"), false)
				.setSaveConsumer(value -> {
					if (value) {
						for (GameProfile profile : profiles) {
							ClientSettings.PlayerDisplay display = settings.displayFor(profile.id(), isLocalPlayer(profile.id()));
							display.hud = true;
							display.overlay = true;
						}
					}
				})
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("一括: 全員表示 OFF"), false)
				.setSaveConsumer(value -> {
					if (value) {
						for (GameProfile profile : profiles) {
							ClientSettings.PlayerDisplay display = settings.displayFor(profile.id(), isLocalPlayer(profile.id()));
							display.hud = false;
							display.overlay = false;
						}
					}
				})
				.build());

		for (GameProfile profile : profiles) {
			if (profile.id() == null) {
				continue;
			}
			boolean local = isLocalPlayer(profile.id());
			ClientSettings.PlayerDisplay display = settings.displayFor(profile.id(), local);
			String label = (local ? "自分: " : "") + profile.name();
			category.addEntry(entries.startBooleanToggle(Component.literal(label + " / HUD"), display.hud)
					.setDefaultValue(!local)
					.setSaveConsumer(value -> display.hud = value)
					.build());
			category.addEntry(entries.startBooleanToggle(Component.literal(label + " / Overlay"), display.overlay)
					.setDefaultValue(!local)
					.setSaveConsumer(value -> display.overlay = value)
					.build());
		}
	}

	private static void addLocations(ConfigBuilder builder, ConfigEntryBuilder entries) {
		ConfigCategory category = builder.getOrCreateCategory(Component.literal("Locations"));
		if (ClientLocationState.locations().isEmpty()) {
			category.addEntry(entries.startTextDescription(Component.literal("共有中のオンラインプレイヤーはまだいません。")).build());
			return;
		}
		for (PlayerLocation location : ClientLocationState.sortedLocations(Comparator.comparing(PlayerLocation::name, String.CASE_INSENSITIVE_ORDER))) {
			category.addEntry(entries.startTextDescription(Component.literal(formatLocation(location))).build());
		}
	}

	private static void addAdmin(ConfigBuilder builder, ConfigEntryBuilder entries, ServerRules draft) {
		ConfigCategory category = builder.getOrCreateCategory(Component.literal("Admin"));
		if (!ClientLocationState.canAdmin()) {
			category.addEntry(entries.startTextDescription(Component.literal("MinecraftのOPだけがサーバー設定を変更できます。")).build());
			return;
		}
		category.addEntry(entries.startBooleanToggle(Component.literal("位置共有機能"), draft.enabled)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.enabled = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("デフォルト共有"), draft.defaultSharing)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.defaultSharing = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("座標共有を許可"), draft.allowCoordinates)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.allowCoordinates = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("距離共有を許可"), draft.allowDistance)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.allowDistance = value)
				.build());
		category.addEntry(entries.startBooleanToggle(Component.literal("ディメンション共有を許可"), draft.allowDimension)
				.setDefaultValue(true)
				.setSaveConsumer(value -> draft.allowDimension = value)
				.build());
		category.addEntry(entries.startIntSlider(Component.literal("位置更新頻度"), draft.syncIntervalTicks, ServerRules.MIN_SYNC_INTERVAL_TICKS, ServerRules.MAX_SYNC_INTERVAL_TICKS)
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

	private static String formatLocation(PlayerLocation location) {
		List<String> parts = new ArrayList<>();
		parts.add(location.name());
		if (location.hasCoordinates()) {
			parts.add(String.format("x: %.1f y: %.1f z: %.1f", location.x(), location.y(), location.z()));
		}
		if (location.hasDimension()) {
			parts.add(prettyDimension(location.dimension()));
		}
		if (location.hasDistance()) {
			parts.add(String.format("%.0fm", location.distance()));
		}
		return String.join("  |  ", parts);
	}

	private static String prettyDimension(String dimension) {
		int index = dimension.indexOf(':');
		String path = index >= 0 ? dimension.substring(index + 1) : dimension;
		return switch (path) {
			case "overworld" -> "Overworld";
			case "the_nether" -> "Nether";
			case "the_end" -> "The End";
			default -> path;
		};
	}
}
