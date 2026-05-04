package net.misemise.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.misemise.config.ServerRules;
import net.misemise.config.WhereAreYouServerConfig;
import net.misemise.network.PlayerLocation;
import net.misemise.network.payload.AdminRulesPayload;
import net.misemise.network.payload.LocationUpdatePayload;
import net.misemise.network.payload.RequestSyncPayload;
import net.misemise.network.payload.ServerRulesPayload;
import net.misemise.network.payload.SharePreferencePayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WhereAreYouServer {
	private static final Map<UUID, Boolean> SHARE_STATES = new HashMap<>();
	private static int ticksUntilSync = ServerRules.DEFAULT_SYNC_INTERVAL_TICKS;

	private WhereAreYouServer() {
	}

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(SharePreferencePayload.TYPE, (payload, context) -> {
			SHARE_STATES.put(context.player().getUUID(), payload.sharing());
			syncLocations(context.server());
		});
		ServerPlayNetworking.registerGlobalReceiver(AdminRulesPayload.TYPE, (payload, context) -> {
			if (!isOp(context.server(), context.player())) {
				return;
			}
			WhereAreYouServerConfig.update(payload.toRules());
			syncRulesToAll(context.server());
			syncLocations(context.server());
		});
		ServerPlayNetworking.registerGlobalReceiver(RequestSyncPayload.TYPE, (payload, context) -> {
			syncRules(context.player(), context.server());
			syncLocations(context.server());
		});

		ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
			ServerPlayer player = listener.player;
			SHARE_STATES.putIfAbsent(player.getUUID(), WhereAreYouServerConfig.rules().defaultSharing);
			syncRules(player, server);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((listener, server) -> SHARE_STATES.remove(listener.player.getUUID()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SHARE_STATES.clear());
		ServerTickEvents.END_SERVER_TICK.register(WhereAreYouServer::tick);
	}

	private static void tick(MinecraftServer server) {
		ticksUntilSync--;
		if (ticksUntilSync <= 0) {
			ticksUntilSync = WhereAreYouServerConfig.rules().syncIntervalTicks;
			syncLocations(server);
		}
	}

	private static void syncRulesToAll(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			syncRules(player, server);
		}
	}

	private static void syncRules(ServerPlayer player, MinecraftServer server) {
		if (ServerPlayNetworking.canSend(player, ServerRulesPayload.TYPE)) {
			ServerPlayNetworking.send(player, ServerRulesPayload.from(WhereAreYouServerConfig.rules(), isOp(server, player)));
		}
	}

	private static void syncLocations(MinecraftServer server) {
		for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
			if (ServerPlayNetworking.canSend(recipient, LocationUpdatePayload.TYPE)) {
				ServerPlayNetworking.send(recipient, new LocationUpdatePayload(buildLocationsFor(recipient, server)));
			}
		}
	}

	private static List<PlayerLocation> buildLocationsFor(ServerPlayer recipient, MinecraftServer server) {
		ServerRules rules = WhereAreYouServerConfig.rules();
		List<PlayerLocation> locations = new ArrayList<>();
		if (!rules.enabled) {
			return locations;
		}
		for (ServerPlayer target : server.getPlayerList().getPlayers()) {
			boolean sharing = SHARE_STATES.getOrDefault(target.getUUID(), rules.defaultSharing);
			if (!sharing) {
				continue;
			}
			boolean sameDimension = target.level().dimension().equals(recipient.level().dimension());
			Vec3 targetPosition = target.position();
			Vec3 recipientPosition = recipient.position();
			double distance = sameDimension ? targetPosition.distanceTo(recipientPosition) : 0.0D;
			locations.add(new PlayerLocation(
					target.getUUID(),
					target.getGameProfile().name(),
					rules.allowCoordinates,
					target.getX(),
					target.getY(),
					target.getZ(),
					rules.allowDimension,
					target.level().dimension().identifier().toString(),
					rules.allowDistance && sameDimension,
					distance
			));
		}
		return locations;
	}

	private static boolean isOp(MinecraftServer server, ServerPlayer player) {
		return server.getPlayerList().isOp(new NameAndId(player.getGameProfile()));
	}
}
