package net.misemise.client.gui.entry;

import com.mojang.authlib.GameProfile;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.misemise.client.config.ClientSettings;
import net.misemise.client.config.WhereAreYouClientConfig;
import net.misemise.client.render.RenderHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class PlayerDisplayEntry extends AbstractConfigListEntry<Void> {
	private final GameProfile profile;
	private final ClientSettings.PlayerDisplay display;
	private final boolean localPlayer;
	private final Button hudButton;
	private final Button overlayButton;

	public PlayerDisplayEntry(GameProfile profile, ClientSettings.PlayerDisplay display, boolean localPlayer) {
		super(Component.literal(profile.name()), false);
		this.profile = profile;
		this.display = display;
		this.localPlayer = localPlayer;
		this.hudButton = Button.builder(Component.empty(), button -> {
			display.hud = !display.hud;
			updateButtonLabels();
			WhereAreYouClientConfig.save();
		}).size(58, 20).build();
		this.overlayButton = Button.builder(Component.empty(), button -> {
			display.overlay = !display.overlay;
			updateButtonLabels();
			WhereAreYouClientConfig.save();
		}).size(76, 20).build();
		updateButtonLabels();
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		if (hovered) {
			graphics.fill(x, y, x + entryWidth, y + entryHeight, 0x22000000);
		}
		int iconX = x + 6;
		int iconY = y + 5;
		RenderHelpers.renderPlayerIcon(graphics, profile.id(), iconX, iconY, 16);
		String label = localPlayer ? profile.name() + " (you)" : profile.name();
		graphics.text(Minecraft.getInstance().font, label, x + 28, y + 9, 0xFFFFFFFF, true);

		int overlayX = x + entryWidth - 84;
		int hudX = overlayX - 66;
		hudButton.setRectangle(hudX, y + 4, 58, 20);
		overlayButton.setRectangle(overlayX, y + 4, 76, 20);
		hudButton.extractRenderState(graphics, mouseX, mouseY, delta);
		overlayButton.extractRenderState(graphics, mouseX, mouseY, delta);
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
		return List.of(hudButton, overlayButton);
	}

	@Override
	public List<? extends NarratableEntry> narratables() {
		return List.of(hudButton, overlayButton);
	}

	private void updateButtonLabels() {
		hudButton.setMessage(Component.literal(display.hud ? "HUD ON" : "HUD OFF"));
		overlayButton.setMessage(Component.literal(display.overlay ? "Overlay ON" : "Overlay OFF"));
	}
}
