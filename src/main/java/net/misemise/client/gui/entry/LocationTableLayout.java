package net.misemise.client.gui.entry;

final class LocationTableLayout {
	static final int ROW_HEIGHT = 28;
	static final int ICON_SIZE = 16;
	static final int CELL_PADDING = 6;

	private LocationTableLayout() {
	}

	static int playerColumnLeft(int x) {
		return x + CELL_PADDING;
	}

	static int playerTextX(int x) {
		return x + 28;
	}

	static int locationColumnLeft(int x, int entryWidth) {
		int split = Math.max(220, entryWidth / 3);
		return x + Math.min(420, split);
	}

	static int locationTextX(int x, int entryWidth) {
		return locationColumnLeft(x, entryWidth) + CELL_PADDING;
	}

	static int columnRight(int x, int entryWidth) {
		return x + entryWidth - 4;
	}
}
