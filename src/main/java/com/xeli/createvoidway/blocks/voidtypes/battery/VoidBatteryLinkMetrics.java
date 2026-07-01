package com.xeli.createvoidway.blocks.voidtypes.battery;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.IVoidStorageRelay;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageKind;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageLinkBehaviour;
import com.xeli.createvoidway.compat.VoidwaySableCompat;
import com.xeli.createvoidway.config.VoidwayConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class VoidBatteryLinkMetrics {

	public record NetworkFluidReadiness(boolean inputReady, boolean outputReady) {
		public boolean bothReady() {
			return inputReady && outputReady;
		}
	}

	private VoidBatteryLinkMetrics() {}

	public static NetworkFluidReadiness getNetworkFluidReadiness(AbstractVoidBatteryTileEntity battery) {
		Level level = battery.getLevel();
		if (level == null)
			return new NetworkFluidReadiness(false, false);

		VoidStorageLinkBehaviour link = battery.getStorageLink();
		final boolean[] inputReady = {false};
		final boolean[] outputReady = {false};

		forEachLinkedBattery(link, level, (relay, partnerLevel, pos) -> {
			if (!relay.hasRequiredStress() || !relay.hasSufficientTransferFluid())
				return;
			if (relay.isVoidBatteryInput())
				inputReady[0] = true;
			else
				outputReady[0] = true;
		});

		return new NetworkFluidReadiness(inputReady[0], outputReady[0]);
	}

	public static boolean networkHasFluidOnBothSides(AbstractVoidBatteryTileEntity battery) {
		NetworkFluidReadiness readiness = getNetworkFluidReadiness(battery);
		return readiness.bothReady();
	}

	public static int computeLinkDistanceBlocks(AbstractVoidBatteryTileEntity battery) {
		Level level = battery.getLevel();
		if (level == null)
			return 0;

		VoidStorageLinkBehaviour link = battery.getStorageLink();
		BlockPos selfPos = battery.getBlockPos();
		boolean wantOutputs = battery.isVoidBatteryInput();
		ResourceLocation selfDimension = level.dimension().location();

		final int[] maxDistance = {0};
		final boolean[] foundSameDimension = {false};
		final boolean[] foundCrossDimension = {false};

		forEachLinkedPartner(link, level, selfPos, (relay, partnerLevel, pos, dimension) -> {
			if (relay.getStorageKind() != VoidStorageKind.BATTERY)
				return;
			if (relay.isStorageOutput() == wantOutputs)
				return;

			if (!dimension.equals(selfDimension))
				foundCrossDimension[0] = true;
			else {
				foundSameDimension[0] = true;
				maxDistance[0] = Math.max(maxDistance[0],
						VoidwaySableCompat.distanceBlocks(level, selfPos, pos));
			}
		});

		if (foundCrossDimension[0])
			return VoidwayConfig.getVoidBatteryDryTransferMaxLossDistanceBlocks();
		return foundSameDimension[0] ? maxDistance[0] : 0;
	}

	public static int computeDryTransferLossPercent(int distanceBlocks) {
		int maxLoss = VoidwayConfig.getVoidBatteryDryTransferMaxLossPercent();
		if (maxLoss <= 0 || distanceBlocks <= 0)
			return 0;
		int maxDistance = Math.max(1, VoidwayConfig.getVoidBatteryDryTransferMaxLossDistanceBlocks());
		return Math.min(maxLoss, maxLoss * distanceBlocks / maxDistance);
	}

	public static float dryTransferLossFraction(int distanceBlocks) {
		return computeDryTransferLossPercent(distanceBlocks) / 100f;
	}

	public static float effectiveTransferLossFraction(AbstractVoidBatteryTileEntity battery) {
		if (networkHasFluidOnBothSides(battery))
			return 0f;
		if (!battery.isVoidBatteryInput() || !battery.hasRequiredStress())
			return 0f;
		return dryTransferLossFraction(computeLinkDistanceBlocks(battery));
	}

	private interface LinkedBatteryConsumer {
		void accept(AbstractVoidBatteryTileEntity relay, Level partnerLevel, BlockPos pos);
	}

	private interface LinkedPartnerConsumer {
		void accept(IVoidStorageRelay relay, Level partnerLevel, BlockPos pos, ResourceLocation dimension);
	}

	private static void forEachLinkedBattery(VoidStorageLinkBehaviour link, Level contextLevel,
			LinkedBatteryConsumer consumer) {
		forEachLinkedPartner(link, contextLevel, null, (relay, partnerLevel, pos, dimension) -> {
			if (relay instanceof AbstractVoidBatteryTileEntity battery)
				consumer.accept(battery, partnerLevel, pos);
		});
	}

	private static void forEachLinkedPartner(VoidStorageLinkBehaviour link, Level contextLevel,
			@Nullable BlockPos skipPos, LinkedPartnerConsumer consumer) {
		ResourceLocation contextDimension = contextLevel.dimension().location();
		VoidwayMod.VOID_STORAGE_LINK_NETWORK_HANDLER.collectPositions(link.getNetworkKey(),
				(dimension, pos) -> {
					if (skipPos != null && pos.equals(skipPos) && dimension.equals(contextDimension))
						return;

					Level partnerLevel = resolveLevel(contextLevel, dimension);
					if (partnerLevel == null || !partnerLevel.hasChunkAt(pos))
						return;

					BlockEntity blockEntity = partnerLevel.getBlockEntity(pos);
					if (!(blockEntity instanceof IVoidStorageRelay relay))
						return;
					consumer.accept(relay, partnerLevel, pos, dimension);
				});
	}

	@Nullable
	private static Level resolveLevel(Level contextLevel, ResourceLocation dimension) {
		if (contextLevel.dimension().location().equals(dimension))
			return contextLevel;
		MinecraftServer server = contextLevel.getServer();
		if (server == null)
			return null;
		return server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
	}

}
