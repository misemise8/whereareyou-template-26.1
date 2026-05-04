package net.misemise.client.render;

import net.minecraft.client.Minecraft;
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
}
