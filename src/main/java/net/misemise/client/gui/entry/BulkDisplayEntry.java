package net.misemise.client.gui.entry;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.misemise.client.config.ClientSettings;
import net.misemise.client.config.WhereAreYouClientConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class BulkDisplayEntry extends AbstractConfigListEntry<Void> {
	private static final int BUTTON_HEIGHT = 20;

	private final ClientSettings settings;
	private final List<DisplayPlayer> players;
	private final List<Button> buttons;

	public BulkDisplayEntry(ClientSettings settings, List<DisplayPlayer> players) {
		super(Component.translatable("config.whereareyou.players.bulk"), false);
		this.settings = settings;
		this.players = players;
		this.buttons = List.of(
				button("config.whereareyou.players.hud_on", () -> setAll(true, null)),
				button("config.whereareyou.players.hud_off", () -> setAll(false, null)),
				button("config.whereareyou.players.overlay_on", () -> setAll(null, true)),
				button("config.whereareyou.players.overlay_off", () -> setAll(null, false)),
				button("config.whereareyou.players.all_on", () -> setAll(true, true)),
				button("config.whereareyou.players.all_off", () -> setAll(false, false))
		);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, delta);
		int buttonWidth = Math.min(78, Math.max(56, (entryWidth - 20) / buttons.size()));
		int startX = x + 4;
		for (int i = 0; i < buttons.size(); i++) {
			Button button = buttons.get(i);
			button.setRectangle(buttonWidth - 4, BUTTON_HEIGHT, startX + i * buttonWidth, y + 4);
			button.extractRenderState(graphics, mouseX, mouseY, delta);
		}
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
		return buttons;
	}

	@Override
	public List<? extends NarratableEntry> narratables() {
		return buttons;
	}

	private Button button(String translationKey, Runnable action) {
		return Button.builder(Component.translatable(translationKey), button -> {
			action.run();
			WhereAreYouClientConfig.save();
		}).size(60, BUTTON_HEIGHT).build();
	}

	private void setAll(Boolean hud, Boolean overlay) {
		for (DisplayPlayer player : players) {
			ClientSettings.PlayerDisplay display = settings.displayFor(player.uuid(), player.localPlayer());
			if (hud != null) {
				display.hud = hud;
			}
			if (overlay != null) {
				display.overlay = overlay;
			}
		}
	}
}
