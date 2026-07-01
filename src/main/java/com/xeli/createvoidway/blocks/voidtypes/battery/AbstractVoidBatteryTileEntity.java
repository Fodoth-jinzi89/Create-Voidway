package com.xeli.createvoidway.blocks.voidtypes.battery;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.VoidwayClient;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.IVoidStorageRelay;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageGoggleTooltip;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageKind;
import com.xeli.createvoidway.blocks.voidtypes.VoidStorageLinkBehaviour;
import com.xeli.createvoidway.config.VoidChannelStress;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.fluids.RWFluids;
import com.xeli.createvoidway.fluids.VoidTransferFluidTank;
import com.xeli.createvoidway.voidlink.VoidLinkSlot;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractVoidBatteryTileEntity extends KineticBlockEntity
		implements IHaveGoggleInformation, IVoidStorageRelay {

	public static final int FLUID_CAPACITY = 4000;

	protected VoidStorageLinkBehaviour link;
	protected int linkedPartners;

	private final VoidTransferFluidTank fluidTank = new VoidTransferFluidTank(FLUID_CAPACITY, () -> {
		if (level == null || level.isClientSide)
			return;
		setChanged();
		sendData();
	});

	protected AbstractVoidBatteryTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	protected void createLink() {
		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidLinkSlot(index,
						blockState -> blockState.getValue(AbstractVoidBatteryBlock.FACING),
						VecHelper.voxelSpace(5.5F, 10.5F, -.001F)));
		link = new VoidStorageLinkBehaviour(this, slots);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);
	}

	@Override
	public VoidStorageKind getStorageKind() {
		return VoidStorageKind.BATTERY;
	}

	@Override
	public boolean isStorageOutput() {
		return !isVoidBatteryInput();
	}

	@Override
	public VoidStorageLinkBehaviour getStorageLink() {
		return link;
	}

	@Override
	public int getLinkedPartners() {
		return linkedPartners;
	}

	@Override
	public void setLinkedPartners(int partners) {
		if (linkedPartners == partners)
			return;
		linkedPartners = partners;
		sendData();
	}

	@Override
	public void updateLinkedPartnerCount(LevelAccessor world) {
		if (level == null || level.isClientSide)
			return;
		int partners = VoidwayMod.VOID_STORAGE_LINK_NETWORK_HANDLER.countLinkedPartners(world, link);
		if (partners == linkedPartners)
			return;
		linkedPartners = partners;
		sendData();
	}

	@Override
	public boolean isRelayAlive() {
		return level != null && !isRemoved() && level.isLoaded(worldPosition) && level.getBlockEntity(worldPosition) == this;
	}

	public float getChannelFillRatio() {
		VoidBattery battery = getBattery();
		int capacity = battery.getMaxEnergyStored();
		if (capacity <= 0)
			return 0;
		return battery.getEnergyStored() / (float) capacity;
	}

	public int getChannelStressDemand() {
		return VoidChannelStress.computeDemand(
				VoidwayConfig.getVoidBatteryStressBase(),
				VoidwayConfig.getVoidBatteryStressAtFullChannel(),
				getChannelFillRatio());
	}

	public abstract boolean isVoidBatteryInput();

	public boolean hasRequiredStress() {
		return hasShaftConnection() && hasSource() && !isOverStressed()
				&& Math.abs(getTheoreticalSpeed()) > 0;
	}

	public boolean hasSufficientTransferFluid() {
		if (!hasRequiredStress())
			return false;
		FluidStack stored = fluidTank.getFluid();
		if (stored.isEmpty() || !RWFluids.isAllowedInVoidMotorInput(stored))
			return false;
		return fluidTank.getFluidAmount() >= getTransferFluidDrainThisTick();
	}

	public int getTransferFluidDrainThisTick() {
		if (!hasRequiredStress())
			return 0;
		return VoidwayConfig.getVoidBatteryTransferFluidDrainMbPerTick();
	}

	public boolean canOperate() {
		if (!hasRequiredStress())
			return false;
		if (networkUsesEfficientTransfer())
			return hasSufficientTransferFluid();
		return true;
	}

	public boolean networkUsesEfficientTransfer() {
		return VoidBatteryLinkMetrics.networkHasFluidOnBothSides(this);
	}

	public boolean usesEfficientTransfer() {
		return canOperate() && networkUsesEfficientTransfer();
	}

	public int getDryTransferLossPercent() {
		return VoidBatteryLinkMetrics.computeDryTransferLossPercent(
				VoidBatteryLinkMetrics.computeLinkDistanceBlocks(this));
	}

	public boolean isDryTransferMode() {
		return hasRequiredStress() && !networkUsesEfficientTransfer();
	}

	public float getDryTransferLossFraction() {
		return VoidBatteryLinkMetrics.effectiveTransferLossFraction(this);
	}

	public FluidTank getFluidTank() {
		return fluidTank;
	}

	public boolean acceptsFluidFrom(Direction side) {
		if (side == null)
			return false;
		Direction facing = getBlockState().getValue(AbstractVoidBatteryBlock.FACING);
		return side == facing.getClockWise() || side == facing.getCounterClockWise();
	}

	@Nullable
	public IFluidHandler getFluidHandler(Direction side) {
		if (!acceptsFluidFrom(side))
			return null;
		return fluidTank;
	}

	private boolean hasPersistentData() {
		return level != null && !level.isClientSide;
	}

	private static VoidBatteryData getPersistentStorageData() {
		return VoidwayMod.VOID_BATTERIES_DATA;
	}

	public VoidBattery getBattery() {
		return hasPersistentData() ?
				getPersistentStorageData().computeStorageIfAbsent(link.getNetworkKey()) :
				VoidwayClient.VOID_BATTERIES.computeStorageIfAbsent(link.getNetworkKey());
	}

	public IEnergyStorage getEnergyHandler() {
		VoidBatteryFilteredEnergyStorage.Mode mode = VoidBatteryFilteredEnergyStorage.Mode.BLOCKED;
		float transferLoss = 0f;
		if (canOperate()) {
			mode = isVoidBatteryInput() ? VoidBatteryFilteredEnergyStorage.Mode.INSERT_ONLY
					: VoidBatteryFilteredEnergyStorage.Mode.EXTRACT_ONLY;
			if (!networkUsesEfficientTransfer())
				transferLoss = getDryTransferLossFraction();
		}
		return new VoidBatteryFilteredEnergyStorage(getBattery(), mode, transferLoss);
	}

	@Override
	public float calculateStressApplied() {
		if (!canOperate() || !hasShaftConnection() || Math.abs(getTheoreticalSpeed()) == 0)
			return 0;
		return VoidChannelStress.toImpactPerRpm(getChannelStressDemand(), getTheoreticalSpeed());
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		if (clientPacket)
			getBattery().deserializeNBT(tag.getCompound("Battery"));
		linkedPartners = tag.getInt("LinkedPartners");
		if (tag.contains("FluidTank")) {
			fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
			fluidTank.purgeInvalidContents();
		}
		super.read(tag, registries, clientPacket);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		if (clientPacket)
			tag.put("Battery", getBattery().serializeNBT());
		tag.putInt("LinkedPartners", linkedPartners);
		tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
		super.write(tag, registries, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();
		if (level != null && !level.isClientSide && usesEfficientTransfer())
			fluidTank.drain(getTransferFluidDrainThisTick(), IFluidHandler.FluidAction.EXECUTE);
	}

	public boolean hasShaftConnection() {
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof AbstractVoidBatteryBlock<?> block))
			return false;
		return block.hasShaftTowards(level, worldPosition, state, Direction.DOWN);
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		VoidStorageGoggleTooltip.addRoleAndLink(tooltip, "void_battery", isVoidBatteryInput(), linkedPartners);

		VoidBattery battery = getBattery();
		new LangBuilder(VoidwayMod.ID)
				.translate("void_battery.channel_energy", battery.getEnergyStored(), battery.getMaxEnergyStored())
				.forGoggles(tooltip);

		boolean added = containedFluidTooltip(tooltip, isPlayerSneaking, fluidTank);

		int speed = (int) Math.abs(getTheoreticalSpeed());
		VoidStorageGoggleTooltip.addBatteryKineticStatus(tooltip, speed,
				getChannelStressDemand(), getTransferFluidDrainThisTick(),
				hasShaftConnection(), hasSource(), isOverStressed(), hasRequiredStress(),
				hasSufficientTransferFluid(), canOperate(), networkUsesEfficientTransfer(),
				isDryTransferMode(), getDryTransferLossPercent(),
				VoidBatteryLinkMetrics.computeLinkDistanceBlocks(this),
				linkedPartners,
				VoidBatteryLinkMetrics.getNetworkFluidReadiness(this));

		return added;
	}

}
