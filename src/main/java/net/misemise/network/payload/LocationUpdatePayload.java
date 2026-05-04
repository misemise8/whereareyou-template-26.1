package net.misemise.network.payload;

import net.misemise.WhereAreYou;
import net.misemise.network.PlayerLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record LocationUpdatePayload(List<PlayerLocation> players) implements CustomPacketPayload {
	public static final Type<LocationUpdatePayload> TYPE = new Type<>(WhereAreYou.id("location_update"));
	public static final StreamCodec<RegistryFriendlyByteBuf, LocationUpdatePayload> CODEC = StreamCodec.ofMember(LocationUpdatePayload::write, LocationUpdatePayload::read);

	public static LocationUpdatePayload read(RegistryFriendlyByteBuf buffer) {
		int count = buffer.readVarInt();
		List<PlayerLocation> players = new ArrayList<>(count);
		for (int index = 0; index < count; index++) {
			players.add(PlayerLocation.read(buffer));
		}
		return new LocationUpdatePayload(players);
	}

	public void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeVarInt(players.size());
		for (PlayerLocation player : players) {
			player.write(buffer);
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
