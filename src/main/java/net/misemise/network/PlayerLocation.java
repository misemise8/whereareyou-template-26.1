package net.misemise.network;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.UUID;

public record PlayerLocation(
		UUID uuid,
		String name,
		boolean hasCoordinates,
		double x,
		double y,
		double z,
		boolean hasDimension,
		String dimension,
		boolean hasDistance,
		double distance
) {
	public static PlayerLocation read(RegistryFriendlyByteBuf buffer) {
		UUID uuid = buffer.readUUID();
		String name = buffer.readUtf(64);
		boolean hasCoordinates = buffer.readBoolean();
		double x = hasCoordinates ? buffer.readDouble() : 0.0D;
		double y = hasCoordinates ? buffer.readDouble() : 0.0D;
		double z = hasCoordinates ? buffer.readDouble() : 0.0D;
		boolean hasDimension = buffer.readBoolean();
		String dimension = hasDimension ? buffer.readUtf(128) : "";
		boolean hasDistance = buffer.readBoolean();
		double distance = hasDistance ? buffer.readDouble() : 0.0D;
		return new PlayerLocation(uuid, name, hasCoordinates, x, y, z, hasDimension, dimension, hasDistance, distance);
	}

	public void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeUUID(uuid);
		buffer.writeUtf(name, 64);
		buffer.writeBoolean(hasCoordinates);
		if (hasCoordinates) {
			buffer.writeDouble(x);
			buffer.writeDouble(y);
			buffer.writeDouble(z);
		}
		buffer.writeBoolean(hasDimension);
		if (hasDimension) {
			buffer.writeUtf(dimension, 128);
		}
		buffer.writeBoolean(hasDistance);
		if (hasDistance) {
			buffer.writeDouble(distance);
		}
	}
}
