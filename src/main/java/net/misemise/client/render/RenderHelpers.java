package net.misemise.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public final class RenderHelpers {
	private RenderHelpers() {
	}

	public static void renderPlayerIcon(GuiGraphicsExtractor graphics, UUID uuid, int x, int y, int size) {
		Minecraft client = Minecraft.getInstance();
		if (client.getConnection() != null) {
			PlayerInfo info = client.getConnection().getPlayerInfo(uuid);
			if (info != null) {
				Identifier texture = info.getSkin().body().texturePath();
				graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 8.0F, 8.0F, size, size, 64, 64);
				graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 40.0F, 8.0F, size, size, 64, 64);
				return;
			}
		}
		graphics.fill(x, y, x + size, y + size, colorFor(uuid.hashCode()));
	}

	public static String prettyDimension(String dimension) {
		int index = dimension.indexOf(':');
		String path = index >= 0 ? dimension.substring(index + 1) : dimension;
		return switch (path) {
			case "overworld" -> "Overworld";
			case "the_nether" -> "Nether";
			case "the_end" -> "The End";
			default -> path;
		};
	}

	private static int colorFor(int seed) {
		int r = 80 + Math.floorMod(seed, 120);
		int g = 80 + Math.floorMod(seed / 31, 120);
		int b = 80 + Math.floorMod(seed / 997, 120);
		return 0xFF000000 | r << 16 | g << 8 | b;
	}
}
