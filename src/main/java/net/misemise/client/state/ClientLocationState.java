package net.misemise.client.state;

import net.misemise.config.ServerRules;
import net.misemise.network.PlayerLocation;
import net.misemise.network.payload.LocationUpdatePayload;
import net.misemise.network.payload.ServerRulesPayload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClientLocationState {
	private static final Map<UUID, PlayerLocation> LOCATIONS = new LinkedHashMap<>();
	private static ServerRules serverRules = new ServerRules();
	private static boolean admin;
	private static boolean toggleHidden;
	private static boolean holdHidden;

	private ClientLocationState() {
	}

	public static void clear() {
		LOCATIONS.clear();
		admin = false;
		toggleHidden = false;
		holdHidden = false;
		serverRules = new ServerRules();
	}

	public static void updateLocations(LocationUpdatePayload payload) {
		LOCATIONS.clear();
		for (PlayerLocation location : payload.players()) {
			LOCATIONS.put(location.uuid(), sanitize(location));
		}
	}

	public static void updateServerRules(ServerRulesPayload payload) {
		ServerRules previous = serverRules;
		serverRules = payload.toRules();
		admin = payload.admin();
		if (previous.allowCoordinates && !serverRules.allowCoordinates
				|| previous.allowDimension && !serverRules.allowDimension
				|| previous.allowDistance && !serverRules.allowDistance) {
			LOCATIONS.replaceAll((uuid, location) -> sanitize(location));
		}
	}

	public static Collection<PlayerLocation> locations() {
		return LOCATIONS.values();
	}

	public static List<PlayerLocation> sortedLocations(Comparator<PlayerLocation> comparator) {
		List<PlayerLocation> locations = new ArrayList<>(LOCATIONS.values());
		locations.sort(comparator);
		return locations;
	}

	public static PlayerLocation get(UUID uuid) {
		return LOCATIONS.get(uuid);
	}

	public static ServerRules serverRules() {
		return serverRules.copy();
	}

	public static boolean canAdmin() {
		return admin;
	}

	public static boolean displaysVisible() {
		return !toggleHidden && !holdHidden;
	}

	public static void toggleHidden() {
		toggleHidden = !toggleHidden;
	}

	public static void setHoldHidden(boolean hidden) {
		holdHidden = hidden;
	}

	private static PlayerLocation sanitize(PlayerLocation location) {
		return new PlayerLocation(
				location.uuid(),
				location.name(),
				location.hasCoordinates() && serverRules.allowCoordinates,
				location.x(),
				location.y(),
				location.z(),
				location.hasDimension() && serverRules.allowDimension,
				location.dimension(),
				location.hasDistance() && serverRules.allowDistance,
				location.distance()
		);
	}
}
