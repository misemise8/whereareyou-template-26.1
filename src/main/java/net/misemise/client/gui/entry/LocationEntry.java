package net.misemise.client.gui.entry;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.misemise.client.render.RenderHelpers;
import net.misemise.network.PlayerLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class LocationEntry extends AbstractConfigListEntry<Void> {
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
		graphics.fill(x, y, right, bottom, hovered ? 0x30000000 : 0x18000000);
		LocationTableHeaderEntry.drawGrid(graphics, x, y, bottom, locationLeft, right);

		RenderHelpers.renderPlayerIcon(graphics, player.uuid(), LocationTableLayout.playerColumnLeft(x), y + 5, LocationTableLayout.ICON_SIZE);
		graphics.text(Minecraft.getInstance().font, player.name(), LocationTableLayout.playerTextX(x), y + 9, 0xFFFFFFFF, true);
		graphics.text(Minecraft.getInstance().font, formatLocation(), LocationTableLayout.locationTextX(x, entryWidth), y + 9, 0xFFE0E0E0, true);
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

	private String formatLocation() {
		if (location == null) {
			return I18n.get("config.whereareyou.locations.no_location");
		}
		List<String> parts = new java.util.ArrayList<>();
		if (location.hasCoordinates()) {
			parts.add(I18n.get("config.whereareyou.locations.coordinates", blockCoord(location.x()), blockCoord(location.y()), blockCoord(location.z())));
		}
		if (location.hasDimension()) {
			parts.add(RenderHelpers.prettyDimension(location.dimension()));
		}
		if (location.hasDistance()) {
			parts.add(I18n.get("config.whereareyou.locations.distance", Math.round(location.distance())));
		}
		return parts.isEmpty() ? I18n.get("config.whereareyou.locations.hidden") : String.join("  |  ", parts);
	}

	private static int blockCoord(double value) {
		return (int) Math.floor(value);
	}
}
