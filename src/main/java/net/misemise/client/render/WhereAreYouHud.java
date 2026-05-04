package net.misemise.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.misemise.client.config.ClientSettings;
import net.misemise.client.config.WhereAreYouClientConfig;
import net.misemise.client.state.ClientLocationState;
import net.misemise.network.PlayerLocation;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WhereAreYouHud {
	private static final int OVERLAY_MARGIN = 14;
	private static final int OVERLAY_PADDING_X = 5;
	private static final int OVERLAY_ICON_SIZE = 8;
	private static final int OVERLAY_ICON_GAP = 4;
	private static final int OVERLAY_MARKER_HEIGHT = 16;
	private static final int OVERLAY_ONSCREEN_OFFSET = 4;
	private static final int OVERLAY_NAMEPLATE_HEIGHT = 9;
	private static final int OVERLAY_NAMEPLATE_GAP = 1;
	private static final int OVERLAY_STACK_GAP = 2;
	private static final int OVERLAY_ACCENT_COLOR = 0xFFFFD15A;
	private static final int OVERLAY_TEXT_COLOR = 0xFFFFFFFF;
	private static final float OVERLAY_NEAR_SCALE = 0.90F;
	private static final float OVERLAY_FAR_SCALE = 0.58F;
	private static final double OVERLAY_NEAR_DISTANCE = 8.0D;
	private static final double OVERLAY_FAR_DISTANCE = 96.0D;
	private static final double OVERLAY_UNLOADED_HEIGHT = 2.35D;

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
			renderOverlay(graphics, client, settings, deltaTracker.getGameTimeDeltaPartialTick(false));
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

	private static void renderOverlay(GuiGraphicsExtractor graphics, Minecraft client, ClientSettings settings, float partialTick) {
		List<OverlayMarker> markers = overlayMarkers(graphics, client, settings, partialTick);
		if (markers.isEmpty()) {
			return;
		}

		resolveMarkerOverlap(markers, graphics.guiHeight());
		for (OverlayMarker marker : markers) {
			renderOverlayMarker(graphics, client, settings, marker);
		}
	}

	private static List<OverlayMarker> overlayMarkers(GuiGraphicsExtractor graphics, Minecraft client, ClientSettings settings, float partialTick) {
		List<OverlayMarker> markers = new ArrayList<>();
		Font font = client.font;
		int screenWidth = graphics.guiWidth();
		int screenHeight = graphics.guiHeight();
		int centerX = screenWidth / 2;
		int centerY = screenHeight / 2;
		boolean showIcon = overlayShowsIcon(settings.overlayContentMode);
		for (PlayerLocation location : overlayLocations(client, settings)) {
			String label = fitOverlayLabel(font, overlayLabel(location, settings.overlayContentMode), screenWidth, showIcon);
			int markerWidth = overlayMarkerWidth(font, label, showIcon);
			float scale = overlayScale(location, settings);
			Entity entity = client.level.getEntity(location.uuid());
			Vec3 target = overlayTarget(entity, location, partialTick);
			Vec3 projected = client.gameRenderer.projectPointToScreen(target);
			boolean onScreen = isOnScreen(projected);
			int x;
			int y;
			double sx = 0.0D;
			double sy = -1.0D;
			if (onScreen) {
				x = screenX(projected, screenWidth);
				y = overlayY(screenY(projected, screenHeight), scale, entity != null);
			} else {
				double dx = target.x - client.player.getX();
				double dz = target.z - client.player.getZ();
				if (Math.abs(dx) < 0.01D && Math.abs(dz) < 0.01D) {
					continue;
				}
				double targetAngle = Math.atan2(dx, dz);
				double relative = targetAngle + Math.toRadians(client.player.getYRot());
				sx = Math.sin(relative);
				sy = -Math.cos(relative);
				double halfWidth = Math.max(1.0D, screenWidth / 2.0D - OVERLAY_MARGIN);
				double halfHeight = Math.max(1.0D, screenHeight / 2.0D - OVERLAY_MARGIN);
				double edgeScale = Math.min(halfWidth / Math.max(0.2D, Math.abs(sx)),
						halfHeight / Math.max(0.2D, Math.abs(sy)));
				x = centerX + (int) Math.round(sx * edgeScale);
				y = centerY + (int) Math.round(sy * edgeScale);
			}
			OverlayMarker marker = new OverlayMarker(location, label, x, y, markerWidth, OVERLAY_MARKER_HEIGHT, scale, !onScreen, showIcon, sx, sy);
			marker.x = clamp(marker.x, marker.halfWidth() + 2, screenWidth - marker.halfWidth() - 2);
			marker.y = clamp(marker.y, marker.halfHeight() + 2, screenHeight - marker.halfHeight() - 2);
			markers.add(marker);
		}
		return markers;
	}

	private static List<PlayerLocation> overlayLocations(Minecraft client, ClientSettings settings) {
		String localDimension = client.level.dimension().identifier().toString();
		List<PlayerLocation> locations = new ArrayList<>();
		for (PlayerLocation location : ClientLocationState.sortedLocations(comparator(settings))) {
			boolean local = client.player.getUUID().equals(location.uuid());
			if (local) {
				continue;
			}
			ClientSettings.PlayerDisplay display = settings.displayFor(location.uuid(), false);
			if (!display.overlay || !location.hasCoordinates() || !location.hasDimension()
					|| !localDimension.equals(location.dimension())) {
				continue;
			}
			locations.add(location);
			if (locations.size() >= settings.maxPlayers) {
				break;
			}
		}
		return locations;
	}

	private static void renderOverlayMarker(GuiGraphicsExtractor graphics, Minecraft client, ClientSettings settings, OverlayMarker marker) {
		Font font = client.font;
		int left = -marker.width / 2;
		int top = -marker.height / 2;
		int right = left + marker.width;
		int bottom = top + marker.height;
		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(marker.x, marker.y);
		pose.scale(marker.scale);
		graphics.fill(left, top, right, bottom, overlayBackgroundColor(settings));
		if (marker.edge) {
			renderOverlayAccent(graphics, left, top, right, bottom, marker.sx, marker.sy);
		} else {
			graphics.fill(left, top, right, top + 1, OVERLAY_ACCENT_COLOR);
		}
		int textX = left + OVERLAY_PADDING_X;
		if (marker.showIcon) {
			int iconX = left + OVERLAY_PADDING_X;
			int iconY = top + (marker.height - OVERLAY_ICON_SIZE) / 2;
			RenderHelpers.renderPlayerIcon(graphics, marker.location.uuid(), iconX, iconY, OVERLAY_ICON_SIZE);
			textX = iconX + OVERLAY_ICON_SIZE + OVERLAY_ICON_GAP;
		}
		int textY = top + (marker.height - font.lineHeight) / 2;
		graphics.text(font, marker.label, textX, textY, OVERLAY_TEXT_COLOR, true);
		pose.popMatrix();
	}

	private static void renderOverlayAccent(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, double sx, double sy) {
		if (Math.abs(sx) > Math.abs(sy)) {
			if (sx >= 0.0D) {
				graphics.fill(right - 2, top, right, bottom, OVERLAY_ACCENT_COLOR);
			} else {
				graphics.fill(left, top, left + 2, bottom, OVERLAY_ACCENT_COLOR);
			}
			return;
		}
		if (sy >= 0.0D) {
			graphics.fill(left, bottom - 2, right, bottom, OVERLAY_ACCENT_COLOR);
		} else {
			graphics.fill(left, top, right, top + 2, OVERLAY_ACCENT_COLOR);
		}
	}

	private static int overlayMarkerWidth(Font font, String label, boolean showIcon) {
		int iconWidth = showIcon ? OVERLAY_ICON_SIZE + OVERLAY_ICON_GAP : 0;
		return OVERLAY_PADDING_X * 2 + iconWidth + font.width(label);
	}

	private static String fitOverlayLabel(Font font, String label, int screenWidth, boolean showIcon) {
		int iconWidth = showIcon ? OVERLAY_ICON_SIZE + OVERLAY_ICON_GAP : 0;
		int maxTextWidth = Math.max(24, screenWidth - OVERLAY_MARGIN * 2 - OVERLAY_PADDING_X * 2 - iconWidth);
		if (font.width(label) <= maxTextWidth) {
			return label;
		}
		String suffix = "...";
		int suffixWidth = font.width(suffix);
		int end = label.length();
		while (end > 0 && font.width(label.substring(0, end)) + suffixWidth > maxTextWidth) {
			end--;
		}
		return end == 0 ? suffix : label.substring(0, end) + suffix;
	}

	private static void resolveMarkerOverlap(List<OverlayMarker> markers, int screenHeight) {
		markers.sort(Comparator
				.comparing((OverlayMarker marker) -> marker.location.name(), String.CASE_INSENSITIVE_ORDER)
				.thenComparing(marker -> marker.location.uuid().toString()));
		List<OverlayMarker> placed = new ArrayList<>();
		for (OverlayMarker marker : markers) {
			int y = marker.y;
			for (int attempts = 0; attempts < markers.size() + 4; attempts++) {
				OverlayMarker overlap = firstOverlap(marker, y, placed);
				if (overlap == null) {
					break;
				}
				int up = overlap.top() - marker.halfHeight() - OVERLAY_STACK_GAP;
				y = up >= marker.halfHeight() + 2
						? up
						: overlap.bottom() + marker.halfHeight() + OVERLAY_STACK_GAP;
			}
			marker.y = clamp(y, marker.halfHeight() + 2, screenHeight - marker.halfHeight() - 2);
			placed.add(marker);
		}
	}

	private static OverlayMarker firstOverlap(OverlayMarker marker, int y, List<OverlayMarker> placed) {
		for (OverlayMarker other : placed) {
			if (marker.overlapsAt(y, other)) {
				return other;
			}
		}
		return null;
	}

	private static Vec3 overlayTarget(Entity entity, PlayerLocation location, float partialTick) {
		if (entity != null) {
			Vec3 attachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot(partialTick));
			if (attachment != null) {
				return entity.getPosition(partialTick).add(attachment);
			}
			return entity.getPosition(partialTick).add(0.0D, entity.getBbHeight(), 0.0D);
		}
		return new Vec3(location.x(), location.y() + OVERLAY_UNLOADED_HEIGHT, location.z());
	}

	private static int overlayY(int projectedY, float scale, boolean nameplateAnchored) {
		int markerHalfHeight = Math.round(OVERLAY_MARKER_HEIGHT * scale / 2.0F);
		if (nameplateAnchored) {
			return projectedY - markerHalfHeight - OVERLAY_NAMEPLATE_HEIGHT / 2 - OVERLAY_NAMEPLATE_GAP;
		}
		return projectedY - markerHalfHeight - OVERLAY_ONSCREEN_OFFSET;
	}

	private static float overlayScale(PlayerLocation location, ClientSettings settings) {
		if (!location.hasDistance()) {
			return 0.72F * settings.overlayScale / 100.0F;
		}
		double t = (location.distance() - OVERLAY_NEAR_DISTANCE) / (OVERLAY_FAR_DISTANCE - OVERLAY_NEAR_DISTANCE);
		t = Math.max(0.0D, Math.min(1.0D, t));
		return (float) (OVERLAY_NEAR_SCALE + (OVERLAY_FAR_SCALE - OVERLAY_NEAR_SCALE) * t) * settings.overlayScale / 100.0F;
	}

	private static boolean isOnScreen(Vec3 projected) {
		return Double.isFinite(projected.x) && Double.isFinite(projected.y) && Double.isFinite(projected.z)
				&& projected.z <= 1.0D
				&& projected.x >= -1.0D && projected.x <= 1.0D
				&& projected.y >= -1.0D && projected.y <= 1.0D;
	}

	private static int screenX(Vec3 projected, int screenWidth) {
		return (int) Math.round((projected.x + 1.0D) * 0.5D * screenWidth);
	}

	private static int screenY(Vec3 projected, int screenHeight) {
		return (int) Math.round((1.0D - projected.y) * 0.5D * screenHeight);
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

	private static String overlayLabel(PlayerLocation location, ClientSettings.OverlayContentMode mode) {
		if (mode == ClientSettings.OverlayContentMode.DISTANCE) {
			return location.hasDistance() ? String.format("%.0fm", location.distance()) : location.name();
		}
		if (location.hasDistance()) {
			return location.name() + " " + String.format("%.0fm", location.distance());
		}
		return location.name();
	}

	private static boolean overlayShowsIcon(ClientSettings.OverlayContentMode mode) {
		return mode == ClientSettings.OverlayContentMode.ICON_NAME_DISTANCE;
	}

	private static int overlayBackgroundColor(ClientSettings settings) {
		int alpha = clamp(Math.round(settings.overlayBackgroundOpacity * 255.0F / 100.0F), 0, 255);
		return alpha << 24;
	}

	private static int clamp(int value, int min, int max) {
		if (max < min) {
			return (min + max) / 2;
		}
		return Math.max(min, Math.min(max, value));
	}

	private static final class OverlayMarker {
		private final PlayerLocation location;
		private final String label;
		private int x;
		private int y;
		private final int width;
		private final int height;
		private final float scale;
		private final boolean edge;
		private final boolean showIcon;
		private final double sx;
		private final double sy;

		private OverlayMarker(PlayerLocation location, String label, int x, int y, int width, int height, float scale, boolean edge, boolean showIcon, double sx, double sy) {
			this.location = location;
			this.label = label;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.scale = scale;
			this.edge = edge;
			this.showIcon = showIcon;
			this.sx = sx;
			this.sy = sy;
		}

		private int displayWidth() {
			return Math.max(1, Math.round(width * scale));
		}

		private int displayHeight() {
			return Math.max(1, Math.round(height * scale));
		}

		private int halfWidth() {
			return displayWidth() / 2 + 1;
		}

		private int halfHeight() {
			return displayHeight() / 2 + 1;
		}

		private int left() {
			return x - halfWidth();
		}

		private int right() {
			return x + halfWidth();
		}

		private int top() {
			return y - halfHeight();
		}

		private int bottom() {
			return y + halfHeight();
		}

		private boolean overlapsAt(int candidateY, OverlayMarker other) {
			int top = candidateY - halfHeight();
			int bottom = candidateY + halfHeight();
			return left() < other.right()
					&& right() > other.left()
					&& top < other.bottom()
					&& bottom > other.top();
		}
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
