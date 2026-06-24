package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.networking.RWStreamCodecs;
import com.xeli.createvoidway.blocks.terminal.VoidNodeService;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PortableVoidTerminalTeleportPacket(InteractionHand hand, NetworkKey networkKey,
		ResourceLocation targetDimension, BlockPos targetPos) implements CustomPacketPayload {

	public static final Type<PortableVoidTerminalTeleportPacket> TYPE = new Type<>(
			VoidwayMod.asResource("portable_void_terminal_teleport"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PortableVoidTerminalTeleportPacket> STREAM_CODEC = StreamCodec.composite(
			RWStreamCodecs.INTERACTION_HAND, PortableVoidTerminalTeleportPacket::hand,
			RWStreamCodecs.NETWORK_KEY, PortableVoidTerminalTeleportPacket::networkKey,
			ResourceLocation.STREAM_CODEC, PortableVoidTerminalTeleportPacket::targetDimension,
			BlockPos.STREAM_CODEC, PortableVoidTerminalTeleportPacket::targetPos,
			PortableVoidTerminalTeleportPacket::new);

	public static void handle(PortableVoidTerminalTeleportPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player)
				VoidNodeService.teleportViaPortable(player, packet.hand(), packet.networkKey(),
						packet.targetDimension(), packet.targetPos());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}
