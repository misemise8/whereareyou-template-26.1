package net.misemise.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.misemise.client.config.ClientSettings;
import net.misemise.client.config.WhereAreYouClientConfig;
import net.misemise.client.state.ClientLocationState;
import net.misemise.network.PlayerLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public final class WhereAreYouWorldOverlay {
	private WhereAreYouWorldOverlay() {
	}

	public static void render(LevelRenderContext context) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null || client.options.hideGui || !ClientLocationState.displaysVisible()) {
			return;
		}
		ClientSettings settings = WhereAreYouClientConfig.active();
		if (!settings.overlayEnabled) {
			return;
		}

		String localDimension = client.level.dimension().identifier().toString();
		List<PlayerLocation> locations = ClientLocationState.sortedLocations(comparator(settings));
		int rendered = 0;
		for (PlayerLocation location : locations) {
			if (rendered >= settings.maxPlayers) {
				break;
			}
			if (!location.hasDimension() || !localDimension.equals(location.dimension())) {
				continue;
			}
			boolean local = client.player.getUUID().equals(location.uuid());
			ClientSettings.PlayerDisplay display = settings.displayFor(location.uuid(), local);
			if (!display.overlay || local) {
				continue;
			}
			Entity entity = client.level.getEntity(location.uuid());
			if (entity == null) {
				continue;
			}
			renderNameplate(context, client, entity, overlayLabel(location));
			rendered++;
		}
	}

	private static void renderNameplate(LevelRenderContext context, Minecraft client, Entity entity, String text) {
		Vec3 camera = context.levelState().cameraRenderState.pos;
		double x = entity.getX() - camera.x;
		double y = entity.getY() - camera.y;
		double z = entity.getZ() - camera.z;
		Vec3 attachment = new Vec3(0.0D, entity.getBbHeight() + 0.35D, 0.0D);
		double distanceToCameraSq = entity.distanceToSqr(camera);
		PoseStack pose = context.poseStack();
		pose.pushPose();
		pose.translate(x, y, z);
		context.submitNodeCollector().submitNameTag(
				pose,
				attachment,
				0,
				Component.literal(text),
				true,
				LightCoordsUtil.FULL_BRIGHT,
				distanceToCameraSq,
				context.levelState().cameraRenderState
		);
		pose.popPose();
	}

	private static Comparator<PlayerLocation> comparator(ClientSettings settings) {
		Comparator<PlayerLocation> byName = Comparator.comparing(PlayerLocation::name, String.CASE_INSENSITIVE_ORDER);
		if (settings.sortMode == ClientSettings.SortMode.DISTANCE) {
			return Comparator
					.comparingDouble((PlayerLocation location) -> location.hasDistance() ? location.distance() : Double.MAX_VALUE)
					.thenComparing(byName);
		}
		return byName;
	}

	private static String overlayLabel(PlayerLocation location) {
		if (location.hasDistance()) {
			return location.name() + " " + String.format("%.0fm", location.distance());
		}
		return location.name();
	}
}
