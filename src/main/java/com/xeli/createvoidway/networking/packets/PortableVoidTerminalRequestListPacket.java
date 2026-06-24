package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.networking.RWStreamCodecs;
import com.xeli.createvoidway.blocks.terminal.VoidNodeService;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PortableVoidTerminalRequestListPacket(InteractionHand hand, NetworkKey networkKey)
		implements CustomPacketPayload {

	public static final Type<PortableVoidTerminalRequestListPacket> TYPE = new Type<>(
			VoidwayMod.asResource("portable_void_terminal_request_list"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PortableVoidTerminalRequestListPacket> STREAM_CODEC = StreamCodec.composite(
			RWStreamCodecs.INTERACTION_HAND, PortableVoidTerminalRequestListPacket::hand,
			RWStreamCodecs.NETWORK_KEY, PortableVoidTerminalRequestListPacket::networkKey,
			PortableVoidTerminalRequestListPacket::new);

	public static void handle(PortableVoidTerminalRequestListPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player))
				return;
			VoidNodeService.sendPortableNodeList(player, packet.hand(), packet.networkKey());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}
