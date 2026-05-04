package net.misemise.client.gui.entry;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class LocationTableHeaderEntry extends AbstractConfigListEntry<Void> {
	private final Component playerLabel;
	private final Component locationLabel;

	public LocationTableHeaderEntry() {
		super(Component.translatable("config.whereareyou.locations.header"), false);
		this.playerLabel = Component.translatable("config.whereareyou.locations.column.player");
		this.locationLabel = Component.translatable("config.whereareyou.locations.column.location");
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, delta);
		int bottom = y + entryHeight;
		int locationLeft = LocationTableLayout.locationColumnLeft(x, entryWidth);
		int right = LocationTableLayout.columnRight(x, entryWidth);

		graphics.fill(x, y, right, bottom, 0x30000000);
		drawGrid(graphics, x, y, bottom, locationLeft, right);

		Font font = Minecraft.getInstance().font;
		graphics.text(font, playerLabel, LocationTableLayout.playerColumnLeft(x), y + 8, 0xFFE8E8E8, true);
		graphics.text(font, locationLabel, LocationTableLayout.locationTextX(x, entryWidth), y + 8, 0xFFE8E8E8, true);
	}

	@Override
	public int getItemHeight() {
		return 26;
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

	static void drawGrid(GuiGraphicsExtractor graphics, int left, int top, int bottom, int locationLeft, int right) {
		int color = 0x55FFFFFF;
		graphics.fill(left, top, right, top + 1, color);
		graphics.fill(left, bottom - 1, right, bottom, color);
		graphics.fill(left, top, left + 1, bottom, color);
		graphics.fill(locationLeft, top, locationLeft + 1, bottom, color);
		graphics.fill(right - 1, top, right, bottom, color);
	}
}
