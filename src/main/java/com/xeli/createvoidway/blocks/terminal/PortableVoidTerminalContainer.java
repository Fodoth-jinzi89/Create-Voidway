package com.xeli.createvoidway.blocks.terminal;

import com.xeli.createvoidway.items.PortableVoidTerminalBinding;
import com.xeli.createvoidway.items.RWItems;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.networking.packets.PortableVoidTerminalListPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PortableVoidTerminalContainer extends AbstractContainerMenu {

	private final InteractionHand hand;
	private final NetworkKey networkKey;
	private final List<VoidNodeEntry> nodes = new ArrayList<>();
	private int selectedIndex = -1;

	public PortableVoidTerminalContainer(MenuType<?> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
		super(type, id);
		hand = extraData.readEnum(InteractionHand.class);
		networkKey = NetworkKey.fromBuffer(extraData);
		PortableVoidTerminalListPacket.applyPending(this);
	}

	public PortableVoidTerminalContainer(MenuType<?> type, int id, Inventory inv, InteractionHand hand,
			NetworkKey networkKey) {
		super(type, id);
		this.hand = hand;
		this.networkKey = networkKey;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(RWItems.PORTABLE_VOID_TERMINAL.get()))
			return false;
		return PortableVoidTerminalBinding.read(stack, player.registryAccess())
				.filter(key -> key.equals(networkKey))
				.isPresent();
	}

	public InteractionHand getHand() {
		return hand;
	}

	public NetworkKey getNetworkKey() {
		return networkKey;
	}

	public boolean matchesBinding(InteractionHand packetHand, NetworkKey key) {
		return hand == packetHand && networkKey.equals(key);
	}

	public void updateNodes(List<VoidNodeEntry> entries) {
		nodes.clear();
		nodes.addAll(entries);
		if (selectedIndex >= nodes.size())
			selectedIndex = nodes.isEmpty() ? -1 : 0;
		else if (selectedIndex < 0 && !nodes.isEmpty())
			selectedIndex = 0;
	}

	public List<VoidNodeEntry> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int index) {
		if (index >= 0 && index < nodes.size())
			selectedIndex = index;
	}

	public VoidNodeEntry getSelectedNode() {
		if (selectedIndex < 0 || selectedIndex >= nodes.size())
			return null;
		return nodes.get(selectedIndex);
	}

}
