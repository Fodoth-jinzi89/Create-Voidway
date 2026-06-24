package com.xeli.createvoidway.config;

import com.xeli.createvoidway.VoidwayMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = VoidwayMod.ID)
public final class VoidwayConfig {

	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

	private static final ModConfigSpec.IntValue VOID_MOTOR_INPUT_LOCAL_STRESS_PERCENT = BUILDER
			.comment("Percentage of locally received stress consumed by the void motor input.")
			.comment("The remainder is injected into the voidway channel.")
			.defineInRange("voidMotorInputLocalStressPercent", 10, 0, 100);

	private static final ModConfigSpec.IntValue VOID_MOTOR_INPUT_TRANSFER_FLUID_DRAIN_PERCENT = BUILDER
			.comment("Percentage of channel stress contribution consumed as void transfer fluid each tick.")
			.comment("The actual drain is the greater of this value and voidMotorInputTransferFluidDrainMinMbPerTick.")
			.comment("For example, 1 means 500 SU contributed drains 5 mB per tick.")
			.defineInRange("voidMotorInputTransferFluidDrainPercent", 1, 0, 100);

	private static final ModConfigSpec.IntValue VOID_MOTOR_INPUT_TRANSFER_FLUID_DRAIN_MIN_MB = BUILDER
			.comment("Minimum void transfer fluid drained per tick while the input is relaying.")
			.defineInRange("voidMotorInputTransferFluidDrainMinMbPerTick", 5, 0, 10000);

