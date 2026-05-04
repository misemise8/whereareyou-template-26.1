package net.misemise.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.misemise.network.payload.AdminRulesPayload;
import net.misemise.network.payload.LocationUpdatePayload;
import net.misemise.network.payload.RequestSyncPayload;
import net.misemise.network.payload.ServerRulesPayload;
import net.misemise.network.payload.SharePreferencePayload;

public final class WhereAreYouNetworking {
	private WhereAreYouNetworking() {
	}

	public static void registerPayloadTypes() {
		PayloadTypeRegistry.clientboundPlay().register(LocationUpdatePayload.TYPE, LocationUpdatePayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ServerRulesPayload.TYPE, ServerRulesPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SharePreferencePayload.TYPE, SharePreferencePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(AdminRulesPayload.TYPE, AdminRulesPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RequestSyncPayload.TYPE, RequestSyncPayload.CODEC);
	}
}
