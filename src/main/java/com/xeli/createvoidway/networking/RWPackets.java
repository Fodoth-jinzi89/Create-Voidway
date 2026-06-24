package com.xeli.createvoidway.networking;

import com.xeli.createvoidway.networking.packets.PortableVoidTerminalListPacket;
import com.xeli.createvoidway.networking.packets.PortableVoidTerminalRequestListPacket;
import com.xeli.createvoidway.networking.packets.PortableVoidTerminalTeleportPacket;
import com.xeli.createvoidway.networking.packets.VoidBatteryUpdatePacket;
import com.xeli.createvoidway.networking.packets.VoidChestUpdatePacket;
import com.xeli.createvoidway.networking.packets.VoidNodeListPacket;
import com.xeli.createvoidway.networking.packets.VoidNodeRenamePacket;
import com.xeli.createvoidway.networking.packets.VoidNodeRequestListPacket;
import com.xeli.createvoidway.networking.packets.VoidNodeTeleportPacket;
import com.xeli.createvoidway.networking.packets.VoidTankUpdatePacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class RWPackets {

	public static final String NETWORK_VERSION = "8";

	private RWPackets() {
	}

	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
		registrar.playToClient(VoidTankUpdatePacket.TYPE, VoidTankUpdatePacket.STREAM_CODEC, VoidTankUpdatePacket::handle);
		registrar.playToClient(VoidBatteryUpdatePacket.TYPE, VoidBatteryUpdatePacket.STREAM_CODEC, VoidBatteryUpdatePacket::handle);
		registrar.playToClient(VoidChestUpdatePacket.TYPE, VoidChestUpdatePacket.STREAM_CODEC, VoidChestUpdatePacket::handle);
		registrar.playToClient(VoidNodeListPacket.TYPE, VoidNodeListPacket.STREAM_CODEC, VoidNodeListPacket::handle);
		registrar.playToServer(VoidNodeRequestListPacket.TYPE, VoidNodeRequestListPacket.STREAM_CODEC,
				VoidNodeRequestListPacket::handle);
		registrar.playToServer(VoidNodeRenamePacket.TYPE, VoidNodeRenamePacket.STREAM_CODEC, VoidNodeRenamePacket::handle);
		registrar.playToServer(VoidNodeTeleportPacket.TYPE, VoidNodeTeleportPacket.STREAM_CODEC, VoidNodeTeleportPacket::handle);
		registrar.playToServer(PortableVoidTerminalRequestListPacket.TYPE, PortableVoidTerminalRequestListPacket.STREAM_CODEC,
				PortableVoidTerminalRequestListPacket::handle);
		registrar.playToClient(PortableVoidTerminalListPacket.TYPE, PortableVoidTerminalListPacket.STREAM_CODEC,
				PortableVoidTerminalListPacket::handle);
		registrar.playToServer(PortableVoidTerminalTeleportPacket.TYPE, PortableVoidTerminalTeleportPacket.STREAM_CODEC,
				PortableVoidTerminalTeleportPacket::handle);
	}
}
