package net.misemise.config;

public class ServerRules {
	public static final int DEFAULT_SYNC_INTERVAL_TICKS = 20;
	public static final int MIN_SYNC_INTERVAL_TICKS = 1;
	public static final int MAX_SYNC_INTERVAL_TICKS = 200;

	public boolean enabled = true;
	public boolean defaultSharing = true;
	public boolean allowCoordinates = true;
	public boolean allowDistance = true;
	public boolean allowDimension = true;
	public int syncIntervalTicks = DEFAULT_SYNC_INTERVAL_TICKS;

	public ServerRules copy() {
		ServerRules copy = new ServerRules();
		copy.enabled = enabled;
		copy.defaultSharing = defaultSharing;
		copy.allowCoordinates = allowCoordinates;
		copy.allowDistance = allowDistance;
		copy.allowDimension = allowDimension;
		copy.syncIntervalTicks = syncIntervalTicks;
		return copy;
	}

	public void clamp() {
		if (syncIntervalTicks < MIN_SYNC_INTERVAL_TICKS) {
			syncIntervalTicks = MIN_SYNC_INTERVAL_TICKS;
		}
		if (syncIntervalTicks > MAX_SYNC_INTERVAL_TICKS) {
			syncIntervalTicks = MAX_SYNC_INTERVAL_TICKS;
		}
	}
}
