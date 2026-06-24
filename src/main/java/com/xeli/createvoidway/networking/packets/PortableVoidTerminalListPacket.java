package com.xeli.createvoidway.networking.packets;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.networking.RWStreamCodecs;
import com.xeli.createvoidway.blocks.terminal.PortableVoidTerminalContainer;
import com.xeli.createvoidway.blocks.terminal.PortableVoidTerminalScreen;
import com.xeli.createvoidway.blocks.terminal.VoidNodeEntry;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PortableVoidTerminalListPacket(InteractionHand hand, NetworkKey networkKey, List<VoidNodeEntry> nodes)
		implements CustomPacketPayload {

	public record ListKey(InteractionHand hand, NetworkKey networkKey) {
	}

	private static final Map<ListKey, List<VoidNodeEntry>> PENDING = new HashMap<>();

	public static final Type<PortableVoidTerminalListPacket> TYPE = new Type<>(
			VoidwayMod.asResource("portable_void_terminal_list"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PortableVoidTerminalListPacket> STREAM_CODEC = StreamCodec.composite(
			RWStreamCodecs.INTERACTION_HAND, PortableVoidTerminalListPacket::hand,
			RWStreamCodecs.NETWORK_KEY, PortableVoidTerminalListPacket::networkKey,
			VoidNodeEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), PortableVoidTerminalListPacket::nodes,
			PortableVoidTerminalListPacket::new);

	public static void handle(PortableVoidTerminalListPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() == null)
				return;
			if (context.player().containerMenu instanceof PortableVoidTerminalContainer menu
					&& menu.matchesBinding(packet.hand(), packet.networkKey())) {
				menu.updateNodes(packet.nodes());
				PENDING.remove(new ListKey(packet.hand(), packet.networkKey()));
				if (Minecraft.getInstance().screen instanceof PortableVoidTerminalScreen screen
						&& screen.getMenu() == menu)
					screen.onNodesUpdated();
				return;
			}
			PENDING.put(new ListKey(packet.hand(), packet.networkKey()), packet.nodes());
		});
	}

	public static void applyPending(PortableVoidTerminalContainer menu) {
		List<VoidNodeEntry> pending = PENDING.remove(new ListKey(menu.getHand(), menu.getNetworkKey()));
		if (pending != null)
			menu.updateNodes(pending);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

}
