package net.misemise.network.payload;

import net.misemise.WhereAreYou;
import net.misemise.config.ServerRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerRulesPayload(
		boolean enabled,
		boolean defaultSharing,
		boolean allowCoordinates,
		boolean allowDistance,
		boolean allowDimension,
		int syncIntervalTicks,
		boolean admin
) implements CustomPacketPayload {
	public static final Type<ServerRulesPayload> TYPE = new Type<>(WhereAreYou.id("server_rules"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerRulesPayload> CODEC = StreamCodec.ofMember(ServerRulesPayload::write, ServerRulesPayload::read);

	public static ServerRulesPayload from(ServerRules rules, boolean admin) {
		return new ServerRulesPayload(
				rules.enabled,
				rules.defaultSharing,
				rules.allowCoordinates,
				rules.allowDistance,
				rules.allowDimension,
				rules.syncIntervalTicks,
				admin
		);
	}

	public ServerRules toRules() {
		ServerRules rules = new ServerRules();
		rules.enabled = enabled;
		rules.defaultSharing = defaultSharing;
		rules.allowCoordinates = allowCoordinates;
		rules.allowDistance = allowDistance;
		rules.allowDimension = allowDimension;
		rules.syncIntervalTicks = syncIntervalTicks;
		rules.clamp();
		return rules;
	}

	public static ServerRulesPayload read(RegistryFriendlyByteBuf buffer) {
		return new ServerRulesPayload(
				buffer.readBoolean(),
				buffer.readBoolean(),
				buffer.readBoolean(),
				buffer.readBoolean(),
				buffer.readBoolean(),
				buffer.readVarInt(),
				buffer.readBoolean()
		);
	}

	public void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeBoolean(enabled);
		buffer.writeBoolean(defaultSharing);
		buffer.writeBoolean(allowCoordinates);
		buffer.writeBoolean(allowDistance);
		buffer.writeBoolean(allowDimension);
		buffer.writeVarInt(syncIntervalTicks);
		buffer.writeBoolean(admin);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