	private static final ModConfigSpec.IntValue VOID_CHEST_STRESS_BASE = BUILDER
			.comment("Stress demand (SU) for void chest input and output when the linked channel is empty.")
			.comment("Demand scales linearly with occupied inventory slots up to voidChestStressAtFullChannel.")
			.defineInRange("voidChestStressBase", 256, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_CHEST_STRESS_AT_FULL_CHANNEL = BUILDER
			.comment("Stress demand (SU) for void chest input and output when all channel slots are occupied.")
			.defineInRange("voidChestStressAtFullChannel", 8192, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_CHEST_TRANSFER_FLUID_DRAIN_MB = BUILDER
			.comment("Void transfer fluid drained per tick while a void chest is operating.")
			.defineInRange("voidChestTransferFluidDrainMbPerTick", 1, 0, 10000);

	private static final ModConfigSpec.IntValue VOID_BATTERY_STRESS_BASE = BUILDER
			.comment("Stress demand (SU) for void battery input and output when the linked channel is empty.")
			.comment("Demand scales linearly with stored energy up to voidBatteryStressAtFullChannel.")
			.defineInRange("voidBatteryStressBase", 256, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_BATTERY_STRESS_AT_FULL_CHANNEL = BUILDER
			.comment("Stress demand (SU) for void battery input and output when the channel battery is full.")
			.defineInRange("voidBatteryStressAtFullChannel", 8192, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_BATTERY_TRANSFER_FLUID_DRAIN_MB = BUILDER
			.comment("Void transfer fluid drained per tick while a void battery is operating.")
			.defineInRange("voidBatteryTransferFluidDrainMbPerTick", 1, 0, 10000);

	private static final ModConfigSpec.IntValue VOID_TANK_STRESS_BASE = BUILDER
			.comment("Stress demand (SU) for void tank input and output when the linked channel is empty.")
			.comment("Demand scales linearly with stored fluid up to voidTankStressAtFullChannel.")
			.defineInRange("voidTankStressBase", 256, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_TANK_STRESS_AT_FULL_CHANNEL = BUILDER
			.comment("Stress demand (SU) for void tank input and output when the channel tank is full.")
			.defineInRange("voidTankStressAtFullChannel", 8192, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_TANK_TRANSFER_FLUID_DRAIN_MB = BUILDER
			.comment("Void transfer fluid drained per tick while a void tank is operating.")
			.defineInRange("voidTankTransferFluidDrainMbPerTick", 1, 0, 10000);

	private static final ModConfigSpec.IntValue VOID_TELEPORT_CHARGE_TICKS = BUILDER
			.comment("Ticks entities must stand on a paired void teleport pad before a batch teleport.")
			.defineInRange("voidTeleportChargeTicks", 60, 1, 6000);

	private static final ModConfigSpec.IntValue VOID_TELEPORT_LIVING_FLUID_COST_MB = BUILDER
			.comment("Void transfer fluid consumed per living entity in a batch teleport.")
			.defineInRange("voidTeleportLivingFluidCostMb", 500, 0, 100000);

	private static final ModConfigSpec.IntValue VOID_TELEPORT_ITEM_FLUID_COST_MB = BUILDER
			.comment("Void transfer fluid consumed per item entity in a batch teleport.")
			.defineInRange("voidTeleportItemFluidCostMb", 50, 0, 100000);

	private static final ModConfigSpec.IntValue VOID_TELEPORT_STRESS_BASE = BUILDER
			.comment("Base stress demand (SU) added to the distance-scaled portion of a void teleport pad link.")
			.defineInRange("voidTeleportStressBase", 64, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_TELEPORT_STRESS_PER_BLOCK = BUILDER
			.comment("Additional stress demand (SU) per block of link distance between paired pads.")
			.defineInRange("voidTeleportStressPerBlock", 12, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_TELEPORT_STRESS_MIN = BUILDER
			.comment("Minimum stress demand (SU) for a void teleport pad link.")
			.defineInRange("voidTeleportStressMin", 128, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_TELEPORT_STRESS_MAX = BUILDER
			.comment("Maximum stress demand (SU) for a void teleport pad link, regardless of distance.")
			.defineInRange("voidTeleportStressMax", 8192, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_TELEPORT_FLUID_SHARE_MB = BUILDER
			.comment("Void transfer fluid moved per tick from the fuller paired pad to the emptier one while rotating.")
			.comment("Set to 0 to disable paired fluid balancing.")
			.defineInRange("voidTeleportFluidShareMbPerTick", 0, 0, 10000);

	private static final ModConfigSpec.IntValue VOID_TELEPORT_FLUID_CAPACITY = BUILDER
			.comment("Internal void transfer fluid tank capacity (mB) for void teleport pads.")
			.defineInRange("voidTeleportFluidCapacity", 1000, 1, 1000000);

	private static final ModConfigSpec.IntValue VOID_NODE_TERMINAL_STRESS = BUILDER
			.comment("Base multiplier for void node terminal stress demand.")
			.comment("Demand = this value x terminal count x dimension count x farthest same-dimension terminal distance.")
			.comment("If the current dimension has only one terminal, the distance factor is treated as 1.")
			.defineInRange("voidNodeTerminalStressDemand", 64, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_NODE_TERMINAL_STRESS_MAX = BUILDER
			.comment("Maximum stress demand (SU) for a void node terminal after dynamic scaling.")
			.defineInRange("voidNodeTerminalStressMax", 16384, 0, 1000000);

	private static final ModConfigSpec.IntValue VOID_NODE_TERMINAL_TRANSFER_FLUID_DRAIN_MB = BUILDER
			.comment("Void transfer fluid drained per tick while a void node terminal is operating.")
			.defineInRange("voidNodeTerminalTransferFluidDrainMbPerTick", 1, 0, 10000);

	private static final ModConfigSpec.IntValue VOID_NODE_TERMINAL_TELEPORT_FLUID_COST_MB = BUILDER
			.comment("Void transfer fluid consumed when teleporting through a void node terminal.")
			.defineInRange("voidNodeTerminalTeleportFluidCostMb", 500, 0, 100000);

	private static final ModConfigSpec.IntValue PORTABLE_VOID_TERMINAL_TELEPORT_FLUID_COST_MB = BUILDER
			.comment("Void transfer fluid consumed from the destination void node terminal when teleporting via a portable void terminal.")
			.defineInRange("portableVoidTerminalTeleportFluidCostMb", 4000, 0, 100000);

	private static final ModConfigSpec.IntValue PORTABLE_VOID_TERMINAL_COOLDOWN_SECONDS = BUILDER
			.comment("Seconds a void node terminal cannot send or receive teleports after a portable void terminal teleport.")
			.defineInRange("portableVoidTerminalCooldownSeconds", 20, 0, 3600);

	public static final ModConfigSpec SPEC = BUILDER.build();

	private static int voidMotorInputLocalStressPercent = VOID_MOTOR_INPUT_LOCAL_STRESS_PERCENT.getDefault();
	private static int voidMotorInputTransferFluidDrainPercent = VOID_MOTOR_INPUT_TRANSFER_FLUID_DRAIN_PERCENT.getDefault();
	private static int voidMotorInputTransferFluidDrainMinMbPerTick = VOID_MOTOR_INPUT_TRANSFER_FLUID_DRAIN_MIN_MB.getDefault();
	private static int voidChestStressBase = VOID_CHEST_STRESS_BASE.getDefault();
	private static int voidChestStressAtFullChannel = VOID_CHEST_STRESS_AT_FULL_CHANNEL.getDefault();
	private static int voidChestTransferFluidDrainMbPerTick = VOID_CHEST_TRANSFER_FLUID_DRAIN_MB.getDefault();
	private static int voidBatteryStressBase = VOID_BATTERY_STRESS_BASE.getDefault();
	private static int voidBatteryStressAtFullChannel = VOID_BATTERY_STRESS_AT_FULL_CHANNEL.getDefault();
	private static int voidBatteryTransferFluidDrainMbPerTick = VOID_BATTERY_TRANSFER_FLUID_DRAIN_MB.getDefault();
	private static int voidTankStressBase = VOID_TANK_STRESS_BASE.getDefault();
	private static int voidTankStressAtFullChannel = VOID_TANK_STRESS_AT_FULL_CHANNEL.getDefault();
	private static int voidTankTransferFluidDrainMbPerTick = VOID_TANK_TRANSFER_FLUID_DRAIN_MB.getDefault();
	private static int voidTeleportChargeTicks = VOID_TELEPORT_CHARGE_TICKS.getDefault();
	private static int voidTeleportLivingFluidCostMb = VOID_TELEPORT_LIVING_FLUID_COST_MB.getDefault();
	private static int voidTeleportItemFluidCostMb = VOID_TELEPORT_ITEM_FLUID_COST_MB.getDefault();
	private static int voidTeleportFluidShareMbPerTick = VOID_TELEPORT_FLUID_SHARE_MB.getDefault();
	private static int voidTeleportStressBase = VOID_TELEPORT_STRESS_BASE.getDefault();
	private static int voidTeleportStressPerBlock = VOID_TELEPORT_STRESS_PER_BLOCK.getDefault();
	private static int voidTeleportStressMin = VOID_TELEPORT_STRESS_MIN.getDefault();
	private static int voidTeleportStressMax = VOID_TELEPORT_STRESS_MAX.getDefault();
	private static int voidTeleportFluidCapacity = VOID_TELEPORT_FLUID_CAPACITY.getDefault();
	private static int voidNodeTerminalStressDemand = VOID_NODE_TERMINAL_STRESS.getDefault();
	private static int voidNodeTerminalStressMax = VOID_NODE_TERMINAL_STRESS_MAX.getDefault();
	private static int voidNodeTerminalTransferFluidDrainMbPerTick = VOID_NODE_TERMINAL_TRANSFER_FLUID_DRAIN_MB.getDefault();
	private static int voidNodeTerminalTeleportFluidCostMb = VOID_NODE_TERMINAL_TELEPORT_FLUID_COST_MB.getDefault();
	private static int portableVoidTerminalTeleportFluidCostMb = PORTABLE_VOID_TERMINAL_TELEPORT_FLUID_COST_MB.getDefault();
	private static int portableVoidTerminalCooldownSeconds = PORTABLE_VOID_TERMINAL_COOLDOWN_SECONDS.getDefault();

	private VoidwayConfig() {}

	public static float getInputLocalStressFraction() {
		return voidMotorInputLocalStressPercent / 100f;
	}

	public static float getInputNetworkContributionFraction() {
		return 1f - getInputLocalStressFraction();
	}

	public static int getInputLocalStressPercent() {
		return voidMotorInputLocalStressPercent;
	}

	public static int getInputTransferFluidDrainPercent() {
		return voidMotorInputTransferFluidDrainPercent;
	}

	public static int getInputTransferFluidDrainMinMbPerTick() {
		return voidMotorInputTransferFluidDrainMinMbPerTick;
	}

	public static int getVoidChestStressBase() {
		return voidChestStressBase;
	}

	public static int getVoidChestStressAtFullChannel() {
		return voidChestStressAtFullChannel;
	}

	public static int getVoidChestTransferFluidDrainMbPerTick() {
		return voidChestTransferFluidDrainMbPerTick;
	}

	public static int getVoidBatteryStressBase() {
		return voidBatteryStressBase;
	}

	public static int getVoidBatteryStressAtFullChannel() {
		return voidBatteryStressAtFullChannel;
	}

	public static int getVoidBatteryTransferFluidDrainMbPerTick() {
		return voidBatteryTransferFluidDrainMbPerTick;
	}

	public static int getVoidTankStressBase() {
		return voidTankStressBase;
	}

	public static int getVoidTankStressAtFullChannel() {
		return voidTankStressAtFullChannel;
	}

	public static int getVoidTankTransferFluidDrainMbPerTick() {
		return voidTankTransferFluidDrainMbPerTick;
	}

	public static int getVoidTeleportChargeTicks() {
		return voidTeleportChargeTicks;
	}

	public static int getVoidTeleportItemFluidCostMb() {
		return voidTeleportItemFluidCostMb;
	}

	public static int getVoidTeleportLivingFluidCostMb() {
		return voidTeleportLivingFluidCostMb;
	}

	public static int getVoidTeleportFluidShareMbPerTick() {
		return voidTeleportFluidShareMbPerTick;
	}

	public static boolean isVoidTeleportFluidSharingEnabled() {
		return voidTeleportFluidShareMbPerTick > 0;
	}

	public static int getVoidTeleportStressBase() {
		return voidTeleportStressBase;
	}

	public static int getVoidTeleportStressPerBlock() {
		return voidTeleportStressPerBlock;
	}

	public static int getVoidTeleportStressMin() {
		return voidTeleportStressMin;
	}

	public static int getVoidTeleportStressMax() {
		return voidTeleportStressMax;
	}

	public static int getVoidTeleportFluidCapacity() {
		return voidTeleportFluidCapacity;
	}

	public static int getVoidNodeTerminalStressMultiplier() {
		return voidNodeTerminalStressDemand;
	}

	public static int getVoidNodeTerminalStressMax() {
		return voidNodeTerminalStressMax;
	}

	public static int getVoidNodeTerminalTransferFluidDrainMbPerTick() {
		return voidNodeTerminalTransferFluidDrainMbPerTick;
	}

	public static int getVoidNodeTerminalTeleportFluidCostMb() {
		return voidNodeTerminalTeleportFluidCostMb;
	}

	public static int getPortableVoidTerminalTeleportFluidCostMb() {
		return portableVoidTerminalTeleportFluidCostMb;
	}

	public static int getPortableVoidTerminalCooldownSeconds() {
		return portableVoidTerminalCooldownSeconds;
	}

	private static void load() {
		voidMotorInputLocalStressPercent = VOID_MOTOR_INPUT_LOCAL_STRESS_PERCENT.get();
		voidMotorInputTransferFluidDrainPercent = VOID_MOTOR_INPUT_TRANSFER_FLUID_DRAIN_PERCENT.get();
		voidMotorInputTransferFluidDrainMinMbPerTick = VOID_MOTOR_INPUT_TRANSFER_FLUID_DRAIN_MIN_MB.get();
		voidChestStressBase = VOID_CHEST_STRESS_BASE.get();
		voidChestStressAtFullChannel = VOID_CHEST_STRESS_AT_FULL_CHANNEL.get();
		voidChestTransferFluidDrainMbPerTick = VOID_CHEST_TRANSFER_FLUID_DRAIN_MB.get();
		voidBatteryStressBase = VOID_BATTERY_STRESS_BASE.get();
		voidBatteryStressAtFullChannel = VOID_BATTERY_STRESS_AT_FULL_CHANNEL.get();
		voidBatteryTransferFluidDrainMbPerTick = VOID_BATTERY_TRANSFER_FLUID_DRAIN_MB.get();
		voidTankStressBase = VOID_TANK_STRESS_BASE.get();
		voidTankStressAtFullChannel = VOID_TANK_STRESS_AT_FULL_CHANNEL.get();
		voidTankTransferFluidDrainMbPerTick = VOID_TANK_TRANSFER_FLUID_DRAIN_MB.get();
		voidTeleportChargeTicks = VOID_TELEPORT_CHARGE_TICKS.get();
		voidTeleportLivingFluidCostMb = VOID_TELEPORT_LIVING_FLUID_COST_MB.get();
		voidTeleportItemFluidCostMb = VOID_TELEPORT_ITEM_FLUID_COST_MB.get();
		voidTeleportFluidShareMbPerTick = VOID_TELEPORT_FLUID_SHARE_MB.get();
		voidTeleportStressBase = VOID_TELEPORT_STRESS_BASE.get();
		voidTeleportStressPerBlock = VOID_TELEPORT_STRESS_PER_BLOCK.get();
		voidTeleportStressMin = VOID_TELEPORT_STRESS_MIN.get();
		voidTeleportStressMax = VOID_TELEPORT_STRESS_MAX.get();
		voidTeleportFluidCapacity = VOID_TELEPORT_FLUID_CAPACITY.get();
		voidNodeTerminalStressDemand = VOID_NODE_TERMINAL_STRESS.get();
		voidNodeTerminalStressMax = VOID_NODE_TERMINAL_STRESS_MAX.get();
		voidNodeTerminalTransferFluidDrainMbPerTick = VOID_NODE_TERMINAL_TRANSFER_FLUID_DRAIN_MB.get();
		voidNodeTerminalTeleportFluidCostMb = VOID_NODE_TERMINAL_TELEPORT_FLUID_COST_MB.get();
		portableVoidTerminalTeleportFluidCostMb = PORTABLE_VOID_TERMINAL_TELEPORT_FLUID_COST_MB.get();
		portableVoidTerminalCooldownSeconds = PORTABLE_VOID_TERMINAL_COOLDOWN_SECONDS.get();
	}

	@SubscribeEvent
	static void onLoad(ModConfigEvent.Loading event) {
		if (event.getConfig().getSpec() == SPEC)
			load();
	}

	@SubscribeEvent
	static void onReload(ModConfigEvent.Reloading event) {
		if (event.getConfig().getSpec() == SPEC)
			load();
	}

}
