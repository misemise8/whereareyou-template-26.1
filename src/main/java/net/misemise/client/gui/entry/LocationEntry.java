package net.misemise.client.gui.entry;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.misemise.client.render.RenderHelpers;
import net.misemise.network.PlayerLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class LocationEntry extends AbstractConfigListEntry<Void> {
	private final PlayerLocation location;

	public LocationEntry(PlayerLocation location) {
		super(Component.literal(location.name()), false);
		this.location = location;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		if (hovered) {
			graphics.fill(x, y, x + entryWidth, y + entryHeight, 0x22000000);
		}
		RenderHelpers.renderPlayerIcon(graphics, location.uuid(), x + 6, y + 5, 16);
		graphics.text(Minecraft.getInstance().font, location.name(), x + 28, y + 9, 0xFFFFFFFF, true);
		graphics.text(Minecraft.getInstance().font, formatLocation(), x + Math.max(160, entryWidth / 3), y + 9, 0xFFE0E0E0, true);
	}

	@Override
	public int getItemHeight() {
		return 28;
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
		List<String> parts = new java.util.ArrayList<>();
		if (location.hasCoordinates()) {
			parts.add(String.format("x %.1f  y %.1f  z %.1f", location.x(), location.y(), location.z()));
		}
		if (location.hasDimension()) {
			parts.add(RenderHelpers.prettyDimension(location.dimension()));
		}
		if (location.hasDistance()) {
			parts.add(String.format("%.0fm", location.distance()));
		}
		return parts.isEmpty() ? "Hidden by server" : String.join("  |  ", parts);
	}
}
