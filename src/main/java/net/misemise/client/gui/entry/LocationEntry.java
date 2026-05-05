package net.misemise.client.gui.entry;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.misemise.client.render.RenderHelpers;
import net.misemise.network.PlayerLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class LocationEntry extends AbstractConfigListEntry<Void> {
	private static final int ROW_FILL_COLOR = 0x12000000;
	private static final int ROW_HOVER_FILL_COLOR = 0x26000000;
	private static final int PLAYER_TEXT_COLOR = 0xFFFFFFFF;
	private static final int DETAIL_TEXT_COLOR = 0xFFD6D6D6;
	private static final int DISTANCE_TEXT_COLOR = 0xFFFFD36F;
	private static final int STATUS_TEXT_COLOR = 0xFFB8B8B8;
	private static final int DETAIL_GAP = 10;

	private final DisplayPlayer player;
	private final PlayerLocation location;

	public LocationEntry(DisplayPlayer player, PlayerLocation location) {
		super(Component.literal(player.name()), false);
		this.player = player;
		this.location = location;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, delta);
		int bottom = y + entryHeight;
		int locationLeft = LocationTableLayout.locationColumnLeft(x, entryWidth);
		int right = LocationTableLayout.columnRight(x, entryWidth);
		graphics.fill(x, y, right, bottom, hovered ? ROW_HOVER_FILL_COLOR : ROW_FILL_COLOR);
		LocationTableHeaderEntry.drawGrid(graphics, x, y, bottom, locationLeft, right);

		Font font = Minecraft.getInstance().font;
		RenderHelpers.renderPlayerIcon(graphics, player.uuid(), LocationTableLayout.playerColumnLeft(x), y + 5, LocationTableLayout.ICON_SIZE);
		int maxNameWidth = locationLeft - LocationTableLayout.playerTextX(x) - LocationTableLayout.CELL_PADDING;
		String playerLabel = RenderHelpers.ellipsize(font, player.name(), maxNameWidth);
		graphics.text(font, playerLabel, LocationTableLayout.playerTextX(x), y + 9, PLAYER_TEXT_COLOR, true);
		renderLocationDetails(graphics, font, LocationTableLayout.locationTextX(x, entryWidth), right - LocationTableLayout.CELL_PADDING, y + 9);
	}

	@Override
	public int getItemHeight() {
		return LocationTableLayout.ROW_HEIGHT;
	}

	@Override
	public Void getValue() {
		return null;
	}

	@Override
	public Optional<Void> getDefaultValue() {
		return Optional.empty();
	}

	@Override
	public boolean isEdited() {
		return false;
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of();
	}

	@Override
	public List<? extends NarratableEntry> narratables() {
		return List.of();
	}

	private void renderLocationDetails(GuiGraphicsExtractor graphics, Font font, int left, int right, int y) {
		LocationText text = locationText();
		if (!text.status.isEmpty()) {
			graphics.text(font, RenderHelpers.ellipsize(font, text.status, right - left), left, y, STATUS_TEXT_COLOR, true);
			return;
		}

		int textRight = right;
		if (!text.distance.isEmpty()) {
			int distanceX = right - font.width(text.distance);
			graphics.text(font, text.distance, distanceX, y, DISTANCE_TEXT_COLOR, true);
			textRight = distanceX - DETAIL_GAP;
		}

		String details = joinedDetails(text.coordinates, text.dimension);
		if (!details.isEmpty() && textRight > left) {
			graphics.text(font, RenderHelpers.ellipsize(font, details, textRight - left), left, y, DETAIL_TEXT_COLOR, true);
		}
	}

	private LocationText locationText() {
		if (location == null) {
			return new LocationText("", "", "", I18n.get("config.whereareyou.locations.no_location"));
		}
		String coordinates = "";
		String dimension = "";
		String distance = "";
		if (location.hasCoordinates()) {
			coordinates = I18n.get("config.whereareyou.locations.coordinates", blockCoord(location.x()), blockCoord(location.y()), blockCoord(location.z()));
		}
		if (location.hasDimension()) {
			dimension = RenderHelpers.prettyDimension(location.dimension());
		}
		if (location.hasDistance()) {
			distance = I18n.get("config.whereareyou.locations.distance", Math.round(location.distance()));
		}
		String status = coordinates.isEmpty() && dimension.isEmpty() && distance.isEmpty()
				? I18n.get("config.whereareyou.locations.hidden")
				: "";
		return new LocationText(coordinates, dimension, distance, status);
	}

	private static String joinedDetails(String coordinates, String dimension) {
		List<String> parts = new java.util.ArrayList<>();
		if (!coordinates.isEmpty()) {
			parts.add(coordinates);
		}
		if (!dimension.isEmpty()) {
			parts.add(dimension);
		}
		return String.join("  |  ", parts);
	}

	private static int blockCoord(double value) {
		return (int) Math.floor(value);
	}

	private record LocationText(String coordinates, String dimension, String distance, String status) {
	}
}
