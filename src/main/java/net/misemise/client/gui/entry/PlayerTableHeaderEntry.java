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

public class PlayerTableHeaderEntry extends AbstractConfigListEntry<Void> {
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

		graphics.fill(x, y, right, bottom, 0x30000000);
		drawGrid(graphics, x, y, bottom, hudLeft, overlayLeft, right);

		Font font = Minecraft.getInstance().font;
		graphics.text(font, playerLabel, PlayerTableLayout.playerColumnLeft(x), y + 8, 0xFFE8E8E8, true);
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
		int color = 0x55FFFFFF;
		graphics.fill(left, top, right, top + 1, color);
		graphics.fill(left, bottom - 1, right, bottom, color);
		graphics.fill(left, top, left + 1, bottom, color);
		graphics.fill(hudLeft, top, hudLeft + 1, bottom, color);
		graphics.fill(overlayLeft, top, overlayLeft + 1, bottom, color);
		graphics.fill(right - 1, top, right, bottom, color);
	}

	private static void drawCentered(GuiGraphicsExtractor graphics, Font font, Component text, int left, int right, int y) {
		int width = font.width(text);
		int x = left + Math.max(0, (right - left - width) / 2);
		graphics.text(font, text, x, y, 0xFFE8E8E8, true);
	}
}
