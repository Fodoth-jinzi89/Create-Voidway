package com.xeli.createvoidway.blocks.terminal;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.teleport.VoidTeleportPadTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class VoidNodeDiscovery {

	private VoidNodeDiscovery() {
	}

	public static List<VoidNodeNetworkIndex.DimensionalBlockPos> collectValidTerminals(ServerLevel terminalLevel,
			NetworkKey key) {
		List<VoidNodeNetworkIndex.DimensionalBlockPos> terminals = new ArrayList<>();

		for (VoidNodeNetworkIndex.DimensionalBlockPos node : VoidNodeNetworkIndex.collectPositions(key)) {
			ServerLevel level = terminalLevel.getServer()
					.getLevel(ResourceKey.create(Registries.DIMENSION, node.dimension()));
			if (level == null || !level.hasChunkAt(node.pos()))
				continue;

			VoidLinkBehaviour link = resolveLink(level, node.pos());
			if (link == null || !link.getNetworkKey().equals(key))
				continue;
			if (link.getFrequencyStack(true).isEmpty() || link.getFrequencyStack(false).isEmpty())
				continue;

			BlockState state = level.getBlockState(node.pos());
			VoidNodeType type = VoidNodeType.fromBlockState(state);
			if (!VoidNodeType.isTerminalDestination(type))
				continue;

			terminals.add(node);
		}

		return terminals;
	}

	public static List<VoidNodeEntry> listNodes(ServerLevel terminalLevel, NetworkKey key, BlockPos terminalPos) {
		ResourceLocation terminalDimension = WorldHelper.getDimensionID(terminalLevel);
		VoidNodeNamesData names = VoidwayMod.VOID_NODE_NAMES_DATA;
		List<VoidNodeEntry> entries = new ArrayList<>();

		for (VoidNodeNetworkIndex.DimensionalBlockPos node : collectValidTerminals(terminalLevel, key)) {
			ServerLevel level = terminalLevel.getServer()
					.getLevel(ResourceKey.create(Registries.DIMENSION, node.dimension()));
			BlockState state = level != null ? level.getBlockState(node.pos()) : null;
			VoidNodeType type = state != null ? VoidNodeType.fromBlockState(state) : VoidNodeType.UNKNOWN;
			String customName = names != null ? names.getName(node.dimension(), node.pos()) : null;
			String renameName = customName != null && !customName.isBlank()
					? customName
					: buildDefaultName(type, node.pos());
			String displayName = withDimensionPrefix(renameName, node.dimension(), terminalDimension);
			boolean currentTerminal = node.dimension().equals(terminalDimension) && node.pos().equals(terminalPos);
			int distanceBlocks = computeDistanceBlocks(terminalLevel, terminalDimension, terminalPos,
					node.dimension(), node.pos());
			entries.add(new VoidNodeEntry(node.dimension(), node.pos(), type.name(), displayName, renameName,
					currentTerminal, distanceBlocks));
		}

		entries.sort(Comparator
				.comparing(VoidNodeEntry::currentTerminal).reversed()
				.thenComparing(VoidNodeEntry::dimension)
				.thenComparing(e -> e.displayName().toLowerCase(Locale.ROOT)));
		return entries;
	}

	@Nullable
	public static VoidLinkBehaviour resolveLink(ServerLevel level, BlockPos pos) {
		BlockPos basePos = VoidNodeTerminalMultiblock.getBasePos(level, pos);
		if (basePos != null)
			pos = basePos;

		VoidLinkBehaviour link = BlockEntityBehaviour.get(level, pos, VoidLinkBehaviour.TYPE);
		if (link != null)
			return link;

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof VoidTeleportPadTileEntity pad) {
			BlockPos linkPos = pad.getBoundLinkPos();
			if (linkPos != null)
				return BlockEntityBehaviour.get(level, linkPos, VoidLinkBehaviour.TYPE);
		}
		return null;
	}

	private static String buildDefaultName(VoidNodeType type, BlockPos pos) {
		String typeText = type == VoidNodeType.UNKNOWN
				? type.name()
				: Component.translatable("block.createvoidway." + type.getBlockKey()).getString();
		return typeText + " (" + VoidNodeType.formatCoords(pos) + ")";
	}

	private static String withDimensionPrefix(String base, ResourceLocation dimension, ResourceLocation terminalDimension) {
		if (dimension.equals(terminalDimension))
			return base;
		return dimensionLabel(dimension) + " \u203A " + base;
	}

	private static int computeDistanceBlocks(ServerLevel level, ResourceLocation fromDimension, BlockPos fromPos,
			ResourceLocation toDimension, BlockPos toPos) {
		if (!fromDimension.equals(toDimension))
			return VoidNodeEntry.DISTANCE_OTHER_DIMENSION;
		return distanceBlocks(level, fromPos, toPos);
	}

	public static int distanceBlocks(ServerLevel level, BlockPos fromPos, BlockPos toPos) {
		return com.xeli.createvoidway.compat.VoidwaySableCompat.distanceBlocks(level, fromPos, toPos);
	}

	public static String dimensionLabel(ResourceLocation dimension) {
		return Component.translatable(dimensionTranslationKey(dimension)).getString();
	}

	public static String dimensionTranslationKey(ResourceLocation dimension) {
		return "dimension." + dimension.getNamespace() + "." + dimension.getPath();
	}

}
