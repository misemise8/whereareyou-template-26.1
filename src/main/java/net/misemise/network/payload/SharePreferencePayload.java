package net.misemise.network.payload;

import net.misemise.WhereAreYou;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SharePreferencePayload(boolean sharing) implements CustomPacketPayload {
	public static final Type<SharePreferencePayload> TYPE = new Type<>(WhereAreYou.id("share_preference"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SharePreferencePayload> CODEC = StreamCodec.ofMember(SharePreferencePayload::write, SharePreferencePayload::read);

	public static SharePreferencePayload read(RegistryFriendlyByteBuf buffer) {
		return new SharePreferencePayload(buffer.readBoolean());
	}

	public void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeBoolean(sharing);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
