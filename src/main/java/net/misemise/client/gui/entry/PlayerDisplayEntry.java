package net.misemise.client.gui.entry;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.misemise.client.config.ClientSettings;
import net.misemise.client.config.WhereAreYouClientConfig;
import net.misemise.client.render.RenderHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class PlayerDisplayEntry extends AbstractConfigListEntry<Void> {
	private static final int ROW_FILL_COLOR = 0x12000000;
	private static final int ROW_HOVER_FILL_COLOR = 0x26000000;
	private static final int TEXT_COLOR = 0xFFFFFFFF;
	private static final int LOCAL_TEXT_COLOR = 0xFFFFD36F;

	private final DisplayPlayer player;
	private final ClientSettings.PlayerDisplay display;
	private final Button hudButton;
	private final Button overlayButton;

	public PlayerDisplayEntry(DisplayPlayer player, ClientSettings.PlayerDisplay display) {
		super(Component.literal(player.name()), false);
		this.player = player;
		this.display = display;
		this.hudButton = Button.builder(Component.empty(), button -> {
			display.hud = !display.hud;
			updateButtonLabels();
			WhereAreYouClientConfig.save();
		}).size(PlayerTableLayout.BUTTON_WIDTH, PlayerTableLayout.BUTTON_HEIGHT).build();
		this.overlayButton = Button.builder(Component.empty(), button -> {
			display.overlay = !display.overlay;
			updateButtonLabels();
			WhereAreYouClientConfig.save();
		}).size(PlayerTableLayout.BUTTON_WIDTH, PlayerTableLayout.BUTTON_HEIGHT).build();
		updateButtonLabels();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, delta);
		int bottom = y + entryHeight;
		int hudLeft = PlayerTableLayout.hudColumnLeft(x, entryWidth);
		int overlayLeft = PlayerTableLayout.overlayColumnLeft(x, entryWidth);
		int right = PlayerTableLayout.columnRight(x, entryWidth);
		graphics.fill(x, y, right, bottom, hovered ? ROW_HOVER_FILL_COLOR : ROW_FILL_COLOR);
		PlayerTableHeaderEntry.drawGrid(graphics, x, y, bottom, hudLeft, overlayLeft, right);

		Font font = Minecraft.getInstance().font;
		int iconX = PlayerTableLayout.playerColumnLeft(x);
		int iconY = y + 5;
		RenderHelpers.renderPlayerIcon(graphics, player.uuid(), iconX, iconY, PlayerTableLayout.ICON_SIZE);
		String label = player.localPlayer() ? I18n.get("config.whereareyou.players.you", player.name()) : player.name();
		int maxNameWidth = hudLeft - PlayerTableLayout.playerTextX(x) - PlayerTableLayout.CELL_PADDING;
		label = RenderHelpers.ellipsize(font, label, maxNameWidth);
		graphics.text(font, label, PlayerTableLayout.playerTextX(x), y + 9, player.localPlayer() ? LOCAL_TEXT_COLOR : TEXT_COLOR, true);

		int hudX = PlayerTableLayout.hudButtonX(x, entryWidth);
		int overlayX = PlayerTableLayout.overlayButtonX(x, entryWidth);
		updateButtonLabels();
		hudButton.setRectangle(PlayerTableLayout.BUTTON_WIDTH, PlayerTableLayout.BUTTON_HEIGHT, hudX, y + PlayerTableLayout.BUTTON_TOP_PADDING);
		overlayButton.setRectangle(PlayerTableLayout.BUTTON_WIDTH, PlayerTableLayout.BUTTON_HEIGHT, overlayX, y + PlayerTableLayout.BUTTON_TOP_PADDING);
		hudButton.extractRenderState(graphics, mouseX, mouseY, delta);
		overlayButton.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	@Override
	public int getItemHeight() {
		return PlayerTableLayout.ROW_HEIGHT;
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
		return List.of(hudButton, overlayButton);
	}

	@Override
	public List<? extends NarratableEntry> narratables() {
		return List.of(hudButton, overlayButton);
	}

	private void updateButtonLabels() {
		hudButton.setMessage(Component.translatable(display.hud ? "config.whereareyou.state.on" : "config.whereareyou.state.off"));
		overlayButton.setMessage(Component.translatable(display.overlay ? "config.whereareyou.state.on" : "config.whereareyou.state.off"));
	}
}
