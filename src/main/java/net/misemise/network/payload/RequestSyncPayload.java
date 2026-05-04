package net.misemise.network.payload;

import net.misemise.WhereAreYou;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestSyncPayload() implements CustomPacketPayload {
	public static final Type<RequestSyncPayload> TYPE = new Type<>(WhereAreYou.id("request_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestSyncPayload> CODEC = StreamCodec.ofMember(RequestSyncPayload::write, RequestSyncPayload::read);

	public static RequestSyncPayload read(RegistryFriendlyByteBuf buffer) {
		return new RequestSyncPayload();
	}

	public void write(RegistryFriendlyByteBuf buffer) {
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
