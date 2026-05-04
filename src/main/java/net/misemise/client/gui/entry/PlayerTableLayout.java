package net.misemise.client.gui.entry;

final class PlayerTableLayout {
	static final int ROW_HEIGHT = 28;
	static final int ICON_SIZE = 16;
	static final int BUTTON_WIDTH = 36;
	static final int BUTTON_HEIGHT = 20;
	static final int BUTTON_TOP_PADDING = 4;
	static final int CELL_PADDING = 6;

	private PlayerTableLayout() {
	}

	static int playerColumnLeft(int x) {
		return x + CELL_PADDING;
	}

	static int playerTextX(int x) {
		return x + 28;
	}

	static int overlayButtonX(int x, int entryWidth) {
		return x + entryWidth - BUTTON_WIDTH - 8;
	}

	static int hudButtonX(int x, int entryWidth) {
		return overlayButtonX(x, entryWidth) - BUTTON_WIDTH - 16;
	}

	static int hudColumnLeft(int x, int entryWidth) {
		return hudButtonX(x, entryWidth) - CELL_PADDING;
	}

	static int overlayColumnLeft(int x, int entryWidth) {
		return overlayButtonX(x, entryWidth) - CELL_PADDING;
	}

	static int columnRight(int x, int entryWidth) {
		return x + entryWidth - 4;
	}
}
