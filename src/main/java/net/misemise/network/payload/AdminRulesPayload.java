package net.misemise.network.payload;

import net.misemise.WhereAreYou;
import net.misemise.config.ServerRules;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AdminRulesPayload(
		boolean enabled,
		boolean defaultSharing,
		boolean allowCoordinates,
		boolean allowDistance,
		boolean allowDimension,
		int syncIntervalTicks
) implements CustomPacketPayload {
	public static final Type<AdminRulesPayload> TYPE = new Type<>(WhereAreYou.id("admin_rules"));
	public static final StreamCodec<RegistryFriendlyByteBuf, AdminRulesPayload> CODEC = StreamCodec.ofMember(AdminRulesPayload::write, AdminRulesPayload::read);

	public static AdminRulesPayload from(ServerRules rules) {
		return new AdminRulesPayload(
				rules.enabled,
				rules.defaultSharing,
				rules.allowCoordinates,
				rules.allowDistance,
				rules.allowDimension,
				rules.syncIntervalTicks
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

	public static AdminRulesPayload read(RegistryFriendlyByteBuf buffer) {
		return new AdminRulesPayload(
				buffer.readBoolean(),
				buffer.readBoolean(),
				buffer.readBoolean(),
				buffer.readBoolean(),
				buffer.readBoolean(),
				buffer.readVarInt()
		);
	}

	public void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeBoolean(enabled);
		buffer.writeBoolean(defaultSharing);
		buffer.writeBoolean(allowCoordinates);
		buffer.writeBoolean(allowDistance);
		buffer.writeBoolean(allowDimension);
		buffer.writeVarInt(syncIntervalTicks);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
