package net.misemise.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.misemise.client.config.ClientSettings;
import net.misemise.client.config.WhereAreYouClientConfig;
import net.misemise.client.state.ClientLocationState;
import net.misemise.network.PlayerLocation;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WhereAreYouHud {
	private WhereAreYouHud() {
	}

	public static void render(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null || client.options.hideGui || !ClientLocationState.displaysVisible()) {
			return;
		}
		ClientSettings settings = WhereAreYouClientConfig.active();
		if (settings.hudEnabled) {
			renderHud(graphics, client, settings);
		}
		if (settings.overlayEnabled) {
			renderOverlay(graphics, client, settings);
		}
	}

	private static void renderHud(GuiGraphicsExtractor graphics, Minecraft client, ClientSettings settings) {
		List<PlayerLocation> locations = filteredLocations(client, settings, true);
		if (locations.isEmpty()) {
			return;
		}
		Font font = client.font;
		List<String> lines = new ArrayList<>();
		for (PlayerLocation location : locations) {
			lines.add(formatHudLine(location, settings));
		}

		int iconSize = settings.showIcon ? 8 : 0;
		int iconGap = settings.showIcon ? 4 : 0;
		int width = 0;
		for (String line : lines) {
			width = Math.max(width, iconSize + iconGap + font.width(line));
		}
		int lineHeight = Math.max(font.lineHeight, iconSize) + 2;
		int height = lines.size() * lineHeight + 2;
		int[] xy = resolveHudPosition(settings, graphics.guiWidth(), graphics.guiHeight(), width + 6, height + 4);
		float scale = settings.hudScale / 100.0F;
		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.scale(scale);
		int x = Math.round(xy[0] / scale);
		int y = Math.round(xy[1] / scale);
		graphics.fill(x - 2, y - 2, x + width + 6, y + height, 0x66000000);
		for (int index = 0; index < lines.size(); index++) {
			PlayerLocation location = locations.get(index);
			int rowY = y + 2 + index * lineHeight;
			int textX = x;
			if (settings.showIcon) {
				RenderHelpers.renderPlayerIcon(graphics, location.uuid(), x, rowY + 1, iconSize);
				textX += iconSize + iconGap;
			}
			graphics.text(font, lines.get(index), textX, rowY, 0xFFFFFFFF, true);
		}
		pose.popMatrix();
	}

	private static void renderOverlay(GuiGraphicsExtractor graphics, Minecraft client, ClientSettings settings) {
		String localDimension = client.level.dimension().identifier().toString();
		List<PlayerLocation> locations = filteredLocations(client, settings, false).stream()
				.filter(PlayerLocation::hasCoordinates)
				.filter(PlayerLocation::hasDimension)
				.filter(location -> localDimension.equals(location.dimension()))
				.filter(location -> client.level.getEntity(location.uuid()) == null)
				.limit(settings.maxPlayers)
				.toList();
		if (locations.isEmpty()) {
			return;
		}

		int centerX = graphics.guiWidth() / 2;
		int centerY = graphics.guiHeight() / 2;
		int margin = 14;
		for (PlayerLocation location : locations) {
			double dx = location.x() - client.player.getX();
			double dz = location.z() - client.player.getZ();
			if (Math.abs(dx) < 0.01D && Math.abs(dz) < 0.01D) {
				continue;
			}
			double targetAngle = Math.atan2(dx, dz);
			double relative = targetAngle + Math.toRadians(client.player.getYRot());
			double sx = Math.sin(relative);
			double sy = -Math.cos(relative);
			double scale = Math.min((graphics.guiWidth() / 2.0D - margin) / Math.max(0.2D, Math.abs(sx)),
					(graphics.guiHeight() / 2.0D - margin) / Math.max(0.2D, Math.abs(sy)));
			int x = centerX + (int) Math.round(sx * scale);
			int y = centerY + (int) Math.round(sy * scale);
			String label = overlayLabel(location);
			int textWidth = client.font.width(label);
			graphics.fill(x - textWidth / 2 - 4, y - 6, x + textWidth / 2 + 4, y + 6, 0x66000000);
			graphics.centeredText(client.font, label, x, y - 4, 0xFFFFFFFF);
		}
	}

	private static List<PlayerLocation> filteredLocations(Minecraft client, ClientSettings settings, boolean hud) {
		List<PlayerLocation> locations = ClientLocationState.sortedLocations(comparator(settings));
		locations.removeIf(location -> {
			boolean local = client.player != null && client.player.getUUID().equals(location.uuid());
			ClientSettings.PlayerDisplay display = settings.displayFor(location.uuid(), local);
			return hud ? !display.hud : !display.overlay;
		});
		if (locations.size() > settings.maxPlayers) {
			return new ArrayList<>(locations.subList(0, settings.maxPlayers));
		}
		return locations;
	}

	private static Comparator<PlayerLocation> comparator(ClientSettings settings) {
		Comparator<PlayerLocation> byName = Comparator.comparing(PlayerLocation::name, String.CASE_INSENSITIVE_ORDER);
		if (settings.sortMode == ClientSettings.SortMode.DISTANCE) {
			return Comparator
					.comparingDouble((PlayerLocation location) -> location.hasDistance() ? location.distance() : Double.MAX_VALUE)
					.thenComparing(byName);
		}
		return byName;
	}

	private static String formatHudLine(PlayerLocation location, ClientSettings settings) {
		List<String> parts = new ArrayList<>();
		if (settings.showName) {
			parts.add(location.name());
		}
		if (settings.showDistance && location.hasDistance()) {
			parts.add(String.format("%.0fm", location.distance()));
		}
		if (settings.showCoordinates && location.hasCoordinates()) {
			parts.add(String.format("%.0f %.0f %.0f", location.x(), location.y(), location.z()));
		}
		if (settings.showDimension && location.hasDimension()) {
			parts.add(RenderHelpers.prettyDimension(location.dimension()));
		}
		if (parts.isEmpty()) {
			return location.name();
		}
		return String.join("  ", parts);
	}

	private static String overlayLabel(PlayerLocation location) {
		if (location.hasDistance()) {
			return location.name() + " " + String.format("%.0fm", location.distance());
		}
		return location.name();
	}

	private static int[] resolveHudPosition(ClientSettings settings, int screenWidth, int screenHeight, int width, int height) {
		int x = switch (settings.hudPosition) {
			case TOP_LEFT, BOTTOM_LEFT -> settings.hudXOffset;
			case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - width - settings.hudXOffset;
		};
		int y = switch (settings.hudPosition) {
			case TOP_LEFT, TOP_RIGHT -> settings.hudYOffset;
			case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - height - settings.hudYOffset;
		};
		return new int[]{Math.max(0, x), Math.max(0, y)};
	}

}
