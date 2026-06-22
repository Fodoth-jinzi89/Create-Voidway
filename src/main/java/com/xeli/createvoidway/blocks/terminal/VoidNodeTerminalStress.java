package com.xeli.createvoidway.blocks.terminal;

import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.config.VoidwayConfig;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public final class VoidNodeTerminalStress {

	private VoidNodeTerminalStress() {
	}

	public static int computeDemand(ServerLevel terminalLevel, BlockPos terminalPos, NetworkKey key) {
		List<VoidNodeNetworkIndex.DimensionalBlockPos> terminals =
				VoidNodeDiscovery.collectValidTerminals(terminalLevel, key);
		int terminalCount = terminals.size();
		if (terminalCount == 0)
			return 0;

		ResourceLocation terminalDimension = WorldHelper.getDimensionID(terminalLevel);
		int dimensionCount = (int) terminals.stream()
				.map(VoidNodeNetworkIndex.DimensionalBlockPos::dimension)
				.distinct()
				.count();

		int terminalsInCurrentDimension = 0;
		int farthestDistance = 0;
		for (VoidNodeNetworkIndex.DimensionalBlockPos node : terminals) {
			if (!node.dimension().equals(terminalDimension))
				continue;
			terminalsInCurrentDimension++;
			if (node.pos().equals(terminalPos))
				continue;
			farthestDistance = Math.max(farthestDistance,
					VoidNodeDiscovery.distanceBlocks(terminalLevel, terminalPos, node.pos()));
		}

		if (terminalsInCurrentDimension <= 1)
			farthestDistance = 1;

		long demand = (long) VoidwayConfig.getVoidNodeTerminalStressMultiplier()
				* terminalCount
				* dimensionCount
				* farthestDistance;
		if (demand > Integer.MAX_VALUE)
			demand = Integer.MAX_VALUE;
		return (int) Math.min(demand, VoidwayConfig.getVoidNodeTerminalStressMax());
	}

}
