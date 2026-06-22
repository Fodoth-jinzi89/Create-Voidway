package com.xeli.createvoidway.blocks.teleport;

import com.xeli.createvoidway.compat.VoidwaySableCompat;
import com.xeli.createvoidway.config.VoidwayConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class VoidTeleportLinkMetrics {

	private VoidTeleportLinkMetrics() {}

	public static int computeDistanceBlocks(@Nullable Level level, BlockPos from, BlockPos to) {
		return VoidwaySableCompat.distanceBlocks(level, from, to);
	}

	public static int computeRawStressDemand(int distanceBlocks) {
		if (distanceBlocks <= 0)
			return 0;
		return VoidwayConfig.getVoidTeleportStressBase()
				+ VoidwayConfig.getVoidTeleportStressPerBlock() * distanceBlocks;
	}

	public static int computeStressDemand(int distanceBlocks) {
		int raw = computeRawStressDemand(distanceBlocks);
		if (raw <= 0)
			return 0;
		return Math.min(VoidwayConfig.getVoidTeleportStressMax(),
				Math.max(VoidwayConfig.getVoidTeleportStressMin(), raw));
	}

	public static boolean isStressFloored(int distanceBlocks) {
		int raw = computeRawStressDemand(distanceBlocks);
		return raw > 0 && raw < VoidwayConfig.getVoidTeleportStressMin();
	}

	public static boolean isStressCapped(int distanceBlocks) {
		int raw = computeRawStressDemand(distanceBlocks);
		return raw > VoidwayConfig.getVoidTeleportStressMax();
	}

}
