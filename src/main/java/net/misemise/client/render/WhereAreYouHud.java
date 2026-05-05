package net.misemise.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
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
	private static final int HUD_ICON_SIZE = 8;
	private static final int HUD_ICON_GAP = 4;
	private static final int HUD_COLUMN_GAP = 14;
	private static final int OVERLAY_MARGIN = 14;
	private static final int OVERLAY_PADDING_X = 5;
	private static final int OVERLAY_ICON_SIZE = 8;
	private static final int OVERLAY_ICON_GAP = 4;
	private static final int OVERLAY_TEXT_GAP = 4;
	private static final int OVERLAY_MARKER_HEIGHT = 16;
	private static final int OVERLAY_COMPACT_MARKER_HEIGHT = 14;
	private static final int OVERLAY_ONSCREEN_OFFSET = 4;
	private static final int OVERLAY_NAMEPLATE_HEIGHT = 9;
	private static final int OVERLAY_NAMEPLATE_GAP = 1;
	private static final int OVERLAY_STACK_GAP = 2;
	private static final int OVERLAY_ACCENT_COLOR = 0xFFFFD15A;
	private static final int OVERLAY_TEXT_COLOR = 0xFFFFFFFF;
	private static final int OVERLAY_DISTANCE_COLOR = 0xFFFFD15A;
	private static final float OVERLAY_CLOSE_SCALE = 0.74F;
	private static final float OVERLAY_MID_SCALE = 0.88F;
	private static final float OVERLAY_FAR_SCALE = 0.70F;
	private static final float OVERLAY_EDGE_SCALE = 0.82F;
	private static final double OVERLAY_CLOSE_DISTANCE = 12.0D;
	private static final double OVERLAY_MID_DISTANCE = 40.0D;
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
		List<HudRow> rows = new ArrayList<>();
		for (PlayerLocation location : locations) {
			rows.add(hudRow(location));
		}

		HudColumns columns = hudColumns(font, rows, settings);
		int iconSize = settings.showIcon ? HUD_ICON_SIZE : 0;
		int iconGap = settings.showIcon ? HUD_ICON_GAP : 0;
		int width = iconSize + iconGap + columns.width;
		int lineHeight = Math.max(font.lineHeight, iconSize) + 2;
		int height = rows.size() * lineHeight + 2;
		int[] xy = resolveHudPosition(settings, graphics.guiWidth(), graphics.guiHeight(), width + 6, height + 4);
		float scale = settings.hudScale / 100.0F;
		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.scale(scale);
		int x = Math.round(xy[0] / scale);
		int y = Math.round(xy[1] / scale);
		graphics.fill(x - 2, y - 2, x + width + 6, y + height, 0x66000000);
		for (int index = 0; index < rows.size(); index++) {
			HudRow row = rows.get(index);
			int rowY = y + 2 + index * lineHeight;
			int textX = x;
			if (settings.showIcon) {
				RenderHelpers.renderPlayerIcon(graphics, row.location.uuid(), x, rowY + 1, iconSize);
				textX += iconSize + iconGap;
			}
			renderHudRow(graphics, font, row, columns, textX, rowY);
		}
		pose.popMatrix();
	}

	private static void renderHudRow(GuiGraphicsExtractor graphics, Font font, HudRow row, HudColumns columns, int x, int y) {
		int cellX = x;
		boolean previous = false;
		if (columns.showName) {
			graphics.text(font, row.name, cellX, y, 0xFFFFFFFF, true);
			cellX += columns.nameWidth;
			previous = true;
		}
		if (columns.showDistance) {
			if (previous) {
				cellX += HUD_COLUMN_GAP;
			}
			renderRightAlignedHudText(graphics, font, row.distance, cellX, y, columns.distanceWidth);
			cellX += columns.distanceWidth;
			previous = true;
		}
		if (columns.showCoordinates) {
			if (previous) {
				cellX += HUD_COLUMN_GAP;
			}
			renderRightAlignedHudText(graphics, font, row.coordinates, cellX, y, columns.coordinatesWidth);
			cellX += columns.coordinatesWidth;
			previous = true;
		}
		if (columns.showDimension) {
			if (previous) {
				cellX += HUD_COLUMN_GAP;
			}
			graphics.text(font, row.dimension, cellX, y, 0xFFFFFFFF, true);
		}
	}

	private static void renderRightAlignedHudText(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int width) {
		if (text.isEmpty()) {
			return;
		}
		graphics.text(font, text, x + width - font.width(text), y, 0xFFFFFFFF, true);
	}

	private static HudColumns hudColumns(Font font, List<HudRow> rows, ClientSettings settings) {
		boolean showName = settings.showName;
		boolean showDistance = settings.showDistance && hasHudColumn(rows, HudColumn.DISTANCE);
		boolean showCoordinates = settings.showCoordinates && hasHudColumn(rows, HudColumn.COORDINATES);
		boolean showDimension = settings.showDimension && hasHudColumn(rows, HudColumn.DIMENSION);
		if (!showName && !showDistance && !showCoordinates && !showDimension) {
			showName = true;
		}
		int nameWidth = 0;
		int distanceWidth = 0;
		int coordinatesWidth = 0;
		int dimensionWidth = 0;
		for (HudRow row : rows) {
			if (showName) {
				nameWidth = Math.max(nameWidth, font.width(row.name));
			}
			if (showDistance) {
				distanceWidth = Math.max(distanceWidth, font.width(row.distance));
			}
			if (showCoordinates) {
				coordinatesWidth = Math.max(coordinatesWidth, font.width(row.coordinates));
			}
			if (showDimension) {
				dimensionWidth = Math.max(dimensionWidth, font.width(row.dimension));
			}
		}
		int width = 0;
		int columns = 0;
		if (showName) {
			width += nameWidth;
			columns++;
		}
		if (showDistance) {
			width += distanceWidth;
			columns++;
		}
		if (showCoordinates) {
			width += coordinatesWidth;
			columns++;
		}
		if (showDimension) {
			width += dimensionWidth;
			columns++;
		}
		if (columns > 1) {
			width += (columns - 1) * HUD_COLUMN_GAP;
		}
		return new HudColumns(showName, showDistance, showCoordinates, showDimension,
				nameWidth, distanceWidth, coordinatesWidth, dimensionWidth, width);
	}

	private static boolean hasHudColumn(List<HudRow> rows, HudColumn column) {
		for (HudRow row : rows) {
			if (switch (column) {
				case DISTANCE -> !row.distance.isEmpty();
				case COORDINATES -> !row.coordinates.isEmpty();
				case DIMENSION -> !row.dimension.isEmpty();
			}) {
				return true;
			}
		}
		return false;
	}

	private static HudRow hudRow(PlayerLocation location) {
		return new HudRow(location, location.name(),
				location.hasDistance() ? String.format("%.0fm", location.distance()) : "",
				location.hasCoordinates() ? String.format("%.0f %.0f %.0f", location.x(), location.y(), location.z()) : "",
				location.hasDimension() ? RenderHelpers.prettyDimension(location.dimension()) : "");
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
		Camera camera = client.gameRenderer.getMainCamera();
		for (PlayerLocation location : overlayLocations(client, settings)) {
			Entity entity = client.level.getEntity(location.uuid());
			Vec3 target = overlayTarget(entity, location, partialTick);
			Vec3 projected = client.gameRenderer.projectPointToScreen(target);
			boolean onScreen = entity != null && isStableOnScreen(camera, target, projected);
			boolean edge = !onScreen;
			OverlayContent content = fitOverlayContent(font,
					overlayContent(location, settings.overlayContentMode, overlayBand(location), edge), screenWidth);
			int markerWidth = overlayMarkerWidth(font, content);
			int markerHeight = content.compact ? OVERLAY_COMPACT_MARKER_HEIGHT : OVERLAY_MARKER_HEIGHT;
			float scale = overlayScale(location, settings, edge);
			int x;
			int y;
			double sx = 0.0D;
			double sy = -1.0D;
			if (onScreen) {
				x = screenX(projected, screenWidth);
				y = overlayY(screenY(projected, screenHeight), markerHeight, scale, entity != null);
			} else {
				Vec3 cameraPosition = camera.position();
				double dx = target.x - cameraPosition.x;
				double dz = target.z - cameraPosition.z;
				if (Math.abs(dx) < 0.01D && Math.abs(dz) < 0.01D) {
					continue;
				}
				double targetAngle = Math.atan2(dx, dz);
				double relative = targetAngle + Math.toRadians(camera.yRot());
				sx = -Math.sin(relative);
				sy = Math.cos(relative);
				double halfWidth = Math.max(1.0D, screenWidth / 2.0D - OVERLAY_MARGIN);
				double halfHeight = Math.max(1.0D, screenHeight / 2.0D - OVERLAY_MARGIN);
				double edgeScale = Math.min(halfWidth / Math.max(0.2D, Math.abs(sx)),
						halfHeight / Math.max(0.2D, Math.abs(sy)));
				x = centerX + (int) Math.round(sx * edgeScale);
				y = centerY + (int) Math.round(sy * edgeScale);
			}
			OverlayMarker marker = new OverlayMarker(location, content, x, y, markerWidth, markerHeight, scale, edge, sx, sy);
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
		if (marker.content.showIcon) {
			int iconX = left + OVERLAY_PADDING_X;
			int iconY = top + (marker.height - OVERLAY_ICON_SIZE) / 2;
			RenderHelpers.renderPlayerIcon(graphics, marker.location.uuid(), iconX, iconY, OVERLAY_ICON_SIZE);
			textX = iconX + OVERLAY_ICON_SIZE + OVERLAY_ICON_GAP;
		}
		int textY = top + (marker.height - font.lineHeight) / 2;
		if (!marker.content.name.isEmpty()) {
			graphics.text(font, marker.content.name, textX, textY, OVERLAY_TEXT_COLOR, true);
			textX += font.width(marker.content.name);
		}
		if (!marker.content.name.isEmpty() && !marker.content.distance.isEmpty()) {
			textX += OVERLAY_TEXT_GAP;
		}
		if (!marker.content.distance.isEmpty()) {
			graphics.text(font, marker.content.distance, textX, textY, OVERLAY_DISTANCE_COLOR, true);
		}
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

	private static int overlayMarkerWidth(Font font, OverlayContent content) {
		int width = OVERLAY_PADDING_X * 2;
		boolean hasText = !content.name.isEmpty() || !content.distance.isEmpty();
		if (content.showIcon) {
			width += OVERLAY_ICON_SIZE;
			if (hasText) {
				width += OVERLAY_ICON_GAP;
			}
		}
		if (!content.name.isEmpty()) {
			width += font.width(content.name);
		}
		if (!content.name.isEmpty() && !content.distance.isEmpty()) {
			width += OVERLAY_TEXT_GAP;
		}
		if (!content.distance.isEmpty()) {
			width += font.width(content.distance);
		}
		return width;
	}

	private static OverlayContent fitOverlayContent(Font font, OverlayContent content, int screenWidth) {
		if (content.name.isEmpty()) {
			return content;
		}
		int fixedWidth = OVERLAY_PADDING_X * 2;
		if (content.showIcon) {
			fixedWidth += OVERLAY_ICON_SIZE + OVERLAY_ICON_GAP;
		}
		if (!content.distance.isEmpty()) {
			fixedWidth += OVERLAY_TEXT_GAP + font.width(content.distance);
		}
		int maxNameWidth = Math.max(24, screenWidth - OVERLAY_MARGIN * 2 - fixedWidth);
		if (font.width(content.name) <= maxNameWidth) {
			return content;
		}
		String suffix = "...";
		int suffixWidth = font.width(suffix);
		int end = content.name.length();
		while (end > 0 && font.width(content.name.substring(0, end)) + suffixWidth > maxNameWidth) {
			end--;
		}
		String name = end == 0 ? suffix : content.name.substring(0, end) + suffix;
		return new OverlayContent(name, content.distance, content.showIcon, content.compact);
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

	private static int overlayY(int projectedY, int markerHeight, float scale, boolean nameplateAnchored) {
		int markerHalfHeight = Math.round(markerHeight * scale / 2.0F);
		if (nameplateAnchored) {
			return projectedY - markerHalfHeight - OVERLAY_NAMEPLATE_HEIGHT / 2 - OVERLAY_NAMEPLATE_GAP;
		}
		return projectedY - markerHalfHeight - OVERLAY_ONSCREEN_OFFSET;
	}

	private static float overlayScale(PlayerLocation location, ClientSettings settings, boolean edge) {
		if (edge) {
			return OVERLAY_EDGE_SCALE * settings.overlayScale / 100.0F;
		}
		if (!location.hasDistance()) {
			return OVERLAY_MID_SCALE * settings.overlayScale / 100.0F;
		}
		double distance = location.distance();
		if (distance <= OVERLAY_CLOSE_DISTANCE) {
			return OVERLAY_CLOSE_SCALE * settings.overlayScale / 100.0F;
		}
		double scale;
		if (distance <= OVERLAY_MID_DISTANCE) {
			double t = (distance - OVERLAY_CLOSE_DISTANCE) / (OVERLAY_MID_DISTANCE - OVERLAY_CLOSE_DISTANCE);
			scale = OVERLAY_CLOSE_SCALE + (OVERLAY_MID_SCALE - OVERLAY_CLOSE_SCALE) * t;
		} else {
			double t = (distance - OVERLAY_MID_DISTANCE) / (OVERLAY_FAR_DISTANCE - OVERLAY_MID_DISTANCE);
			t = Math.max(0.0D, Math.min(1.0D, t));
			scale = OVERLAY_MID_SCALE + (OVERLAY_FAR_SCALE - OVERLAY_MID_SCALE) * t;
		}
		return (float) scale * settings.overlayScale / 100.0F;
	}

	private static boolean isOnScreen(Vec3 projected) {
		return Double.isFinite(projected.x) && Double.isFinite(projected.y) && Double.isFinite(projected.z)
				&& projected.z >= -1.0D
				&& projected.z <= 1.0D
				&& projected.x >= -1.0D && projected.x <= 1.0D
				&& projected.y >= -1.0D && projected.y <= 1.0D;
	}

	private static boolean isStableOnScreen(Camera camera, Vec3 target, Vec3 projected) {
		if (!isOnScreen(projected)) {
			return false;
		}
		Vec3 toTarget = target.subtract(camera.position());
		double distanceSqr = toTarget.lengthSqr();
		if (distanceSqr < 0.0001D) {
			return true;
		}
		Vec3 cameraLook = Vec3.directionFromRotation(camera.xRot(), camera.yRot());
		return toTarget.dot(cameraLook) > 0.05D;
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

	private static OverlayBand overlayBand(PlayerLocation location) {
		if (!location.hasDistance()) {
			return OverlayBand.MID;
		}
		if (location.distance() <= OVERLAY_CLOSE_DISTANCE) {
			return OverlayBand.CLOSE;
		}
		if (location.distance() <= OVERLAY_MID_DISTANCE) {
			return OverlayBand.MID;
		}
		return OverlayBand.FAR;
	}

	private static OverlayContent overlayContent(PlayerLocation location, ClientSettings.OverlayContentMode mode, OverlayBand band, boolean edge) {
		boolean showIcon = overlayShowsIcon(mode);
		if (!location.hasDistance()) {
			return new OverlayContent(location.name(), "", showIcon, false);
		}
		String distance = String.format("%.0fm", location.distance());
		if (edge || band == OverlayBand.CLOSE) {
			return new OverlayContent("", distance, showIcon, true);
		}
		if (mode == ClientSettings.OverlayContentMode.DISTANCE) {
			return new OverlayContent("", distance, false, true);
		}
		return new OverlayContent(location.name(), distance, showIcon, false);
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

	private enum HudColumn {
		DISTANCE,
		COORDINATES,
		DIMENSION
	}

	private record HudRow(PlayerLocation location, String name, String distance, String coordinates, String dimension) {
	}

	private record HudColumns(boolean showName, boolean showDistance, boolean showCoordinates, boolean showDimension,
							  int nameWidth, int distanceWidth, int coordinatesWidth, int dimensionWidth, int width) {
	}

	private enum OverlayBand {
		CLOSE,
		MID,
		FAR
	}

	private record OverlayContent(String name, String distance, boolean showIcon, boolean compact) {
	}

	private static final class OverlayMarker {
		private final PlayerLocation location;
		private final OverlayContent content;
		private int x;
		private int y;
		private final int width;
		private final int height;
		private final float scale;
		private final boolean edge;
		private final double sx;
		private final double sy;

		private OverlayMarker(PlayerLocation location, OverlayContent content, int x, int y, int width, int height, float scale, boolean edge, double sx, double sy) {
			this.location = location;
			this.content = content;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.scale = scale;
			this.edge = edge;
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
