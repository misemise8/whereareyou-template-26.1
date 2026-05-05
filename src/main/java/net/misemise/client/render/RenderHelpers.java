package net.misemise.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.UUID;

public final class RenderHelpers {
	private RenderHelpers() {
	}

	public static void renderPlayerIcon(GuiGraphicsExtractor graphics, UUID uuid, int x, int y, int size) {
		Minecraft client = Minecraft.getInstance();
		PlayerSkin skin = DefaultPlayerSkin.get(uuid);
		if (client.getConnection() != null) {
			PlayerInfo info = client.getConnection().getPlayerInfo(uuid);
			if (info != null) {
				skin = info.getSkin();
			}
		}
		PlayerFaceExtractor.extractRenderState(graphics, skin, x, y, size);
	}

	public static String prettyDimension(String dimension) {
		int index = dimension.indexOf(':');
		String path = index >= 0 ? dimension.substring(index + 1) : dimension;
		return switch (path) {
			case "overworld" -> I18n.get("config.whereareyou.dimension.overworld");
			case "the_nether" -> I18n.get("config.whereareyou.dimension.nether");
			case "the_end" -> I18n.get("config.whereareyou.dimension.end");
			default -> path;
		};
	}

	public static String ellipsize(Font font, String text, int maxWidth) {
		if (text == null || text.isEmpty() || maxWidth <= 0) {
			return "";
		}
		if (font.width(text) <= maxWidth) {
			return text;
		}
		String suffix = "...";
		int suffixWidth = font.width(suffix);
		if (suffixWidth > maxWidth) {
			return "";
		}
		int end = text.length();
		while (end > 0 && font.width(text.substring(0, end)) + suffixWidth > maxWidth) {
			end--;
		}
		return end == 0 ? suffix : text.substring(0, end) + suffix;
	}
}
