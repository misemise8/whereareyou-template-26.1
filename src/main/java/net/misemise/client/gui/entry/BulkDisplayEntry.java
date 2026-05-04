package net.misemise.client.gui.entry;

import com.mojang.authlib.GameProfile;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.misemise.client.config.ClientSettings;
import net.misemise.client.config.WhereAreYouClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BulkDisplayEntry extends AbstractConfigListEntry<Void> {
	private static final int BUTTON_HEIGHT = 20;

	private final ClientSettings settings;
	private final List<GameProfile> profiles;
	private final List<Button> buttons;

	public BulkDisplayEntry(ClientSettings settings, List<GameProfile> profiles) {
		super(Component.literal("Bulk display controls"), false);
		this.settings = settings;
		this.profiles = profiles;
		this.buttons = List.of(
				button("HUD ON", () -> setAll(true, null)),
				button("HUD OFF", () -> setAll(false, null)),
				button("Overlay ON", () -> setAll(null, true)),
				button("Overlay OFF", () -> setAll(null, false)),
				button("All ON", () -> setAll(true, true)),
				button("All OFF", () -> setAll(false, false))
		);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		int buttonWidth = Math.max(56, (entryWidth - 20) / buttons.size());
		int startX = x + 4;
		for (int i = 0; i < buttons.size(); i++) {
			Button button = buttons.get(i);
			button.setRectangle(startX + i * buttonWidth, y + 4, buttonWidth - 4, BUTTON_HEIGHT);
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

	private Button button(String label, Runnable action) {
		return Button.builder(Component.literal(label), button -> {
			action.run();
			WhereAreYouClientConfig.save();
		}).size(60, BUTTON_HEIGHT).build();
	}

	private void setAll(Boolean hud, Boolean overlay) {
		for (GameProfile profile : profiles) {
			if (profile.id() == null) {
				continue;
			}
			ClientSettings.PlayerDisplay display = settings.displayFor(profile.id(), isLocalPlayer(profile.id()));
			if (hud != null) {
				display.hud = hud;
			}
			if (overlay != null) {
				display.overlay = overlay;
			}
		}
	}

	private boolean isLocalPlayer(UUID uuid) {
		Minecraft client = Minecraft.getInstance();
		return client.player != null && client.player.getUUID().equals(uuid);
	}
}
