package com.xeli.createvoidway.items;

import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public final class PortableVoidTerminalBinding {

	private static final String NETWORK_KEY = "NetworkKey";

	private PortableVoidTerminalBinding() {
	}

	public static boolean isBound(ItemStack stack, HolderLookup.Provider registries) {
		return read(stack, registries)
				.map(PortableVoidTerminalBinding::isComplete)
				.orElse(false);
	}

	public static boolean isBound(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		return tag.contains(NETWORK_KEY, Tag.TAG_COMPOUND);
	}

	public static Optional<NetworkKey> read(ItemStack stack, HolderLookup.Provider registries) {
		if (stack.isEmpty())
			return Optional.empty();
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!tag.contains(NETWORK_KEY, Tag.TAG_COMPOUND))
			return Optional.empty();
		return Optional.of(NetworkKey.deserialize(registries, tag.getCompound(NETWORK_KEY)));
	}

	public static void write(ItemStack stack, NetworkKey key, HolderLookup.Provider registries) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.put(NETWORK_KEY, key.serialize(registries));
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static void clear(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.remove(NETWORK_KEY);
		if (tag.isEmpty())
			stack.remove(DataComponents.CUSTOM_DATA);
		else
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static boolean isComplete(NetworkKey key) {
		return !key.frequencies.get(true).getStack().isEmpty() && !key.frequencies.get(false).getStack().isEmpty();
	}

	public static boolean canInteract(net.minecraft.world.entity.player.Player player, NetworkKey key) {
		if (player == null)
			return false;
		if (!player.mayBuild() && !player.isSpectator())
			return false;
		return key.owner == null || player.getGameProfile().equals(key.owner);
	}

}
