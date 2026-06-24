package com.xeli.createvoidway.blocks.terminal;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.config.VoidChannelStress;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.fluids.RWFluids;
import com.xeli.createvoidway.fluids.VoidTransferFluidTank;
import com.xeli.createvoidway.voidlink.VoidLinkSlots;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoidNodeTerminalTileEntity extends KineticBlockEntity
		implements MenuProvider, IHaveGoggleInformation {

	public static final int FLUID_CAPACITY = 5000;

	private VoidTerminalLinkBehaviour link;
	private int openCount;
	private int syncedStressDemand;
	private long teleportCooldownUntilGameTime;

	private final VoidTransferFluidTank fluidTank = new VoidTransferFluidTank(FLUID_CAPACITY, () -> {
		if (level == null || level.isClientSide)
			return;
		setChanged();
		sendData();
	});

	public VoidNodeTerminalTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	protected void createLink() {
		var slots = VoidLinkSlots.southModelHorizontal(
				VoidNodeTerminalBlock::getLinkSlotFace,
				7.5F + VoidNodeTerminalMultiblock.FREQ_SLOT_Y_OFFSET);
		link = new VoidTerminalLinkBehaviour(this, slots);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (level != null && !level.isClientSide) {
			VoidNodeTerminalMultiblock.ensureTop(level, worldPosition,
					getBlockState().getValue(VoidNodeTerminalBlock.FACING));
		}
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);
	}

	public VoidTerminalLinkBehaviour getLink() {
		return link;
	}

	public NetworkKey getNetworkKey() {
		return link.getNetworkKey();
	}

	public VoidTransferFluidTank getFluidTank() {
		return fluidTank;
	}

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
		return VoidwayConfig.getVoidNodeTerminalTransferFluidDrainMbPerTick();
	}

	public boolean canOperate() {
		return hasRequiredStress() && hasSufficientTransferFluid();
	}

	public boolean isTeleportOnCooldown() {
		return level != null && level.getGameTime() < teleportCooldownUntilGameTime;
	}

	public void startPortableTeleportCooldown() {
		if (level == null || level.isClientSide)
			return;
		long cooldownTicks = VoidwayConfig.getPortableVoidTerminalCooldownSeconds() * 20L;
		teleportCooldownUntilGameTime = level.getGameTime() + cooldownTicks;
		sendData();
	}

	public boolean acceptsFluidFrom(Direction side) {
		if (side == null)
			return false;
		Direction facing = getBlockState().getValue(VoidNodeTerminalBlock.FACING);
		return side == facing.getClockWise() || side == facing.getCounterClockWise();
	}

	@Nullable
	public IFluidHandler getFluidHandler(Direction side) {
		if (!acceptsFluidFrom(side))
			return null;
		return fluidTank;
	}

	@Override
	public float calculateStressApplied() {
		if (!hasShaftConnection() || Math.abs(getTheoreticalSpeed()) == 0)
			return 0;
		return VoidChannelStress.toImpactPerRpm(getStressDemand(), getTheoreticalSpeed());
	}

	public int getStressDemand() {
		if (level != null && !level.isClientSide && level instanceof net.minecraft.server.level.ServerLevel serverLevel)
			return VoidNodeTerminalStress.computeDemand(serverLevel, worldPosition, getNetworkKey());
		return syncedStressDemand;
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		if (clientPacket)
			openCount = tag.getInt("OpenCount");
		if (clientPacket && tag.contains("StressDemand"))
			syncedStressDemand = tag.getInt("StressDemand");
		if (tag.contains("TeleportCooldownUntil"))
			teleportCooldownUntilGameTime = tag.getLong("TeleportCooldownUntil");
		if (tag.contains("FluidTank")) {
			fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
			fluidTank.purgeInvalidContents();
		}
		super.read(tag, registries, clientPacket);
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		if (clientPacket)
			tag.putInt("OpenCount", openCount);
		if (clientPacket)
			tag.putInt("StressDemand", syncedStressDemand);
		if (clientPacket)
			tag.putLong("TeleportCooldownUntil", teleportCooldownUntilGameTime);
		tag.putLong("TeleportCooldownUntil", teleportCooldownUntilGameTime);
		tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
		super.write(tag, registries, clientPacket);
	}

	@Override
	public @NotNull Component getDisplayName() {
		return Component.translatable("block.createvoidway.void_node_terminal");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
		if (!canOperate() || isTeleportOnCooldown())
			return null;
		if (player instanceof ServerPlayer serverPlayer)
			VoidNodeService.sendNodeList(serverPlayer, this);
		return VoidNodeTerminalContainer.create(id, playerInventory, this);
	}

	@Override
	public void tick() {
		super.tick();
		if (level != null && !level.isClientSide) {
			if (hasRequiredStress())
				fluidTank.drain(getTransferFluidDrainThisTick(), IFluidHandler.FluidAction.EXECUTE);
			int demand = getStressDemand();
			if (demand != syncedStressDemand) {
				syncedStressDemand = demand;
				sendData();
			}
		}
	}

	public void startOpen(Player player) {
		if (openCount < 0)
			openCount = 0;
		openCount++;
		sendData();
	}

	public void stopOpen(Player player) {
		openCount--;
		sendData();
	}

	public boolean hasShaftConnection() {
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof VoidNodeTerminalBlock block))
			return false;
		return block.hasShaftTowards(level, worldPosition, state, Direction.DOWN);
	}

	@Override
	protected boolean isNoisy() {
		return false;
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		new LangBuilder(VoidwayMod.ID)
				.translate("void_node_terminal.goggle.header")
				.forGoggles(tooltip);

		boolean added = containedFluidTooltip(tooltip, isPlayerSneaking, fluidTank);
		int speed = (int) Math.abs(getTheoreticalSpeed());
		tooltip.add(Component.translatable("createvoidway.void_node_terminal.goggle.stress",
				getStressDemand(), speed));
		return added;
	}

}
