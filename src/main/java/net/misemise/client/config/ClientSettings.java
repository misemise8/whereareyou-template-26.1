package net.misemise.client.config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientSettings {
	public boolean shareLocation = true;
	public boolean hudEnabled = true;
	public boolean overlayEnabled = true;
	public boolean showIcon = true;
	public boolean showName = true;
	public boolean showDistance = true;
	public boolean showCoordinates = false;
	public boolean showDimension = false;
	public boolean groupByDimension = true;
	public HudPosition hudPosition = HudPosition.TOP_LEFT;
	public int hudXOffset = 4;
	public int hudYOffset = 4;
	public int hudScale = 100;
	public int maxPlayers = 8;
	public SortMode sortMode = SortMode.MCID;
	public DisplayKeyMode displayKeyMode = DisplayKeyMode.TOGGLE;
	public OverlayContentMode overlayContentMode = OverlayContentMode.ICON_NAME_DISTANCE;
	public int overlayScale = 100;
	public int overlayBackgroundOpacity = 50;
	public Map<String, PlayerDisplay> playerDisplays = new HashMap<>();

	public PlayerDisplay displayFor(UUID uuid, boolean localPlayer) {
		String key = uuid.toString();
		return playerDisplays.computeIfAbsent(key, ignored -> new PlayerDisplay(!localPlayer, !localPlayer));
	}

	public void clamp() {
		if (hudXOffset < -200) {
			hudXOffset = -200;
		}
		if (hudXOffset > 200) {
			hudXOffset = 200;
		}
		if (hudYOffset < -200) {
			hudYOffset = -200;
		}
		if (hudYOffset > 200) {
			hudYOffset = 200;
		}
		if (hudScale < 50) {
			hudScale = 50;
		}
		if (hudScale > 150) {
			hudScale = 150;
		}
		if (maxPlayers < 1) {
			maxPlayers = 1;
		}
		if (maxPlayers > 32) {
			maxPlayers = 32;
		}
		if (overlayContentMode == null) {
			overlayContentMode = OverlayContentMode.ICON_NAME_DISTANCE;
		}
		if (overlayScale < 50) {
			overlayScale = 50;
		}
		if (overlayScale > 150) {
			overlayScale = 150;
		}
		if (overlayBackgroundOpacity < 10) {
			overlayBackgroundOpacity = 10;
		}
		if (overlayBackgroundOpacity > 100) {
			overlayBackgroundOpacity = 100;
		}
	}

	public enum HudPosition {
		TOP_LEFT,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_RIGHT
	}

	public enum SortMode {
		MCID,
		DISTANCE
	}

	public enum DisplayKeyMode {
		TOGGLE,
		HOLD
	}

	public enum OverlayContentMode {
		ICON_NAME_DISTANCE,
		NAME_DISTANCE,
		DISTANCE
	}

	public static class PlayerDisplay {
		public boolean hud;
		public boolean overlay;

		public PlayerDisplay() {
			this(true, true);
		}

		public PlayerDisplay(boolean hud, boolean overlay) {
			this.hud = hud;
			this.overlay = overlay;
		}
	}
}
