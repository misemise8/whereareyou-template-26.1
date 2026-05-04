package net.misemise.client.gui.entry;

import me.shedaniel.clothconfig2.gui.entries.IntegerSliderEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LiveIntSliderEntry extends IntegerSliderEntry {
	private final Consumer<Integer> liveConsumer;

	public LiveIntSliderEntry(Component fieldName, int value, int min, int max, Component resetButtonKey, Supplier<Integer> defaultValue, Consumer<Integer> saveConsumer, Consumer<Integer> liveConsumer) {
		super(fieldName, value, min, max, resetButtonKey, defaultValue, saveConsumer, Optional::empty, false);
		this.liveConsumer = liveConsumer;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		liveConsumer.accept(getValue());
		super.extractRenderState(graphics, index, x, y, entryWidth, entryHeight, mouseX, mouseY, hovered, delta);
	}
}
