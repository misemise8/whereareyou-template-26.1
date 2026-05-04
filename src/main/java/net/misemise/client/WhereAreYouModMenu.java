package net.misemise.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.misemise.client.gui.WhereAreYouConfigScreen;

public class WhereAreYouModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return WhereAreYouConfigScreen::create;
	}
}
