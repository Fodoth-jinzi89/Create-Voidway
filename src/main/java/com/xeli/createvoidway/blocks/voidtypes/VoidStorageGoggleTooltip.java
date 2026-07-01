package com.xeli.createvoidway.blocks.voidtypes;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.battery.VoidBatteryLinkMetrics;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class VoidStorageGoggleTooltip {

	private VoidStorageGoggleTooltip() {}

	public static void addRoleAndLink(List<Component> tooltip, String keyPrefix, boolean isInput, int linkedPartners) {
		new LangBuilder(VoidwayMod.ID)
				.translate(keyPrefix + (isInput ? "_input.role" : "_output.role"))
				.forGoggles(tooltip);

		new LangBuilder(VoidwayMod.ID)
				.translate(keyPrefix + (isInput ? "_input.linked_outputs" : "_output.linked_inputs"), linkedPartners)
				.forGoggles(tooltip);

		if (linkedPartners == 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".not_linked")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		}
	}

	public static void addKineticStatus(List<Component> tooltip, String keyPrefix,
										int speedRpm, int channelStressDemand, int transferFluidDrain,
										boolean hasShaftConnection, boolean hasSource, boolean isOverStressed,
										boolean hasRequiredStress, boolean hasSufficientTransferFluid,
										boolean canOperate) {
		addKineticStatus(tooltip, keyPrefix, speedRpm, channelStressDemand, transferFluidDrain,
				hasShaftConnection, hasSource, isOverStressed, hasRequiredStress,
				hasSufficientTransferFluid, canOperate, hasSufficientTransferFluid, 0, 0, false);
	}

	public static void addKineticStatus(List<Component> tooltip, String keyPrefix,
										int speedRpm, int channelStressDemand, int transferFluidDrain,
										boolean hasShaftConnection, boolean hasSource, boolean isOverStressed,
										boolean hasRequiredStress, boolean hasSufficientTransferFluid,
										boolean canOperate, boolean usesEfficientTransfer,
										int dryTransferLossPercent, int linkDistanceBlocks,
										boolean showsDryTransferStatus) {
		if (!hasShaftConnection) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".no_shaft")
					.forGoggles(tooltip);
			return;
		}

		if (speedRpm == 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".needs_rotation")
					.forGoggles(tooltip);
			return;
		}

		new LangBuilder(VoidwayMod.ID)
				.translate(keyPrefix + ".speed", speedRpm)
				.forGoggles(tooltip);
		new LangBuilder(VoidwayMod.ID)
				.translate(keyPrefix + ".channel_stress_demand", channelStressDemand)
				.forGoggles(tooltip);
		if (transferFluidDrain > 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".transfer_fluid_drain_per_tick", transferFluidDrain)
					.forGoggles(tooltip);
		}

		if (isOverStressed) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".over_stressed")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (!hasSource) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".no_stress_source")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (canOperate) {
			if (usesEfficientTransfer) {
				new LangBuilder(VoidwayMod.ID)
						.translate(keyPrefix + ".operating")
						.style(ChatFormatting.GREEN)
						.forGoggles(tooltip);
			} else if (showsDryTransferStatus) {
				new LangBuilder(VoidwayMod.ID)
						.translate(keyPrefix + ".operating_dry", dryTransferLossPercent, linkDistanceBlocks)
						.style(ChatFormatting.YELLOW)
						.forGoggles(tooltip);
			} else {
				new LangBuilder(VoidwayMod.ID)
						.translate(keyPrefix + ".operating")
						.style(ChatFormatting.GREEN)
						.forGoggles(tooltip);
			}
		} else if (!hasSufficientTransferFluid) {
			new LangBuilder(VoidwayMod.ID)
					.translate(keyPrefix + ".no_transfer_fluid")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		}
	}

	public static void addBatteryKineticStatus(List<Component> tooltip,
											   int speedRpm, int channelStressDemand, int transferFluidDrain,
											   boolean hasShaftConnection, boolean hasSource, boolean isOverStressed,
											   boolean hasRequiredStress, boolean hasSufficientTransferFluid,
											   boolean canOperate, boolean networkUsesEfficientTransfer,
											   boolean isDryTransferMode, int dryTransferLossPercent,
											   int linkDistanceBlocks, int linkedPartners,
											   VoidBatteryLinkMetrics.NetworkFluidReadiness networkFluidReadiness) {
		if (!hasShaftConnection) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_battery.no_shaft")
					.forGoggles(tooltip);
			return;
		}

		if (speedRpm == 0) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_battery.needs_rotation")
					.forGoggles(tooltip);
			return;
		}

		new LangBuilder(VoidwayMod.ID)
				.translate("void_battery.speed", speedRpm)
				.forGoggles(tooltip);
		new LangBuilder(VoidwayMod.ID)
				.translate("void_battery.channel_stress_demand", channelStressDemand)
				.forGoggles(tooltip);

		if (hasRequiredStress && linkedPartners > 0) {
			if (networkFluidReadiness.bothReady()) {
				new LangBuilder(VoidwayMod.ID)
						.translate("void_battery.network_fluid_both_ready")
						.style(ChatFormatting.GREEN)
						.forGoggles(tooltip);
			} else if (!networkFluidReadiness.inputReady() && !networkFluidReadiness.outputReady()) {
				new LangBuilder(VoidwayMod.ID)
						.translate("void_battery.network_fluid_missing_both")
						.style(ChatFormatting.YELLOW)
						.forGoggles(tooltip);
			} else if (!networkFluidReadiness.inputReady()) {
				new LangBuilder(VoidwayMod.ID)
						.translate("void_battery.network_fluid_missing_input")
						.style(ChatFormatting.YELLOW)
						.forGoggles(tooltip);
			} else {
				new LangBuilder(VoidwayMod.ID)
						.translate("void_battery.network_fluid_missing_output")
						.style(ChatFormatting.YELLOW)
						.forGoggles(tooltip);
			}
		}

		if (transferFluidDrain > 0 && networkUsesEfficientTransfer) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_battery.transfer_fluid_drain_per_tick", transferFluidDrain)
					.forGoggles(tooltip);
		}

		if (isOverStressed) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_battery.over_stressed")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (!hasSource) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_battery.no_stress_source")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		} else if (canOperate) {
			if (networkUsesEfficientTransfer) {
				new LangBuilder(VoidwayMod.ID)
						.translate("void_battery.operating_efficient")
						.style(ChatFormatting.GREEN)
						.forGoggles(tooltip);
			} else if (isDryTransferMode) {
				if (dryTransferLossPercent > 0) {
					new LangBuilder(VoidwayMod.ID)
							.translate("void_battery.operating_dry", dryTransferLossPercent, linkDistanceBlocks)
							.style(ChatFormatting.YELLOW)
							.forGoggles(tooltip);
				} else {
					new LangBuilder(VoidwayMod.ID)
							.translate("void_battery.operating_dry_no_loss")
							.style(ChatFormatting.YELLOW)
							.forGoggles(tooltip);
				}
			} else {
				new LangBuilder(VoidwayMod.ID)
						.translate("void_battery.operating")
						.style(ChatFormatting.GREEN)
						.forGoggles(tooltip);
			}
		} else if (networkUsesEfficientTransfer && !hasSufficientTransferFluid) {
			new LangBuilder(VoidwayMod.ID)
					.translate("void_battery.no_transfer_fluid")
					.style(ChatFormatting.RED)
					.forGoggles(tooltip);
		}
	}

}
