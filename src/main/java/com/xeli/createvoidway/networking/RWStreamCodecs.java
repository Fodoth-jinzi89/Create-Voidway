package com.xeli.createvoidway.networking;

import com.xeli.createvoidway.networking.packets.PortableVoidTerminalListPacket;
import com.xeli.createvoidway.networking.packets.PortableVoidTerminalRequestListPacket;
import com.xeli.createvoidway.networking.packets.PortableVoidTerminalTeleportPacket;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.codec.StreamCodec;

public final class RWStreamCodecs {

	private RWStreamCodecs() {
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, InteractionHand> INTERACTION_HAND = StreamCodec.of(
			(buf, hand) -> buf.writeEnum(hand),
			buf -> buf.readEnum(InteractionHand.class));

	public static final StreamCodec<RegistryFriendlyByteBuf, NetworkKey> NETWORK_KEY = StreamCodec.of(
			(buf, key) -> key.writeToBuffer(buf),
			NetworkKey::fromBuffer);

}
