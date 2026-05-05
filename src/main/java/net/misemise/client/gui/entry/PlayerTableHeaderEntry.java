package net.misemise.client.gui.entry;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.misemise.client.render.RenderHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class PlayerTableHeaderEntry extends AbstractConfigListEntry<Void> {
	private static final int HEADER_FILL_COLOR = 0x26000000;
	private static final int GRID_COLOR = 0x30FFFFFF;
	private static final int HEADER_TEXT_COLOR = 0xFFE8E8E8;

	private final Component playerLabel;
	private final Component hudLabel;
	private final Component overlayLabel;

	public PlayerTableHeaderEntry() {
		super(Component.translatable("config.whereareyou.players.header"), false);
		this.playerLabel = Component.translatable("config.whereareyou.players.column.player");
		this.hudLabel = Component.translatable("config.whereareyou.players.column.hud");
		this.overlayLabel = Component.translatable("config.whereareyou.players.column.overlay");
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, delta);
		int bottom = y + entryHeight;
		int hudLeft = PlayerTableLayout.hudColumnLeft(x, entryWidth);
		int overlayLeft = PlayerTableLayout.overlayColumnLeft(x, entryWidth);
		int right = PlayerTableLayout.columnRight(x, entryWidth);

		graphics.fill(x, y, right, bottom, HEADER_FILL_COLOR);
		drawGrid(graphics, x, y, bottom, hudLeft, overlayLeft, right);

		Font font = Minecraft.getInstance().font;
		int maxPlayerWidth = hudLeft - PlayerTableLayout.playerColumnLeft(x) - PlayerTableLayout.CELL_PADDING;
		graphics.text(font, RenderHelpers.ellipsize(font, playerLabel.getString(), maxPlayerWidth), PlayerTableLayout.playerColumnLeft(x), y + 8, HEADER_TEXT_COLOR, true);
		drawCentered(graphics, font, hudLabel, hudLeft, overlayLeft, y + 8);
		drawCentered(graphics, font, overlayLabel, overlayLeft, right, y + 8);
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

	static void drawGrid(GuiGraphicsExtractor graphics, int left, int top, int bottom, int hudLeft, int overlayLeft, int right) {
		graphics.fill(left, top, right, top + 1, GRID_COLOR);
		graphics.fill(left, bottom - 1, right, bottom, GRID_COLOR);
		graphics.fill(left, top, left + 1, bottom, GRID_COLOR);
		graphics.fill(hudLeft, top, hudLeft + 1, bottom, GRID_COLOR);
		graphics.fill(overlayLeft, top, overlayLeft + 1, bottom, GRID_COLOR);
		graphics.fill(right - 1, top, right, bottom, GRID_COLOR);
	}

	private static void drawCentered(GuiGraphicsExtractor graphics, Font font, Component text, int left, int right, int y) {
		String label = RenderHelpers.ellipsize(font, text.getString(), right - left - PlayerTableLayout.CELL_PADDING * 2);
		int width = font.width(label);
		int x = left + Math.max(0, (right - left - width) / 2);
		graphics.text(font, label, x, y, HEADER_TEXT_COLOR, true);
	}
}
